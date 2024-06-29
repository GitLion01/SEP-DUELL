package com.example.demo.game;
import com.example.demo.cards.Card;
import com.example.demo.cards.Rarity;
import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.duellHerausforderung.Notification;
import com.example.demo.game.requests.*;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;


@Transactional
@Service
public class GameService {
    private final GameRepository gameRepository;
    private final DeckRepository deckRepository;
    private final UserAccountRepository userAccountRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerStateRepository playerStateRepository;
    private final PlayerCardRepository playerCardRepository;

    @Autowired
    public GameService(GameRepository gameRepository,
                       DeckRepository deckRepository,
                       UserAccountRepository userAccountRepository,
                       SimpMessagingTemplate messagingTemplate,
                       PlayerStateRepository playerStateRepository,
                       PlayerCardRepository playerCardRepository)  {
        this.gameRepository = gameRepository;
        this.deckRepository = deckRepository;
        this.userAccountRepository = userAccountRepository;
        this.messagingTemplate = messagingTemplate;
        this.playerStateRepository = playerStateRepository;
        this.playerCardRepository = playerCardRepository;
    }

    //TODO: DIESEN KOMMENTAR NICHT LÖSCHEN!!!!!
    /*@Scheduled(fixedRate = 1000)
    public void updateTimers(){
        List<Game> games = gameRepository.findAll();
        for (Game game : games) {
            game.decrementTimer();
            if(game.getReady() && game.getRemaingTime() <= 0){
                handleTimerExpiration(game);
            }
            sendTimerUpdate(game);
        }
    }

    private void handleTimerExpiration(Game game) {
        try {
            if(game!=null) {
                UserAccount currentTurnPlayer = game.getUsers().get(game.getCurrentTurn());
                currentTurnPlayer.getPlayerState().setLifePoints(-1);
                UserAccount otherPlayer = game.getUsers().get(0).equals(currentTurnPlayer) ? game.getUsers().get(1) : game.getUsers().get(0);
                currentTurnPlayer.getPlayerState().setLifePoints(-1);
                otherPlayer.getPlayerState().setWinner(true);
                terminateMatch(game.getId(), currentTurnPlayer.getId(), otherPlayer.getId());
            }
        }
        catch (Exception e){
            System.out.println(e+"------------------");
        }
    }

    private void sendTimerUpdate(Game game) {
        int remainingTime = game.getRemaingTime();
        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/timer", remainingTime);
        }
    }*/

    public void createGame(CreateGameRequest request) {
        System.out.println("Creating game for users A:" + request.getUserA() + " and B:" + request.getUserB());
        UserAccount userA = userAccountRepository.findById(request.getUserA())
                .orElseThrow(() -> new IllegalArgumentException("User A not found"));
        UserAccount userB = userAccountRepository.findByUsername(request.getUserB())
                .orElseThrow(() -> new IllegalArgumentException("User B not found"));
        // Check if userA is already associated with a game
        if (gameRepository.existsByUsersContaining(userA)) {
            System.out.println("User with id: " + userA.getId() + " already in a game");
            return;
        }
        // Check if userB is already associated with a game
        if (gameRepository.existsByUsersContaining(userB)) {
            System.out.println("User with id: " + userA.getId() + " already in a game");
            return;
        }
        PlayerState playerStateA = new PlayerState();
        playerStateRepository.save(playerStateA);
        userA.setPlayerState(playerStateA);
        userAccountRepository.save(userA);
        PlayerState playerStateB = new PlayerState();
        playerStateRepository.save(playerStateB);
        userB.setPlayerState(playerStateB);
        userAccountRepository.save(userB);
        System.out.println("Vor Game");
        Game newGame = new Game();
        System.out.println("Nach Game");
        newGame.getUsers().add(userA);
        newGame.getUsers().add(userB);
        System.out.println("vor speichern");
        gameRepository.save(newGame);
        System.out.println("Game gespeichert");
        List<UserAccount> users = newGame.getUsers();

        for(UserAccount user : newGame.getUsers()) {
            messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/create", Arrays.asList(newGame, users));
            System.out.println("gesendet-----------------");
            Notification notification = new Notification(user.getId(),0L,userAccountRepository.findById(user.getId()).get().getUsername(),"schon aktiviert");
            messagingTemplate.convertAndSendToUser(user.getId().toString(),"/queue/notifications",notification);
        }
    }

    @Transactional
    public void selectDeck(DeckSelectionRequest request) {
        System.out.println("SERVICE ERREICHT");
        System.out.println("Deck ID: " + request.getDeckId());
        System.out.println("User ID: " + request.getUserId());
        System.out.println("Game ID: " + request.getGameId());
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<Deck> optionalDeck = deckRepository.findByDeckIdAndUserId(request.getDeckId(), request.getUserId());
        if(optionalGame.isEmpty() || optionalDeck.isEmpty()) {
            return;
        }
        System.out.println("Game und Deck vorhanden");
        Game game = optionalGame.get();
        Deck deck = optionalDeck.get();
        List<Card> cards = deck.getCards();
        Collections.shuffle(deck.getCards()); // mischt das Deck
        List<PlayerCard> playerCards = new ArrayList<>();
        // Klonen der Card ind PlayerCard
        for(Card card : cards){
            PlayerCard playerCard = new PlayerCard();
            playerCard.setName(card.getName());
            playerCard.setAttackPoints(card.getAttackPoints());
            playerCard.setDefensePoints(card.getDefensePoints());
            playerCard.setDescription(card.getDescription());
            playerCard.setImage(card.getImage());
            playerCard.setRarity(card.getRarity());
            playerCard.setPlayerState(deck.getUser().getPlayerState());
            playerCards.add(playerCard);
            playerCardRepository.save(playerCard);
        }

        System.out.println("VOR DECK SETZEN");
        UserAccount user = deck.getUser();
        user.getPlayerState().setDeck(deck);
        user.getPlayerState().setReady(true);
        //Fortan wird mit diesem Deck Klon gearbeitet
        user.getPlayerState().setDeckClone(playerCards);
        playerStateRepository.save(user.getPlayerState());
        System.out.println("NACH DECK SETZEN");
        // setzt initial 5 Karten aus dem gemischten Deck auf die Hand
        Iterator<PlayerCard> iterator = user.getPlayerState().getDeckClone().iterator();
        int count = 0;
        List<PlayerCard> cardsToRemove = new ArrayList<>();
        while (iterator.hasNext() && count < 5) {
            PlayerCard playerCard = iterator.next(); // Hohlt die nächste Karte aus dem Deck
            user.getPlayerState().getHandCards().add(playerCard); // Fügt die Karte der Hand des Spielers hinzu
            cardsToRemove.add(playerCard);
            count++; // Inkrementiert den Zähler für die Anzahl der gezogenen Karten
        }


        user.getPlayerState().getDeckClone().removeAll(cardsToRemove);
        System.out.println("VOR SPEICHERN DES USERS");
        playerStateRepository.save(user.getPlayerState());
        userAccountRepository.save(user);
        System.out.println("NACH SPEICHERN DES USERS");
        // überprüft ob in beiden PlayerStates der Spieler ready auf true gesetzt ist
        boolean allPlayersReady = true;
        for (UserAccount userAccount : userAccountRepository.findAll()) {
            if(game.getUsers().contains(userAccount)) {
                if (!userAccount.getPlayerState().getReady()) {
                    allPlayersReady = false;
                    break;
                }
            }
        }
        if (allPlayersReady) {
            game.resetTimer();
            game.setReady(true);
        }
        System.out.println("ALLE READY");

        playerStateRepository.save(user.getPlayerState());

        System.out.println("READY? " + user.getPlayerState().getReady());

        gameRepository.save(game);
        List<UserAccount> users = game.getUsers();
        for(UserAccount player : game.getUsers()) {
            System.out.println("Player: " + player.getId());
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/selectDeck", Arrays.asList(game, users));
        }

        if(game.getReady() && game.getStreamed()) {
            Map<Long, List<String>> streamedGames = new HashMap<>();
            for (Game stream : gameRepository.findAllStreams().get()) {
                streamedGames.put(stream.getId(), List.of(stream.getUsers().get(0).getUsername(), stream.getUsers().get(1).getUsername()));
            }
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
        if(!game.getUsers().get(game.getCurrentTurn()).equals(userAccount)){// prüft ob der User am zug ist
            return;
        }
        Deck deck = userAccount.getPlayerState().getDeck();
        List<PlayerCard> handCards = userAccount.getPlayerState().getHandCards();

        // ersetzt den unteren Kommentar
        if(deck.getUser().getPlayerState().getDeckClone().isEmpty()) {
            return;
        }else{
            handCards.add(deck.getUser().getPlayerState().getDeckClone().remove(0));
        }


        playerStateRepository.save(userAccount.getPlayerState());
        gameRepository.save(game);
        List<UserAccount> users = game.getUsers();
        for(UserAccount player : game.getUsers()) {
            System.out.println(" BEVOR Player: " + player.getId());
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            System.out.println(" DANACH Player: " + player.getId());
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
        if(!game.getUsers().get(game.getCurrentTurn()).equals(userAccount) ||
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
        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
        }
        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users));
            }
        }
    }

    public void endTurn(EndTurnRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameID());
        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(request.getUserID());
        if(optionalGame.isEmpty() || optionalUserAccount.isEmpty()) {
            return;
        }
        Game game = optionalGame.get();
        UserAccount userAccount = optionalUserAccount.get();
        if(!game.getUsers().get(game.getCurrentTurn()).equals(userAccount)){
            return;
        }
        UserAccount currentTurn = game.getUsers().get(game.getCurrentTurn());
        UserAccount notTurn = game.getUsers().get(0).equals(userAccount) ? game.getUsers().get(1) : userAccount;
        game.setCurrentTurn(game.getUsers().get(0).equals(userAccount) ? 1 : 0);
        game.resetTimer();
        if(game.getFirstRound()){
            game.setFirstRound(false);
        }
        List<PlayerCard> cards = userAccount.getPlayerState().getFieldCards();
        for(PlayerCard card : cards){
            card.setHasAttacked(false);
            playerCardRepository.save(card);
        }
        gameRepository.save(game);
        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();
        for(UserAccount player : game.getUsers()) {
            System.out.println(" BEVOR Player: " + player.getId());
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            System.out.println(" DANACH Player: " + player.getId());
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users));
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
            return;
        }
        Game game = optionalGame.get();
        UserAccount attacker = optionalAttacker.get();
        UserAccount defender = optionalDefender.get();
        PlayerCard attackerCard = optionalAttackerCard.get();
        PlayerCard target = optionalTarget.get();
        if(attackerCard.getHasAttacked()){
            return;
        }
        if(!game.getUsers().get(game.getCurrentTurn()).equals(attacker) ||
                defender.getPlayerState().getFieldCards().isEmpty() ||
                attacker.getPlayerState().getFieldCards().contains(target)){
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
        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users));
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
        if(!game.getUsers().get(game.getCurrentTurn()).equals(attacker) || !defender.getPlayerState().getFieldCards().isEmpty() || game.getFirstRound()){
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
        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
        }
        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users));
            }
        }
        if(remainingLifePoints < 0){
            terminateMatch(request.getGameId(), attacker.getId(), defender.getId());
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
        if(!game.getUsers().get(game.getCurrentTurn()).equals(user) || request.getCardIds().size() != 2) {
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
        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users));
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
        if(!game.getUsers().get(game.getCurrentTurn()).equals(user) || request.getCardIds().size() != 3) {
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
        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users));
            }
        }
    }

    public void terminateMatch(Long gameId, Long userA, Long userB) {
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        Optional<UserAccount> optionalUserA = userAccountRepository.findById(userA);
        Optional<UserAccount> optionalUserB = userAccountRepository.findById(userB);
        if (optionalGame.isEmpty() || optionalUserA.isEmpty() || optionalUserB.isEmpty()) {
            return;
        }
        Game game = optionalGame.get();
        UserAccount user1 = optionalUserA.get();
        UserAccount user2 = optionalUserB.get();

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

        Integer sepCoins = 100;
        Integer leaderBoardPointsWinner;
        Integer leaderBoardPointsLoser;
        Integer damageWinner;
        Integer damageLoser;
        List<Integer> cardsPlayedA = Arrays.asList(normalsA.size(), raresA.size(), legendariesA.size());
        List<Integer> cardsPlayedB = Arrays.asList(normalsB.size(), raresB.size(), legendariesB.size());
        List<Integer> sacrificedA = Arrays.asList(sacrificedNormalsA.size(), sacrificedRaresA.size(), sacrificedLegendariesA.size());
        List<Integer> sacrificedB = Arrays.asList(sacrificedNormalsB.size(), sacrificedRaresB.size(), sacrificedLegendariesB.size());

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
        } else {
            user2.setSepCoins(user2.getSepCoins() + 100);
            user2.setLeaderboardPoints(lbPoints2 + Math.max(50, lbPoints1 - lbPoints2));
            user1.setLeaderboardPoints(lbPoints1 - Math.max(50, (lbPoints1 - lbPoints2) / 2));
            leaderBoardPointsWinner = Math.max(50, lbPoints1 - lbPoints2);
            leaderBoardPointsLoser = -1 * (Math.max(50, (lbPoints1 - lbPoints2) / 2));
            damageWinner = user2.getPlayerState().getDamage();
            damageLoser = user1.getPlayerState().getDamage();
        }

        playerStateRepository.save(user1.getPlayerState());
        playerStateRepository.save(user2.getPlayerState());
        userAccountRepository.save(user1);
        userAccountRepository.save(user2);
        gameRepository.save(game);
        List<UserAccount> users = game.getUsers();
        List<UserAccount> viewers = game.getViewers();
        for (UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users, sepCoins, leaderBoardPointsWinner, leaderBoardPointsLoser,damageWinner, damageLoser, cardsPlayedA, cardsPlayedB, sacrificedA, sacrificedB));
        }

        if(!viewers.isEmpty()) {
            for(UserAccount viewer : viewers) {
                messagingTemplate.convertAndSendToUser(viewer.getId().toString(), "/queue/watch", Arrays.asList(game, users, sepCoins, leaderBoardPointsWinner, leaderBoardPointsLoser,damageWinner, damageLoser, cardsPlayedA, cardsPlayedB, sacrificedA, sacrificedB));
            }
        }

        List<Long> userIds=Arrays.asList(game.getUsers().get(0).getId(), game.getUsers().get(1).getId());
        for(UserAccount viewer : game.getViewers()) {
            if(viewer != null) {
                LeaveStreamRequest request = new LeaveStreamRequest();
                request.setUserId(viewer.getId());
                leaveStream(request);
            }
        }
        deleteUserGameData(userIds, game.getId());

        Map<Long, List<String>> streamedGames = new HashMap<>();
        for(Game stream : gameRepository.findAllStreams().get()){
            streamedGames.put(stream.getId(), List.of(stream.getUsers().get(0).getUsername(), stream.getUsers().get(1).getUsername()));
        }
        messagingTemplate.convertAndSend("/queue/streams", streamedGames);
    }



    @Modifying
    @Transactional
    public void deleteUserGameData(List<Long> userIds,Long gameId) {

        userAccountRepository.updatePlayerStateToNullByUserIds(userIds);
        UserAccount userAccount=userAccountRepository.findById(userIds.get(0)).get();
        userAccount.getPlayerState().setHandCards(null);
        userAccount.getPlayerState().setFieldCards(null);
        userAccount.getPlayerState().setCardsPlayed(null);
        userAccount.getPlayerState().setDeckClone(null);
        userAccount.getPlayerState().setDeck(null);
        playerCardRepository.deleteByPlayerStateId(userAccount.getPlayerState().getId());
        playerStateRepository.save(userAccount.getPlayerState());
        playerStateRepository.delete(userAccount.getPlayerState());

        userAccount=userAccountRepository.findById(userIds.get(1)).get();
        userAccount.getPlayerState().setHandCards(null);
        userAccount.getPlayerState().setFieldCards(null);
        userAccount.getPlayerState().setCardsPlayed(null);
        userAccount.getPlayerState().setDeckClone(null);
        userAccount.getPlayerState().setDeck(null);
        playerCardRepository.deleteByPlayerStateId(userAccount.getPlayerState().getId());
        playerStateRepository.save(userAccount.getPlayerState());
        playerStateRepository.delete(userAccount.getPlayerState());

        gameRepository.deleteFromGameUsersByUserIds(userIds);
        gameRepository.deleteById(gameId);
    }



    public Optional<List<Game>> getStreamedGames(){
        return gameRepository.findAllStreams();
    }



    public void streamGame(Long gameId){
        Optional<Game> optionalGame = gameRepository.findById(gameId);
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
            messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/startwatch", List.of(game, users));
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
            }
        }
    }

}
 