package com.example.demo.game;
import com.example.demo.cards.Card;
import com.example.demo.cards.CardInstance;
import com.example.demo.cards.Rarity;
import com.example.demo.decks.Deck;
import com.example.demo.decks.DeckRepository;
import com.example.demo.game.requests.*;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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



    @Autowired
    public GameService(GameRepository gameRepository,
                       DeckRepository deckRepository,
                       UserAccountRepository userAccountRepository,
                       SimpMessagingTemplate messagingTemplate,
                       PlayerStateRepository playerStateRepository) {
        this.gameRepository = gameRepository;
        this.deckRepository = deckRepository;
        this.userAccountRepository = userAccountRepository;
        this.messagingTemplate = messagingTemplate;
        this.playerStateRepository = playerStateRepository;
    }



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
        }

    }






    private Long index; // damit in jeder methode der index => id aktuelle bleibt
    private int deckIndex = 0;

    public void selectDeck(DeckSelectionRequest request) {
        System.out.println("SERVICE ERREICHT");
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
        }



        System.out.println("VOR DECK SETZEN");
        UserAccount user = deck.getUser();
        user.getPlayerState().setDeck(deck);
        user.getPlayerState().setReady(true);
        //Fortan wird mit diesem Deck Klon gearbeitet
        user.getPlayerState().setDeckClone(playerCards);

        System.out.println("NACH DECK SETZEN");

        // setzt initial 5 Karten aus dem gemischten Deck auf die Hand
        Iterator<PlayerCard> iterator = user.getPlayerState().getDeckClone().iterator();
        int count = 0;
        while (iterator.hasNext() && count < 5) {
            PlayerCard playerCard = iterator.next(); // Hohlt die nächste Karte aus dem Deck
            user.getPlayerState().getHandCards().add(playerCard); // Fügt die Karte der Hand des Spielers hinzu
            user.getPlayerState().getDeckClone().remove(playerCard);
            count++; // Inkrementiert den Zähler für die Anzahl der gezogenen Karten
        }

        /*Iterator<PlayerCard> iterator = playerCards.iterator();
        int count = 0;
        while (iterator.hasNext() && count < 5) {
            PlayerCard card = iterator.next(); // Hohlt die nächste Karte aus dem Deck
            user.getPlayerState().getHandCards().add(card); // Fügt die Karte der Hand des Spielers hinzu
            deckIndex++;
            count++; // Inkrementiert den Zähler für die Anzahl der gezogenen Karten
        }*/

        System.out.println("VOR SPEICHERN DES USERS");
        playerStateRepository.save(user.getPlayerState());
        userAccountRepository.save(user);
        System.out.println("NACH SPEICHERN DES USERS");

        // überprüft ob in beiden PlayerStates der Spieler ready auf true gesetzt ist
        boolean allPlayersReady = true;
        for (UserAccount userAccount : game.getUsers()) {
            if (!userAccount.getPlayerState().getReady()) {
                allPlayersReady = false;
                break;
            }
        }

        if (allPlayersReady) {
            game.setReady(true);
        }

        System.out.println("ALLE READY");

        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            System.out.println("Player: " + player.getId());
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/selectDeck", game);
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
        if(!game.getUsers().get(game.getCurrentTurn()).equals(userAccount) || deckIndex == userAccount.getPlayerState().getDeck().getCards().size()){// prüft ob der User am zug ist
            return;
        }
        Deck deck = userAccount.getPlayerState().getDeck();
        List<PlayerCard> handCards = userAccount.getPlayerState().getHandCards();


        // ersetzt den unteren Kommentar
        handCards.add(deck.getUser().getPlayerState().getDeckClone().get(0));
        deck.getUser().getPlayerState().getDeckClone().remove(0);

        /*Card card = deck.getCards().get(deckIndex);
        deckIndex++;
        PlayerCard playerCard = new PlayerCard();
        playerCard.setCard(card);

        handCards.add(cardInstance);
        deck.getCards().remove(deck.getCards().get(0));*/

        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();


        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            System.out.println("Karte wurde gezogen" + player.getId());
        }

    }

    public void placeCard(PlaceCardRequest request){

        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUserAccount = userAccountRepository.findById(request.getUserId());

        if(optionalGame.isEmpty() || optionalUserAccount.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        UserAccount userAccount = optionalUserAccount.get();
        if(!game.getUsers().get(game.getCurrentTurn()).equals(userAccount) ||
                userAccount.getPlayerState().getFieldCards().size() > 5 ||
                userAccount.getPlayerState().getHandCards().get(request.getCardIndex()).getRarity() != Rarity.NORMAL) {// prüft ob der User am zug ist
            return;
        }

        userAccount.getPlayerState().getFieldCards().add(userAccount.getPlayerState().getHandCards().get(request.getCardIndex())); // Fügt Karte aus Hand dem Feld hinzu
        PlayerCard removed = userAccount.getPlayerState().getHandCards().remove(request.getCardIndex()); // Löscht Karte aus Hand
        userAccount.getPlayerState().getCardsPlayed().add(removed); // Fügt die Karte den gespielten Karten hinzu


        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();


        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
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

        game.setCurrentTurn(game.getUsers().get(0).equals(userAccount) ? 1 : 0);

        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();


        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            System.out.println("Zug beendet" + player.getId());
        }

    }

    public void attackCard(AttackCardRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalAttacker = userAccountRepository.findById(request.getUserIdAttacker());
        Optional<UserAccount> optionalDefender = userAccountRepository.findById(request.getUserIdDefender());

        if(optionalGame.isEmpty() || optionalAttacker.isEmpty() || optionalDefender.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        UserAccount attacker = optionalAttacker.get();
        UserAccount defender = optionalDefender.get();
        if(!game.getUsers().get(game.getCurrentTurn()).equals(attacker) || defender.getPlayerState().getFieldCards().isEmpty()){
            return;
        }
        PlayerCard attackerCard = attacker.getPlayerState().getHandCards().get(request.getAttackerIndex());
        PlayerCard target = defender.getPlayerState().getHandCards().get(request.getTargetIndex());

        if(attackerCard.getAttackPoints() > target.getDefensePoints()){
            defender.getPlayerState().getFieldCards().remove(request.getTargetIndex()); // entfernt die Karte vom Feld des Gegners
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getDefensePoints()); // erhöht den Damage Counter des Angreifers
        }else{
            target.setDefensePoints(target.getDefensePoints() - attackerCard.getAttackPoints()); // zieht Angriffspunkte von Verteidigungspunkte ab
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getAttackPoints()); // erhöht den Damage Counter des Angreifers
        }


        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
        }


    }

    public void attackUser(AttackUserRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalAttacker = userAccountRepository.findById(request.getAttackerId());
        Optional<UserAccount> optionalDefender = userAccountRepository.findById(request.getDefenderId());

        if(optionalGame.isEmpty() || optionalAttacker.isEmpty() || optionalDefender.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        UserAccount attacker = optionalAttacker.get();
        UserAccount defender = optionalDefender.get();
        if(!game.getUsers().get(game.getCurrentTurn()).equals(attacker) || !defender.getPlayerState().getFieldCards().isEmpty()){
            return;
        }
        PlayerCard attackerCard = attacker.getPlayerState().getHandCards().get(request.getAttackerCardIndex());

        if(attackerCard.getAttackPoints() > defender.getPlayerState().getLifePoints()){
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getDefensePoints()); // erhöht den Damage Counter des Angreifers
            attacker.getPlayerState().setWinner(true);
            terminateMatch(request.getGameId(), attacker.getId(), defender.getId());
        }else{
            defender.getPlayerState().setLifePoints((defender.getPlayerState().getLifePoints() - attackerCard.getAttackPoints())); // zieht Angriffspunkte von Lebenspunkte ab
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getAttackPoints()); // erhöht den Damage Counter des Angreifers
        }


        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
        }



    }

    public void swapForRare(RareSwapRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserId());

        if(optionalGame.isEmpty() || optionalUser.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        UserAccount user = optionalUser.get();

        if(!game.getUsers().get(0).equals(user) || request.getNormalCardsIndex().size() != 2) {
            return;
        }

        List<PlayerCard> hand = user.getPlayerState().getHandCards();
        List<PlayerCard> field = user.getPlayerState().getFieldCards();
        PlayerCard normal1 = field.get(request.getNormalCardsIndex().get(0));
        PlayerCard normal2 = field.get(request.getNormalCardsIndex().get(1));
        PlayerCard rare = hand.get(request.getRareCardIndex());

        if(rare.getRarity() == Rarity.RARE){
            return;
        }

        user.getPlayerState().getFieldCards().remove(normal1);
        user.getPlayerState().getFieldCards().remove(normal2);
        user.getPlayerState().getFieldCards().add(rare);
        user.getPlayerState().getHandCards().remove(rare);

        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
        }


    }

    public void swapForLegendary(LegendarySwapRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<UserAccount> optionalUser = userAccountRepository.findById(request.getUserId());

        if(optionalGame.isEmpty() || optionalUser.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        UserAccount user = optionalUser.get();
        if(!game.getUsers().get(0).equals(user) || request.getNormalCardsIndex().size() != 3) {
            return;
        }
        List<PlayerCard> hand = user.getPlayerState().getHandCards();
        List<PlayerCard> field = user.getPlayerState().getFieldCards();
        PlayerCard card1 = field.get(request.getNormalCardsIndex().get(0));
        PlayerCard card2 = field.get(request.getNormalCardsIndex().get(1));
        PlayerCard card3 = field.get(request.getNormalCardsIndex().get(2));
        PlayerCard legendary = hand.get(request.getLegendaryCardIndex());

        if(legendary.getRarity() == Rarity.LEGENDARY){
            return;
        }

        user.getPlayerState().getFieldCards().remove(card1);
        user.getPlayerState().getFieldCards().remove(card2);
        user.getPlayerState().getFieldCards().remove(card3);
        user.getPlayerState().getFieldCards().add(legendary);
        user.getPlayerState().getHandCards().remove(legendary);

        gameRepository.save(game);

        List<UserAccount> users = game.getUsers();

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
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

            // Flag setzen damit isTerminated Variable auf true gesetzt wird
            if(game.getIsTerminated()){
                return;
            }
            game.setIsTerminated(true);

            if (user1.getPlayerState().getWinner()) {
                user1.setSepCoins(user1.getSepCoins() + 100);
                user1.setLeaderboardPoints(user1.getLeaderboardPoints() + Math.max(50, user2.getLeaderboardPoints() - user1.getLeaderboardPoints()));
                user2.setLeaderboardPoints(user2.getLeaderboardPoints() - Math.max(50, (user2.getLeaderboardPoints() - user1.getLeaderboardPoints()) / 2));
            } else {
                user2.setSepCoins(user2.getSepCoins() + 100);
                user2.setLeaderboardPoints(user2.getLeaderboardPoints() + Math.max(50, user1.getLeaderboardPoints() - user2.getLeaderboardPoints()));
                user1.setLeaderboardPoints(user1.getLeaderboardPoints() - Math.max(50, (user1.getLeaderboardPoints() - user2.getLeaderboardPoints()) / 2));
            }
            gameRepository.save(game);

        List<UserAccount> users = game.getUsers();

            for (UserAccount player : game.getUsers()) {
                messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", Arrays.asList(game, users));
            }

            gameRepository.delete(game);

            //TODO: Alle Daten zurücksetzten (Deck, Cards, etc)

    }






}
