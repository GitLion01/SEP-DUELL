package com.example.demo.game;
import com.example.demo.cards.Card;
import com.example.demo.cards.Rarity;
import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.dto.UserDTO;
import com.example.demo.duellHerausforderung.Notification;
import com.example.demo.game.requests.*;
import com.example.demo.leaderboard.LeaderboardService;
import com.example.demo.turnier.TurnierService;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import com.fasterxml.jackson.databind.util.JSONPObject;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Transactional
@Service
@AllArgsConstructor
public class GameService {
    private final GameRepository gameRepository;
    private final DeckRepository deckRepository;
    private final UserAccountRepository userAccountRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerStateRepository playerStateRepository;
    private final PlayerCardRepository playerCardRepository;
    private final LeaderboardService leaderboardService;
    private final StatisticRepository statisticRepository;
    private final TurnierService turnierService;

    public void createGame(CreateGameRequest request) {
        System.out.println("Creating game for users A:" + request.getUserA() + " and B:" + request.getUserB());
        UserAccount userA = userAccountRepository.findById(request.getUserA())
                .orElseThrow(() -> new IllegalArgumentException("User A not found"));
        UserAccount userB = userAccountRepository.findByUsername(request.getUserB())
                .orElseThrow(() -> new IllegalArgumentException("User B not found"));


        PlayerState playerStateA = new PlayerState();
        playerStateRepository.save(playerStateA);
        userA.setPlayerState(playerStateA);
        userAccountRepository.save(userA);

        PlayerState playerStateB = new PlayerState();
        playerStateRepository.save(playerStateB);
        userB.setPlayerState(playerStateB);
        userAccountRepository.save(userB);

        Game newGame = new Game();
        newGame.getUsers().add(userA);
        newGame.getUsers().add(userB);
        newGame.setCurrentTurn(userA);
        gameRepository.save(newGame);

        List<Long> userIds = gameRepository.findAllUsersByGameId(newGame.getId());
        List<UserAccount> users = new ArrayList<>();
        for (Long userId : userIds) {
            leaderboardService.updateUserStatus(userId,"im Duell");
            users.add(userAccountRepository.findById(userId).get());
        }

        for(UserAccount user : newGame.getUsers()) {
            messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/create", Arrays.asList(newGame, users));

            Notification notification = new Notification(user.getId(),0L,userAccountRepository.findById(user.getId()).get().getUsername(),"schon aktiviert");
            messagingTemplate.convertAndSendToUser(user.getId().toString(),"/queue/notifications",notification);
            messagingTemplate.convertAndSendToUser(user.getId().toString(),"/queue/notifications",notification);
        }

    }


    public void selectDeck(DeckSelectionRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<Deck> optionalDeck = deckRepository.findByDeckIdAndUserId(request.getDeckId(), request.getUserId());
        Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserId());
        if(optionalGame.isEmpty() || optionalDeck.isEmpty() || optionalUser.isEmpty()) {
            System.out.println("in if"+ optionalGame.get().getId());
            System.out.println("in if"+ optionalDeck.get().getId());
            System.out.println("in if"+ optionalUser.get().getId());
            return;
        }

        Game game = optionalGame.get();
        Deck deck = optionalDeck.get();
        UserAccount user = optionalUser.get();

        Collections.shuffle(deck.getCards()); // mischt das Deck
        List<Card> cards = deck.getCards();
        List<PlayerCard> playerCards = cards.stream().map(card -> {
            PlayerCard playerCard = new PlayerCard();
            playerCard.setName(card.getName());
            playerCard.setAttackPoints(card.getAttackPoints());
            playerCard.setDefensePoints(card.getDefensePoints());
            playerCard.setDescription(card.getDescription());
            playerCard.setImage(card.getImage());
            playerCard.setRarity(card.getRarity());
            playerCard.setPlayerState(user.getPlayerState());
            playerCardRepository.save(playerCard);
            return playerCard;
        }).collect(Collectors.toList());

        System.out.println("+++++++++ "+user.getPlayerState().getId());
        user.getPlayerState().setDeck(deck);
        user.getPlayerState().setReady(true);
        user.getPlayerState().setDeckClone(playerCards);

        // legt dem Spieler initial 5 Karten auf die Hand
        List<PlayerCard> handCards = new ArrayList<>();
        if(game.getCurrentTurn().equals(user)) {
            handCards = playerCards.stream().limit(6).toList();
        }else{
            handCards = playerCards.stream().limit(5).toList();
        }


        user.getPlayerState().getHandCards().addAll(handCards);
        user.getPlayerState().getDeckClone().removeAll(handCards);
        System.out.println("------------ "+ deck.getId() + "  " + user.getPlayerState().getReady());

        playerStateRepository.save(user.getPlayerState());
        userAccountRepository.save(user);

        System.out.println("------------ "+ playerStateRepository.findById(user.getPlayerState().getId()).get().getReady());

        boolean allPlayersReady = game.getUsers().stream()
                .allMatch(player -> playerStateRepository.findById(player.getPlayerState().getId()).get().getReady());


        if (allPlayersReady) {
            game.resetTimer();
            game.setReady(true);
        }

        gameRepository.save(game);
        List<UserAccount> users = gameRepository.findAllUsersByGameId(game.getId()).stream()
                .map(userId -> userAccountRepository.findById(userId).orElse(null))
                .collect(Collectors.toList());


        if(game.getReady()) {
            for (UserAccount player : users) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/selectDeck", Arrays.asList(game, users));
            }
        }

        if(game.getReady() && game.getStreamed()) {
            Map<Long, List<String>> streamedGames = gameRepository.findAllStreams().get().stream()
                    .collect(Collectors.toMap(
                            Game::getId,
                            stream -> stream.getUsers().stream()
                                    .map(UserAccount::getUsername)
                                    .collect(Collectors.toList())
                    ));
            messagingTemplate.convertAndSend("/queue/streams", streamedGames);
        }

    }

    // erste Karte im Deck wird auf die Hand gelegt
    public void drawCard(DrawCardRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(request.getUserId());
        if(optionalGame.isEmpty() || optionalUserAccount.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        UserAccount userAccount = optionalUserAccount.get();

        Deck deck = userAccount.getPlayerState().getDeck();
        List<PlayerCard> handCards = userAccount.getPlayerState().getHandCards();

        // ersetzt den unteren Kommentar
        if(userAccount.getPlayerState().getDeckClone().isEmpty()) {
            return;
        }else{
            handCards.add(deck.getUser().getPlayerState().getDeckClone().remove(0));
        }


        playerStateRepository.save(userAccount.getPlayerState());
        gameRepository.save(game);
        List<UserAccount> users = game.getUsers();

        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
        }
    }

    public void placeCard(PlaceCardRequest request){
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(request.getUserId());
        Optional<PlayerCard> optionalCard = playerCardRepository.findById(request.getCardId());
        if(optionalGame.isEmpty() || optionalUserAccount.isEmpty() || optionalCard.isEmpty()) {
            return;
        }
        Game game = optionalGame.get();
        UserAccount userAccount = optionalUserAccount.get();
        PlayerCard card = optionalCard.get();
        if(!game.getCurrentTurn().equals(userAccount) ||
                userAccount.getPlayerState().getFieldCards().size() > 5
                || card.getRarity() != Rarity.NORMAL){// prüft ob der User am zug ist
            return;
        }

        userAccount.getPlayerState().getFieldCards().add(card); // Fügt Karte aus Hand dem Feld hinzu
        userAccount.getPlayerState().getHandCards().remove(card); // Löscht Karte aus Hand
        userAccount.getPlayerState().getCardsPlayed().add(card); // Fügt die Karte den gespielten Karten hinzu
        playerStateRepository.save(userAccount.getPlayerState());
        gameRepository.save(game);
        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();
        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot()));
            }
        }
    }

    public void endTurn(EndTurnRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameID());
        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(request.getUserID());
        if (optionalGame.isEmpty() || optionalUserAccount.isEmpty()) {
            return;
        }
        Game game = optionalGame.get();
        UserAccount userAccount = optionalUserAccount.get();

        int remainingLifePoints = 0;
        // Game gegen anderen Spieler
        if (game.getPlayerStateBot() == null) {
            if (!game.getCurrentTurn().equals(userAccount)) {
                return;
            }

            //Spieler der als nächstes am Zug ist wird eine Karte auf die Hand gelegt
            UserAccount notTurn = game.getCurrentTurn().equals(game.getUsers().get(0)) ? game.getUsers().get(1) : game.getUsers().get(0);
            notTurn.getPlayerState().getHandCards().add(notTurn.getPlayerState().getDeckClone().remove(0));

            game.setCurrentTurn(game.getUsers().get(0).equals(userAccount) ? game.getUsers().get(1) : game.getUsers().get(0));
            game.resetTimer();
            if (game.getFirstRound()) {
                game.setFirstRound(false);
            }
            //Status hasAttacked wird für alle Karten auf der Hand auf false gesetzt
            List<PlayerCard> cards = userAccount.getPlayerState().getFieldCards();
            for (PlayerCard card : cards) {
                card.setHasAttacked(false);
                playerCardRepository.save(card);
            }
            gameRepository.save(game);

        } else {

            //Game gegen Computer
            game.setMyTurn(false);

            //Spieler wird automatisch eine Karte auf die Hand gelegt
            userAccount.getPlayerState().getHandCards().add(userAccount.getPlayerState().getDeckClone().remove(0));

            //Aktionen des Bots

            // 1: Karte Ziehen
            if(!game.getPlayerStateBot().getDeckClone().isEmpty()) {
                game.getPlayerStateBot().getHandCards().add(game.getPlayerStateBot().getDeckClone().remove(0));
            }

            // 2: Alle Normalen Karten setzen
            List<PlayerCard> cardsToRemove = new ArrayList<>();
            for (PlayerCard card : playerStateRepository.findById(game.getPlayerStateBot().getId()).get().getHandCards()) {
                if (card.getRarity() == Rarity.NORMAL) {
                    game.getPlayerStateBot().getFieldCards().add(card);
                    cardsToRemove.add(card);
                    game.getPlayerStateBot().getCardsPlayed().add(card);
                }
            }
            game.getPlayerStateBot().getHandCards().removeAll(cardsToRemove);

            // 3: Mit allen Karten auf dem Feld angreifen

            //Gegner hat keine Karten auf Feld
            if (userAccount.getPlayerState().getFieldCards().isEmpty()) {
                List<PlayerCard> botCards = new ArrayList<>(game.getPlayerStateBot().getFieldCards());
                Iterator<PlayerCard> iterator = botCards.iterator();

                while (iterator.hasNext()) {
                    PlayerCard botFieldCard = iterator.next();
                    remainingLifePoints = userAccount.getPlayerState().getLifePoints() - botFieldCard.getAttackPoints();
                    if (botFieldCard.getAttackPoints() > userAccount.getPlayerState().getLifePoints()) {
                        // Bot hat genug Angriffspunkte, um Spieler zu besiegen
                        game.getPlayerStateBot().setDamage(game.getPlayerStateBot().getDamage() + userAccount.getPlayerState().getLifePoints() + 1);
                        userAccount.getPlayerState().setLifePoints(-1);
                        game.getPlayerStateBot().setWinner(true);
                        playerStateRepository.save(game.getPlayerStateBot());
                        iterator.remove(); // Sicher entfernen
                    } else {
                        // Spieler verliert Lebenspunkte entsprechend des Angriffs des Bots
                        userAccount.getPlayerState().setLifePoints(userAccount.getPlayerState().getLifePoints() - botFieldCard.getAttackPoints());
                        game.getPlayerStateBot().setDamage(game.getPlayerStateBot().getDamage() + botFieldCard.getAttackPoints());
                    }

                    playerCardRepository.save(botFieldCard);
                    playerStateRepository.save(game.getPlayerStateBot());
                    playerStateRepository.save(userAccount.getPlayerState());
                }

            } else {

                // Gegner hat Karten auf dem Feld
                // Greife die Karten des Gegners an, wenn Karten auf dem eigenen Feld sind
                List<PlayerCard> opponentCards = new ArrayList<>(userAccount.getPlayerState().getFieldCards());
                List<PlayerCard> botFieldCards = new ArrayList<>(game.getPlayerStateBot().getFieldCards());

                // Iteriere durch jede Karte auf dem Feld des Bots
                for (Iterator<PlayerCard> botFieldCardIterator = botFieldCards.iterator(); botFieldCardIterator.hasNext();) {
                    // Überprüfe jede Karte auf dem Feld des Gegners (Spieler)
                    PlayerCard botFieldCard = botFieldCardIterator.next();

                    for (Iterator<PlayerCard> opponentCardIterator = opponentCards.iterator(); opponentCardIterator.hasNext();) {
                        PlayerCard opponentCard = opponentCardIterator.next();
                        if (botFieldCard.getHasAttacked()) {
                            break; // Die Karte hat bereits angegriffen, überspringe sie
                        }

                        int remainingTargetDefense = opponentCard.getDefensePoints() - botFieldCard.getAttackPoints();
                        if (remainingTargetDefense < 0) {
                            // Die gegnerische Karte wird zerstört
                            game.getPlayerStateBot().setDamage(game.getPlayerStateBot().getDamage() + opponentCard.getDefensePoints() + 1); // Schaden erhöhen
                            opponentCard.setDefensePoints(-1); // Verteidigungspunkte auf -1 setzen
                            opponentCardIterator.remove(); // Karte aus der Liste entfernen
                        } else {
                            // Die gegnerische Karte überlebt den Angriff
                            opponentCard.setDefensePoints(remainingTargetDefense); // Neue Verteidigungspunkte setzen
                            game.getPlayerStateBot().setDamage(game.getPlayerStateBot().getDamage() + botFieldCard.getAttackPoints()); // Schaden erhöhen
                        }

                        // Überprüfe, ob die gegnerische Karte kontert, wenn sie nicht zerstört wurde
                        if (remainingTargetDefense > 0) {
                            int remainingAttackerDefense = botFieldCard.getDefensePoints() - opponentCard.getAttackPoints();
                            if (remainingAttackerDefense < 0) {
                                // Die Karte des Bots wird zerstört
                                botFieldCard.setDefensePoints(-1);
                                botFieldCardIterator.remove(); // Karte aus der Liste entfernen
                            } else {
                                // Die Karte des Bots überlebt den Konter
                                botFieldCard.setDefensePoints(remainingAttackerDefense); // Neue Verteidigungspunkte setzen
                            }
                        }

                        botFieldCard.setHasAttacked(true); // Die Karte des Bots hat angegriffen
                        playerCardRepository.save(botFieldCard);
                        playerCardRepository.save(opponentCard);
                        playerStateRepository.save(game.getPlayerStateBot());
                        playerStateRepository.save(userAccount.getPlayerState());
                    }
                }

                // Setze hasAttacked auf false für alle Karten des Bots nach dem Zug
                for (PlayerCard botFieldCard : game.getPlayerStateBot().getFieldCards()) {
                    botFieldCard.setHasAttacked(false);
                }

                for (PlayerCard userCard : userAccount.getPlayerState().getFieldCards()) {
                    userCard.setHasAttacked(false);
                }

                // Entferne zerstörte Karten aus den Feldern des Gegners und des Bots
                userAccount.getPlayerState().getFieldCards().clear();
                userAccount.getPlayerState().getFieldCards().addAll(opponentCards); // Nur noch nicht zerstörte Karten bleiben

                game.getPlayerStateBot().getFieldCards().clear();
                game.getPlayerStateBot().getFieldCards().addAll(botFieldCards); // Nur noch nicht zerstörte Karten bleiben
            }

            // Speichere den Spielstatus nach dem Zug
            gameRepository.save(game);
            game.setMyTurn(true);
            game.resetTimer();
            game.setFirstRound(false);

        }
        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();

        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/newTurn", game.getCurrentTurn());
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/newTurn", new Notification("Neuer Zug"));
        }

        if (!viewers.isEmpty()) {
            for (UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot()));
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/newTurn", game.getCurrentTurn());
            }
        }


        if (remainingLifePoints < 0) {
            terminateMatch(request.getGameID(), game.getPlayerStateBot().getId(), userAccount.getId());
        }

    }

    public void attackBotCard(AttackBotCardRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalAttacker = userAccountRepository.findById(request.getUserIdAttacker());
        Optional<PlayerState> optionalBotPS = playerStateRepository.findById(request.getBotPSId());
        Optional<PlayerCard> optionalAttackerCard = playerCardRepository.findById(request.getAttackerId());
        Optional<PlayerCard> optionalTarget = playerCardRepository.findById(request.getTargetId());

        if(optionalGame.isEmpty() || optionalAttacker.isEmpty() || optionalBotPS.isEmpty() || optionalAttackerCard.isEmpty() || optionalTarget.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        UserAccount attacker = optionalAttacker.get();
        PlayerState botPS = optionalBotPS.get();
        PlayerCard attackerCard = optionalAttackerCard.get();
        PlayerCard target = optionalTarget.get();
        if(attackerCard.getHasAttacked()){
            return;
        }
        if(!game.getCurrentTurn().equals(attacker) || botPS.getFieldCards().isEmpty() || attacker.getPlayerState().getFieldCards().contains(target)){
            return;
        }

        // Angriffslogik
        // Angriffspunkte von C1 von Verteidigungspunkten von C2 abziehen
        int remainingTargetDefense = target.getDefensePoints() - attackerCard.getAttackPoints();
        if (remainingTargetDefense < 0) {
            // C2 wird zerstört und vom Spielfeld entfernt
            botPS.getFieldCards().remove(target);
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + target.getDefensePoints() + 1);
            target.setDefensePoints(-1);
            System.out.println("BotCard wurde zerstört");
        } else {
            // C2 überlebt den Angriff, setze neue Verteidigungspunkte
            target.setDefensePoints(remainingTargetDefense);
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getAttackPoints());
            System.out.println("BotCard kontert");
        }
        // Wenn C2 nicht zerstört wurde, dann kontert C2
        if (remainingTargetDefense >= 0) {
            int remainingAttackerDefense = attackerCard.getDefensePoints() - target.getAttackPoints();
            if (remainingAttackerDefense < 0) {
                // C1 wird zerstört und vom Spielfeld entfernt
                attacker.getPlayerState().getFieldCards().remove(attackerCard);
                System.out.println("Konter zerstört meine Karte");
            } else {
                // C1 überlebt den Konter, setze neue Verteidigungspunkte
                attackerCard.setDefensePoints(remainingAttackerDefense);
                System.out.println("Konter zerstört meine Karte nicht");
            }
        }
        attackerCard.setHasAttacked(true);
        playerCardRepository.save(attackerCard);
        playerCardRepository.save(target);
        playerStateRepository.save(game.getUsers().get(0).getPlayerState());
        playerStateRepository.save(botPS);

        gameRepository.save(game);
        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();

        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot()));
            }
        }

    }

    public void attackCard(AttackCardRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalAttacker = userAccountRepository.findById(request.getUserIdAttacker());
        Optional<UserAccount> optionalDefender = userAccountRepository.findById(request.getUserIdDefender());
        Optional<PlayerCard> optionalAttackerCard = playerCardRepository.findById(request.getAttackerId());
        Optional<PlayerCard> optionalTarget = playerCardRepository.findById(request.getTargetId());
        if(optionalGame.isEmpty() || optionalAttacker.isEmpty() || optionalDefender.isEmpty() || optionalAttackerCard.isEmpty() || optionalTarget.isEmpty()) {
            System.out.println("Problem 1");
            return;
        }
        Game game = optionalGame.get();
        UserAccount attacker = optionalAttacker.get();
        UserAccount defender = optionalDefender.get();
        PlayerCard attackerCard = optionalAttackerCard.get();
        PlayerCard target = optionalTarget.get();
        if(attackerCard.getHasAttacked()){
            System.out.println("Karte hat bereits angegriffen");
            return;
        }
        if(!game.getCurrentTurn().equals(attacker) ||
                defender.getPlayerState().getFieldCards().isEmpty() ||
                attacker.getPlayerState().getFieldCards().contains(target)){
            System.out.println("Spieler nicht am Zug");
            return;
        }

        // Angriffslogik
        System.out.println("Start des Angriffs");
        // Angriffspunkte von C1 von Verteidigungspunkten von C2 abziehen
        int remainingTargetDefense = target.getDefensePoints() - attackerCard.getAttackPoints();
        System.out.println("RemainingTargetDefense " + remainingTargetDefense);
        if (remainingTargetDefense < 0) {
            // C2 wird zerstört und vom Spielfeld entfernt
            defender.getPlayerState().getFieldCards().remove(target);
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + target.getDefensePoints() + 1);
            target.setDefensePoints(-1);
            System.out.println("Target wurde entfernt");
        } else {
            // C2 überlebt den Angriff, setze neue Verteidigungspunkte
            target.setDefensePoints(remainingTargetDefense);
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getAttackPoints());
        }
        // Wenn C2 nicht zerstört wurde, dann kontert C2
        if (remainingTargetDefense >= 0) {
            int remainingAttackerDefense = attackerCard.getDefensePoints() - target.getAttackPoints();
            if (remainingAttackerDefense < 0) {
                // C1 wird zerstört und vom Spielfeld entfernt
                attacker.getPlayerState().getFieldCards().remove(attackerCard);
                System.out.println("Attacker wurde entfernt");
            } else {
                // C1 überlebt den Konter, setze neue Verteidigungspunkte
                attackerCard.setDefensePoints(remainingAttackerDefense);
            }
        }
        attackerCard.setHasAttacked(true);
        playerCardRepository.save(attackerCard);
        playerCardRepository.save(target);
        playerStateRepository.save(game.getUsers().get(0).getPlayerState());
        playerStateRepository.save(game.getUsers().get(1).getPlayerState());

        gameRepository.save(game);
        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();

        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot()));
            }
        }

    }



    public void attackUser(AttackUserRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalAttacker = userAccountRepository.findById(request.getAttackerId());
        Optional<UserAccount> optionalDefender = userAccountRepository.findById(request.getDefenderId());
        Optional<PlayerCard> optionalPlayerCard = playerCardRepository.findById(request.getAttackerCardId());
        if(optionalGame.isEmpty() || optionalAttacker.isEmpty() || optionalDefender.isEmpty() || optionalPlayerCard.isEmpty()) {
            return;
        }
        Game game = optionalGame.get();
        UserAccount attacker = optionalAttacker.get();
        UserAccount defender = optionalDefender.get();
        PlayerCard attackerCard = optionalPlayerCard.get();
        if(!game.getCurrentTurn().equals(attacker) || !defender.getPlayerState().getFieldCards().isEmpty() || game.getFirstRound()){
            return;
        }

        if(attackerCard.getHasAttacked()){
            return;
        }
        int remainingLifePoints = defender.getPlayerState().getLifePoints() - attackerCard.getAttackPoints();
        if(attackerCard.getAttackPoints() > defender.getPlayerState().getLifePoints()){
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + defender.getPlayerState().getLifePoints() + 1); // erhöht den Damage Counter des Angreifers
            defender.getPlayerState().setLifePoints(-1);
            attacker.getPlayerState().setWinner(true);
            playerStateRepository.save(attacker.getPlayerState());
        }else{
            defender.getPlayerState().setLifePoints((defender.getPlayerState().getLifePoints() - attackerCard.getAttackPoints())); // zieht Angriffspunkte von Lebenspunkte ab
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getAttackPoints()); // erhöht den Damage Counter des Angreifers
        }

        attackerCard.setHasAttacked(true);
        playerCardRepository.save(attackerCard);
        playerStateRepository.save(attacker.getPlayerState());
        playerStateRepository.save(defender.getPlayerState());
        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();


        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot()));
            }
        }
        if(remainingLifePoints < 0){
            terminateMatch(request.getGameId(), attacker.getId(), defender.getId());
        }
    }


    public void attackBot(AttackBotRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalAttacker = userAccountRepository.findById(request.getAttackerId());
        Optional<PlayerState> optionalBotPS = playerStateRepository.findById(request.getBotPSId());
        Optional<PlayerCard> optionalPlayerCard = playerCardRepository.findById(request.getAttackerCardId());
        if(optionalGame.isEmpty() || optionalAttacker.isEmpty() || optionalBotPS.isEmpty() || optionalPlayerCard.isEmpty()) {
            System.out.println("Problem y");
            return;
        }
        Game game = optionalGame.get();
        UserAccount attacker = optionalAttacker.get();
        PlayerState botPS = optionalBotPS.get();
        PlayerCard attackerCard = optionalPlayerCard.get();
        if(!botPS.getFieldCards().isEmpty() || game.getFirstRound()){
            System.out.println("Problem x");
            return;
        }

        if(attackerCard.getHasAttacked()){
            System.out.println("Problem z");
            return;
        }
        int remainingLifePoints = botPS.getLifePoints() - attackerCard.getAttackPoints();
        if(attackerCard.getAttackPoints() > botPS.getLifePoints()){
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + botPS.getLifePoints() + 1); // erhöht den Damage Counter des Angreifers
            botPS.setLifePoints(-1);
            attacker.getPlayerState().setWinner(true);
            playerStateRepository.save(attacker.getPlayerState());
        }else{
            botPS.setLifePoints((botPS.getLifePoints() - attackerCard.getAttackPoints())); // zieht Angriffspunkte von Lebenspunkte ab
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getAttackPoints()); // erhöht den Damage Counter des Angreifers
        }

        attackerCard.setHasAttacked(true);
        playerCardRepository.save(attackerCard);
        playerStateRepository.save(attacker.getPlayerState());
        playerStateRepository.save(botPS);
        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();


        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot()));
            }
        }
        if(remainingLifePoints < 0){
            System.out.println("match wird terminiert");
            terminateMatch(request.getGameId(), botPS.getId(), attacker.getId());
        }
    }

    public void swapForRare(RareSwapRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserId());
        Optional<PlayerCard> optionalRare = playerCardRepository.findById(request.getRareId());
        Optional<PlayerCard> optionalCard1 = playerCardRepository.findById(request.getCardIds().get(0));
        Optional<PlayerCard> optionalCard2 = playerCardRepository.findById(request.getCardIds().get(1));

        if(optionalGame.isEmpty() || optionalUser.isEmpty() || optionalRare.isEmpty() || optionalCard1.isEmpty() || optionalCard2.isEmpty()) {
            return;
        }
        Game game = optionalGame.get();
        UserAccount user = optionalUser.get();
        if(!game.getCurrentTurn().equals(user) || request.getCardIds().size() != 2) {
            return;
        }
        List<PlayerCard> hand = user.getPlayerState().getHandCards();
        List<PlayerCard> field = user.getPlayerState().getFieldCards();
        PlayerCard card1 = optionalCard1.get();
        PlayerCard card2 = optionalCard2.get();
        PlayerCard rare = optionalRare.get();
        if(rare.getRarity() != Rarity.RARE){
            return;
        }
        card1.setSacrificed(true);
        card2.setSacrificed(true);
        user.getPlayerState().getFieldCards().remove(card1);
        user.getPlayerState().getFieldCards().remove(card2);
        user.getPlayerState().getFieldCards().add(rare);
        user.getPlayerState().getCardsPlayed().add(rare);
        user.getPlayerState().getHandCards().remove(rare);

        playerStateRepository.save(user.getPlayerState());
        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();

        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot()));
            }
        }

    }

    public void swapForLegendary(LegendarySwapRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserId());
        Optional<PlayerCard> optionalLeggendary = playerCardRepository.findById(request.getLegendaryId());
        Optional<PlayerCard> optionalCard1 = playerCardRepository.findById(request.getCardIds().get(0));
        Optional<PlayerCard> optionalCard2 = playerCardRepository.findById(request.getCardIds().get(1));
        Optional<PlayerCard> optionalCard3 = playerCardRepository.findById(request.getCardIds().get(2));
        if(optionalGame.isEmpty() || optionalUser.isEmpty() || optionalCard1.isEmpty() || optionalCard2.isEmpty() || optionalCard3.isEmpty() || optionalLeggendary.isEmpty()) {
            return;
        }
        Game game = optionalGame.get();
        UserAccount user = optionalUser.get();
        if(!game.getCurrentTurn().equals(user) || request.getCardIds().size() != 3) {
            return;
        }
        PlayerCard card1 = optionalCard1.get();
        PlayerCard card2 = optionalCard2.get();
        PlayerCard card3 = optionalCard3.get();
        PlayerCard legendary = optionalLeggendary.get();
        if(legendary.getRarity() != Rarity.LEGENDARY){
            return;
        }
        card1.setSacrificed(true);
        card2.setSacrificed(true);
        card3.setSacrificed(true);
        user.getPlayerState().getFieldCards().remove(card1);
        user.getPlayerState().getFieldCards().remove(card2);
        user.getPlayerState().getFieldCards().remove(card3);
        user.getPlayerState().getFieldCards().add(legendary);
        user.getPlayerState().getCardsPlayed().add(legendary);
        user.getPlayerState().getHandCards().remove(legendary);

        playerStateRepository.save(user.getPlayerState());
        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();

        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }
        }else{
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot()));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot()));
            }
        }
    }

    public void terminateMatch(Long gameId, Long userA, Long userB) {
        System.out.println("wird exekutiert");
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        Optional<UserAccount> optionalUserA = userAccountRepository.findById(userA);
        Optional<UserAccount> optionalUserB = userAccountRepository.findById(userB);
        Optional<PlayerState> optionalBotPS = playerStateRepository.findById(userA);
        if (optionalGame.isEmpty() || optionalUserB.isEmpty() || optionalBotPS.isEmpty() && optionalUserA.isEmpty()) {
            System.out.println(optionalGame.isPresent()+" "+optionalUserB.isPresent()+" "+optionalBotPS.isPresent()+" "+optionalUserA.isPresent());
            return;
        }

        Game game = optionalGame.get();

        List<PlayerCard> raresA ;
        List<PlayerCard> legendariesA;
        List<PlayerCard> normalsA;
        List<PlayerCard> raresB;
        List<PlayerCard> legendariesB;
        List<PlayerCard> normalsB;
        List<PlayerCard> sacrificedNormalsA;
        List<PlayerCard> sacrificedRaresA;
        List<PlayerCard> sacrificedLegendariesA;
        List<PlayerCard> sacrificedNormalsB;
        List<PlayerCard> sacrificedRaresB;
        List<PlayerCard> sacrificedLegendariesB;

        Integer sepCoins = 100;
        Integer leaderBoardPointsWinner;
        Integer leaderBoardPointsLoser;
        Integer damageWinner;
        Integer damageLoser;
        List<Integer> cardsPlayedA;
        List<Integer> cardsPlayedB;
        List<Integer> sacrificedA;
        List<Integer> sacrificedB;

        if(game.getPlayerStateBot() == null) {
            UserAccount user2 = optionalUserB.get();
            UserAccount user1 = optionalUserA.get();
            normalsA = user1.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.NORMAL).toList();
            raresA = user1.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.RARE).toList();
            legendariesA = user1.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.LEGENDARY).toList();
            normalsB = user2.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.NORMAL).toList();
            raresB = user2.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.RARE).toList();
            legendariesB = user2.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.LEGENDARY).toList();
            sacrificedNormalsA = normalsA.stream().filter((x) -> x.getSacrificed()).toList();
            sacrificedRaresA = raresA.stream().filter((x) -> x.getSacrificed()).toList();
            sacrificedLegendariesA = legendariesA.stream().filter((x) -> x.getSacrificed()).toList();

            sacrificedNormalsB = normalsB.stream().filter((x) -> x.getSacrificed()).toList();
            sacrificedRaresB = raresB.stream().filter((x) -> x.getSacrificed()).toList();
            sacrificedLegendariesB = legendariesB.stream().filter((x) -> x.getSacrificed()).toList();

            cardsPlayedA = Arrays.asList(normalsA.size(), raresA.size(), legendariesA.size());
            cardsPlayedB = Arrays.asList(normalsB.size(), raresB.size(), legendariesB.size());
            sacrificedA = Arrays.asList(sacrificedNormalsA.size(), sacrificedRaresA.size(), sacrificedLegendariesA.size());
            sacrificedB = Arrays.asList(sacrificedNormalsB.size(), sacrificedRaresB.size(), sacrificedLegendariesB.size());

            Integer lbPoints1 = user1.getLeaderboardPoints();
            Integer lbPoints2 = user2.getLeaderboardPoints();


            if (user1.getPlayerState().getWinner()) {
                user1.setSepCoins(user1.getSepCoins() + 100);
                user1.setLeaderboardPoints(lbPoints1 + Math.max(50, lbPoints2 - lbPoints1));
                user2.setLeaderboardPoints(lbPoints2 - Math.max(50, (lbPoints2 - lbPoints1) / 2));
                leaderBoardPointsWinner = Math.max(50, lbPoints2 - lbPoints1);
                leaderBoardPointsLoser = -1 * (Math.max(50, (lbPoints2 - lbPoints1) / 2));
                damageWinner = user1.getPlayerState().getDamage();
                damageLoser = user2.getPlayerState().getDamage();
                //for turnier
                if(user1.isInTurnier()) {
                    user1.setInTurnier(false);
                    user2.setInTurnier(false);
                    turnierService.GewinnerSpeichern(user1);
                }
            } else {
                user2.setSepCoins(user2.getSepCoins() + 100);
                user2.setLeaderboardPoints(lbPoints2 + Math.max(50, lbPoints1 - lbPoints2));
                user1.setLeaderboardPoints(lbPoints1 - Math.max(50, (lbPoints1 - lbPoints2) / 2));
                leaderBoardPointsWinner = Math.max(50, lbPoints1 - lbPoints2);
                leaderBoardPointsLoser = -1 * (Math.max(50, (lbPoints1 - lbPoints2) / 2));
                damageWinner = user2.getPlayerState().getDamage();
                damageLoser = user1.getPlayerState().getDamage();
                if(user2.isInTurnier()) {
                    user1.setInTurnier(false);
                    user2.setInTurnier(false);
                    turnierService.GewinnerSpeichern(user2);
                }
            }

            playerStateRepository.save(user1.getPlayerState());
            playerStateRepository.save(user2.getPlayerState());
            userAccountRepository.save(user1);
            userAccountRepository.save(user2);


            Statistic statistic = new Statistic();
            statistic.setUser1(user1.getUsername());
            statistic.setUser2(user2.getUsername());
            statistic.setLPWinner(leaderBoardPointsWinner);
            statistic.setLPLoser(leaderBoardPointsLoser);
            statistic.setWinner(user1.getPlayerState().getWinner() ? user1.getUsername() : user2.getUsername());
            statisticRepository.save(statistic);


        }else{
            System.out.println("Problem 3");
            PlayerState botPS = optionalBotPS.get();
            UserAccount user1 = optionalUserB.get();
            normalsA = user1.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.NORMAL).toList();
            raresA = user1.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.RARE).toList();
            legendariesA = user1.getPlayerState().getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.LEGENDARY).toList();
            normalsB = botPS.getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.NORMAL).toList();
            raresB = botPS.getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.RARE).toList();
            legendariesB = botPS.getCardsPlayed().stream().filter((x) -> x.getRarity() == Rarity.LEGENDARY).toList();
            sacrificedNormalsA = normalsA.stream().filter((x) -> x.getSacrificed()).toList();
            sacrificedRaresA = raresA.stream().filter((x) -> x.getSacrificed()).toList();
            sacrificedLegendariesA = legendariesA.stream().filter((x) -> x.getSacrificed()).toList();

            sacrificedNormalsB = normalsB.stream().filter((x) -> x.getSacrificed()).toList();
            sacrificedRaresB = raresB.stream().filter((x) -> x.getSacrificed()).toList();
            sacrificedLegendariesB = legendariesB.stream().filter((x) -> x.getSacrificed()).toList();


            cardsPlayedA = Arrays.asList(normalsA.size(), raresA.size(), legendariesA.size());
            cardsPlayedB = Arrays.asList(normalsB.size(), raresB.size(), legendariesB.size());
            sacrificedA = Arrays.asList(sacrificedNormalsA.size(), sacrificedRaresA.size(), sacrificedLegendariesA.size());
            sacrificedB = Arrays.asList(sacrificedNormalsB.size(), sacrificedRaresB.size(), sacrificedLegendariesB.size());


            if (user1.getPlayerState().getWinner()) {
                sepCoins = 0;
                leaderBoardPointsWinner = 0;
                leaderBoardPointsLoser = 0;
                damageWinner = user1.getPlayerState().getDamage();
                damageLoser = botPS.getDamage();
            } else {
                sepCoins = 0;
                leaderBoardPointsWinner = 0;
                leaderBoardPointsLoser = 0;
                damageWinner = botPS.getDamage();
                damageLoser = user1.getPlayerState().getDamage();
            }

            playerStateRepository.save(user1.getPlayerState());
            playerStateRepository.save(botPS);
            userAccountRepository.save(user1);

            Statistic statistic = new Statistic();
            statistic.setUser1(user1.getUsername());
            statistic.setUser2("Bot");
            statistic.setLPWinner(leaderBoardPointsWinner);
            statistic.setLPLoser(leaderBoardPointsLoser);
            statistic.setWinner(user1.getPlayerState().getWinner() ? user1.getUsername() : "Bot");
            statisticRepository.save(statistic);
        }

        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers() == null ? new ArrayList<>() : game.getViewers();

        if(users.size() == 2) {
            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users, sepCoins, leaderBoardPointsWinner, leaderBoardPointsLoser,damageWinner, damageLoser, cardsPlayedA, cardsPlayedB, sacrificedA, sacrificedB));
            }
        }else{
            System.out.println("Spiel gegen Bot beendet");
            messagingTemplate.convertAndSendToUser(users.get(0).getId().toString(), "/queue/game", Arrays.asList(game, users, game.getPlayerStateBot(), sepCoins, leaderBoardPointsWinner, leaderBoardPointsLoser,damageWinner, damageLoser, cardsPlayedA, cardsPlayedB, sacrificedA, sacrificedB));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, game.getPlayerStateBot(), sepCoins, leaderBoardPointsWinner, leaderBoardPointsLoser,damageWinner, damageLoser, cardsPlayedA, cardsPlayedB, sacrificedA, sacrificedB));
            }
        }

        List<Long> deepCopyIds = new ArrayList<>();

        for(UserAccount viewer : viewers) {
            deepCopyIds.add(viewer.getId());
        }

        List<UserAccount> deepCopyViewers = new ArrayList<>();
        for(Long userId : deepCopyIds) {
            UserAccount viewer = userAccountRepository.findById(userId).get();
            deepCopyViewers.add(viewer);
        }

        for (UserAccount viewer : deepCopyViewers) {
            LeaveStreamRequest request = new LeaveStreamRequest();
            request.setUserId(viewer.getId());
            leaveStream(request);
        }

        List<Long> userIds = new ArrayList<>();
        if(game.getUsers().size() == 2) {
            for (UserAccount user : game.getUsers()) {
                userIds.add(user.getId());
            }
        }else{
            userIds.addAll(List.of(users.get(0).getId(), optionalBotPS.get().getId()));
        }

        deleteUserGameData(userIds, game.getId());

        Map<Long, List<String>> streamedGames = new HashMap<>();
        Optional<List<Game>> optionalStreams = gameRepository.findAllStreams();
        if(optionalStreams.isPresent()) {
            List<Game> streams = optionalStreams.get();
            for (Game stream : streams) {
                if (stream.getUsers().size() == 2) {
                    streamedGames.put(stream.getId(), List.of(stream.getUsers().get(0).getUsername(), stream.getUsers().get(1).getUsername()));
                } else {
                    streamedGames.put(stream.getId(), List.of(stream.getUsers().get(0).getUsername(), "Bot"));
                }
            }
            messagingTemplate.convertAndSend("/queue/streams", streamedGames);
        }
    }



    public void surrender(SurrenderRequest request){
        System.out.println("Aufgeben wird ausgeführt");
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(request.getUserId());
        Optional<PlayerState> optionalBotPS = playerStateRepository.findById(request.getUserId());
        if(optionalGame.isEmpty() || optionalUserAccount.isEmpty() && optionalBotPS.isEmpty()){
            System.out.println("Aufgeben1");
            return;
        }

        Game game = optionalGame.get();
        if(game.getPlayerStateBot() == null){
            UserAccount loser = optionalUserAccount.get();
            UserAccount winner = game.getUsers().get(0).equals(loser) ? game.getUsers().get(1) : game.getUsers().get(0);
            winner.getPlayerState().setWinner(true);
            playerStateRepository.save(winner.getPlayerState());
            terminateMatch(request.getGameId(), loser.getId(), winner.getId());
            System.out.println("Aufgeben2");
        }else{
            PlayerState botPS = optionalBotPS.get();
            UserAccount loser = game.getUsers().get(0);
            botPS.setWinner(true);
            playerStateRepository.save(botPS);
            terminateMatch(request.getGameId(), botPS.getId(), loser.getId());
            System.out.println("Aufgeben3");
        }
    }



    @Modifying
    @Transactional
    public void deleteUserGameData(List<Long> userIds,Long gameId) {
        Game game = gameRepository.findById(gameId).get();
        if(game.getPlayerStateBot() != null){
            game.setPlayerStateBot(null);
        }
        game.setCurrentTurn(null);
        if (game.getUsers().size() == 2) {
            UserAccount userAccount = userAccountRepository.findById(userIds.get(0)).get();
            userAccount.getPlayerState().setHandCards(null);
            userAccount.getPlayerState().setFieldCards(null);
            userAccount.getPlayerState().setCardsPlayed(null);
            userAccount.getPlayerState().setDeckClone(null);
            userAccount.getPlayerState().setDeck(null);
            PlayerState playerState = userAccount.getPlayerState();
            userAccount.setPlayerState(null);
            playerCardRepository.deleteByPlayerStateId(playerState.getId());
            playerStateRepository.save(playerState);
            playerStateRepository.delete(playerState);

            userAccount = userAccountRepository.findById(userIds.get(1)).get();
            userAccount.getPlayerState().setHandCards(null);
            userAccount.getPlayerState().setFieldCards(null);
            userAccount.getPlayerState().setCardsPlayed(null);
            userAccount.getPlayerState().setDeckClone(null);
            userAccount.getPlayerState().setDeck(null);
            playerState = userAccount.getPlayerState();
            userAccount.setPlayerState(null);
            playerCardRepository.deleteByPlayerStateId(playerState.getId());
            playerStateRepository.save(playerState);
            playerStateRepository.delete(playerState);

            gameRepository.deleteFromGameUsersByUserIds(userIds);
        }else{
            UserAccount userAccount = userAccountRepository.findById(userIds.get(0)).get();
            userAccount.getPlayerState().setHandCards(null);
            userAccount.getPlayerState().setFieldCards(null);
            userAccount.getPlayerState().setCardsPlayed(null);
            userAccount.getPlayerState().setDeckClone(null);
            userAccount.getPlayerState().setDeck(null);
            PlayerState playerState = userAccount.getPlayerState();
            userAccount.setPlayerState(null);
            playerCardRepository.deleteByPlayerStateId(playerState.getId());
            playerStateRepository.save(playerState);
            playerStateRepository.delete(playerState);

            PlayerState botPS = playerStateRepository.findById(userIds.get(1)).get();
            botPS.setHandCards(null);
            botPS.setFieldCards(null);
            botPS.setCardsPlayed(null);
            botPS.setDeckClone(null);
            botPS.setDeck(null);
            playerCardRepository.deleteByPlayerStateId(botPS.getId());
            playerStateRepository.save(botPS);
            playerStateRepository.deleteById(botPS.getId());
            deckRepository.deleteDeckCardsByDeckId(game.getBotDeckId());
            deckRepository.deleteById(game.getBotDeckId());

        }
        gameRepository.deleteById(gameId);
    }


    public Optional<List<Game>> getStreamedGames(){
        return gameRepository.findAllStreams();
    }



    public void streamGame(StreamGameRequest request){
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        if(optionalGame.isPresent()){
            Game game = optionalGame.get();
            game.setStreamed(true);
            gameRepository.save(game);
        }
    }

    public void watchStream(WatchStreamRequest request){
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserId());
        if(optionalGame.isPresent() && optionalUser.isPresent()){
            Game game = optionalGame.get();
            UserAccount user = optionalUser.get();
            game.getViewers().add(user);
            user.setWatching(game);
            userAccountRepository.save(user);
            gameRepository.save(game);

            List<UserAccount> users = game.getUsers();
            if(users.size() == 2) {
                messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/startwatch", List.of(game, users));
            }else{
                messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/startwatch", List.of(game, users, game.getPlayerStateBot()));
            }
        }
    }

    @Transactional
    @Modifying
    public void leaveStream(LeaveStreamRequest request){
        Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserId());
        if(optionalUser.isPresent()){
            UserAccount user = optionalUser.get();
            if(user.getWatching() != null){
                Game game = user.getWatching();
                game.getViewers().remove(user);
                user.setWatching(null);
                gameRepository.save(game);
                userAccountRepository.save(user);

                System.out.println("User " + request.getUserId() + " has left the stream of game " + game.getId());
            } else {
                System.out.println("User " + request.getUserId() + " is not watching any game.");
            }
        } else {
            System.out.println("User " + request.getUserId() + " not found.");
        }
    }


    public void createBotGame(CreateBotRequest request){
        UserAccount user = userAccountRepository.findById(request.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Deck deck = deckRepository.findById(request.getDeckId()).orElseThrow(() -> new IllegalArgumentException("Deck not found"));

        if (gameRepository.existsByUsersContaining(user)) {
            System.out.println("User with id: " + user.getId() + " already in a game");
            return;
        }

        leaderboardService.updateUserStatus(user.getId(), "im Duell");
        PlayerState playerState = new PlayerState();
        playerState.setDeck(deck);
        playerStateRepository.save(playerState);
        user.setPlayerState(playerState);
        userAccountRepository.save(user);

        List<Card> cardCopy = new ArrayList<>(deck.getCards());
        Deck deckCopy = new Deck();
        deckRepository.save(deckCopy);
        deckCopy.setCards(cardCopy);
        deckRepository.save(deckCopy);


        PlayerState playerStateBot = new PlayerState();
        playerStateBot.setDeck(deckCopy);
        playerStateRepository.save(playerStateBot);

        Game newGame = new Game();
        newGame.getUsers().add(user);
        newGame.setPlayerStateBot(playerStateBot);
        newGame.setBotDeckId(deckCopy.getId());
        newGame.setCurrentTurn(user);
        gameRepository.save(newGame);


        Collections.shuffle(deck.getCards()); // mischt das Deck
        List<Card> cards = deck.getCards();
        List<PlayerCard> playerCards = new ArrayList<>();
        // Klonen der Card in PlayerCard
        for(Card card : cards){
            PlayerCard playerCard = new PlayerCard();
            playerCard.setName(card.getName());
            playerCard.setAttackPoints(card.getAttackPoints());
            playerCard.setDefensePoints(card.getDefensePoints());
            playerCard.setDescription(card.getDescription());
            playerCard.setImage(card.getImage());
            playerCard.setRarity(card.getRarity());
            playerCard.setPlayerState(deck.getUser().getPlayerState()); // verbunden mit PlayerState des Users
            playerCards.add(playerCard);
            playerCardRepository.save(playerCard);
        }
        user.getPlayerState().setDeckClone(playerCards);
        playerStateRepository.save(user.getPlayerState());
        playerCards = new ArrayList<>();

        for(Card card : cards){
            PlayerCard playerCard = new PlayerCard();
            playerCard.setName(card.getName());
            playerCard.setAttackPoints(card.getAttackPoints());
            playerCard.setDefensePoints(card.getDefensePoints());
            playerCard.setDescription(card.getDescription());
            playerCard.setImage(card.getImage());
            playerCard.setRarity(card.getRarity());
            playerCard.setPlayerState(playerStateBot); // verbunden mit PlayerState des Bots
            playerCards.add(playerCard);
            playerCardRepository.save(playerCard);
        }
        playerStateBot.setDeckClone(playerCards);
        playerStateRepository.save(playerStateBot);
        gameRepository.save(newGame);



        // setzt initial 5 Karten aus dem gemischten Deck auf die Hand
        Iterator<PlayerCard> iterator = user.getPlayerState().getDeckClone().iterator();
        int count = 0;
        List<PlayerCard> cardsToRemove = new ArrayList<>();
        while (iterator.hasNext() && count < 6) {
            PlayerCard playerCard = iterator.next(); // Holt die nächste Karte aus dem Deck
            user.getPlayerState().getHandCards().add(playerCard); // Fügt die Karte der Hand des Spielers hinzu
            cardsToRemove.add(playerCard);
            count++; // Inkrementiert den Zähler für die Anzahl der gezogenen Karten
        }


        user.getPlayerState().getDeckClone().removeAll(cardsToRemove);
        playerStateRepository.save(user.getPlayerState());
        userAccountRepository.save(user);


        // setzt initial 5 Karten aus dem gemischten Deck auf die Hand des Bots
        iterator = playerStateBot.getDeckClone().iterator();
        count = 0;
        cardsToRemove = new ArrayList<>();
        while (iterator.hasNext() && count < 5) {
            PlayerCard playerCard = iterator.next(); // Hohlt die nächste Karte aus dem Deck
            playerStateBot.getHandCards().add(playerCard); // Fügt die Karte der Hand des Spielers hinzu
            cardsToRemove.add(playerCard);
            count++; // Inkrementiert den Zähler für die Anzahl der gezogenen Karten
        }
        playerStateBot.getDeckClone().removeAll(cardsToRemove);
        playerStateRepository.save(playerStateBot);

        if(request.getStreamed()){
            newGame.setStreamed(true);
        }
        newGame.setReady(true);
        newGame.resetTimer();
        gameRepository.save(newGame);



        messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/createBotDuel", Arrays.asList(newGame, user, playerStateBot));
        Notification notification = new Notification(user.getId(),0L,userAccountRepository.findById(user.getId()).get().getUsername(),"schon aktiviert");
        messagingTemplate.convertAndSendToUser(user.getId().toString(),"/queue/notifications",notification);


        if(newGame.getReady() && newGame.getStreamed() && gameRepository.findAllStreams().isPresent()) {
            Map<Long,List<String>> streamedGames = new HashMap<>();
            for (Game stream : gameRepository.findAllStreams().get()) {
                streamedGames.put(stream.getId(), List.of(stream.getUsers().get(0).getUsername(), "Bot"));
            }
            messagingTemplate.convertAndSend("/queue/streams", streamedGames);
        }

    }


    public void setGameTrue(SetGameTrueRequest request){
        pickDeck(request.getGameId());
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        if(optionalGame.isEmpty()){
            return;
        }
        Game game = optionalGame.get();
        if(game.getReady()){
            return;
        }
        game.setReady(true);
        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();
        for(UserAccount user : game.getUsers()){
            messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/selectDeck", List.of(game, users));
        }
    }


    public void pickDeck(Long gameId){
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        if(optionalGame.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        if(game.getReady()){
            return;
        }

        UserAccount user1 = game.getUsers().get(0);
        UserAccount user2 = game.getUsers().get(1);


        Deck deck1 = deckRepository.findByUserId(user1.getId()).get(0);
        Deck deck2 = deckRepository.findByUserId(user2.getId()).get(0);

        System.out.println("deck1 " + deck1.getId());
        System.out.println("deck2 " + deck2.getId());

        Collections.shuffle(deck1.getCards()); // mischt das Deck
        List<Card> cards1 = deck1.getCards();
        List<PlayerCard> playerCards1 = cards1.stream().map(card -> {
            PlayerCard playerCard = new PlayerCard();
            playerCard.setName(card.getName());
            playerCard.setAttackPoints(card.getAttackPoints());
            playerCard.setDefensePoints(card.getDefensePoints());
            playerCard.setDescription(card.getDescription());
            playerCard.setImage(card.getImage());
            playerCard.setRarity(card.getRarity());
            playerCard.setPlayerState(user1.getPlayerState());
            playerCardRepository.save(playerCard);
            return playerCard;
        }).collect(Collectors.toList());

        user1.getPlayerState().setDeck(deck1);
        user1.getPlayerState().setReady(true);
        user1.getPlayerState().setDeckClone(playerCards1);

        // legt dem Spieler initial 5 Karten auf die Hand
        List<PlayerCard> handCards1 = new ArrayList<>();
        if(game.getCurrentTurn().equals(user1)) {
            handCards1 = playerCards1.stream().limit(6).toList();
        }else{
            handCards1 = playerCards1.stream().limit(5).toList();
        }


        user1.getPlayerState().getHandCards().addAll(handCards1);
        user1.getPlayerState().getDeckClone().removeAll(handCards1);


        playerStateRepository.save(user1.getPlayerState());
        userAccountRepository.save(user1);


        Collections.shuffle(deck2.getCards()); // mischt das Deck
        List<Card> cards2 = deck2.getCards();
        List<PlayerCard> playerCards2 = cards2.stream().map(card -> {
            PlayerCard playerCard = new PlayerCard();
            playerCard.setName(card.getName());
            playerCard.setAttackPoints(card.getAttackPoints());
            playerCard.setDefensePoints(card.getDefensePoints());
            playerCard.setDescription(card.getDescription());
            playerCard.setImage(card.getImage());
            playerCard.setRarity(card.getRarity());
            playerCard.setPlayerState(user2.getPlayerState());
            playerCardRepository.save(playerCard);
            return playerCard;
        }).collect(Collectors.toList());

        user2.getPlayerState().setDeck(deck2);
        user2.getPlayerState().setReady(true);
        user2.getPlayerState().setDeckClone(playerCards2);

        // legt dem Spieler initial 5 Karten auf die Hand
        List<PlayerCard> handCards2 = new ArrayList<>();
        if(game.getCurrentTurn().equals(user2)) {
            handCards2 = playerCards2.stream().limit(6).toList();
        }else{
            handCards2 = playerCards2.stream().limit(5).toList();
        }


        user2.getPlayerState().getHandCards().addAll(handCards2);
        user2.getPlayerState().getDeckClone().removeAll(handCards2);


        playerStateRepository.save(user2.getPlayerState());
        userAccountRepository.save(user2);
        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();

        for (UserAccount player : users) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/selectDeck", Arrays.asList(game, users));
        }


        if(game.getStreamed()) {
            Map<Long, List<String>> streamedGames = gameRepository.findAllStreams().get().stream()
                    .collect(Collectors.toMap(
                            Game::getId,
                            stream -> stream.getUsers().stream()
                                    .map(UserAccount::getUsername)
                                    .collect(Collectors.toList())
                    ));
            messagingTemplate.convertAndSend("/queue/streams", streamedGames);
        }

    }

}
 