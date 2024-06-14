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


            for(UserAccount user : newGame.getUsers()) {
                messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/create", newGame);
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
        List<CardInstance> cardInstances = new ArrayList<>();
        for(Card card : cards){
            CardInstance cardInstance = new CardInstance();
            cardInstance.setId(index);
            cardInstance.setCard(card);
            cardInstances.add(cardInstance);
            index++;
        }


        System.out.println("VOR DECK SETZEN");
        UserAccount user = deck.getUser();
        user.getPlayerState().setDeck(deck);
        user.getPlayerState().setReady(true);

        System.out.println("NACH DECK SETZEN");

        // setzt initial 5 Karten aus dem gemischten Deck auf die Hand
        Iterator<CardInstance> iterator = cardInstances.iterator();
        int count = 0;
        while (iterator.hasNext() && count < 5) {
            CardInstance card = iterator.next(); // Hohlt die nächste Karte aus dem Deck
            user.getPlayerState().getHandCards().add(card); // Fügt die Karte der Hand des Spielers hinzu
            deckIndex++;
            count++; // Inkrementiert den Zähler für die Anzahl der gezogenen Karten
        }

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
        List<CardInstance> handCards = userAccount.getPlayerState().getHandCards();

        Card card = deck.getCards().get(deckIndex);
        deckIndex++;
        CardInstance cardInstance = new CardInstance();
        cardInstance.setId(index);
        cardInstance.setCard(card);

        handCards.add(cardInstance);
        deck.getCards().remove(deck.getCards().get(0));

        game.setCurrentTurn(game.getUsers().get(0).equals(userAccount) ? 1 : 0);

        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", game);
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
        if(!game.getUsers().get(game.getCurrentTurn()).equals(userAccount) || userAccount.getPlayerState().getFieldCards().size() > 5){// prüft ob der User am zug ist
            return;
        }

        userAccount.getPlayerState().getFieldCards().add(userAccount.getPlayerState().getHandCards().get(request.getCardIndex())); // Fügt Karte aus Hand dem Feld hinzu
        CardInstance removed = userAccount.getPlayerState().getHandCards().remove(request.getCardIndex()); // Löscht Karte aus Hand
        userAccount.getPlayerState().getCardsPlayed().add(removed); // Fügt die Karte den gespielten Karten hinzu

        game.setCurrentTurn(game.getUsers().get(0).equals(userAccount) ? 1 : 0);

        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", game);
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

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", game);
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
        CardInstance attackerCard = attacker.getPlayerState().getHandCards().get(request.getAttackerIndex());
        CardInstance target = defender.getPlayerState().getHandCards().get(request.getTargetIndex());

        if(attackerCard.getCard().getAttackPoints() > target.getCard().getDefensePoints()){
            defender.getPlayerState().getFieldCards().remove(request.getTargetIndex()); // entfernt die Karte vom Feld des Gegners
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getCard().getDefensePoints()); // erhöht den Damage Counter des Angreifers
        }else{
            target.getCard().setDefensePoints(target.getCard().getDefensePoints() - attackerCard.getCard().getAttackPoints()); // zieht Angriffspunkte von Verteidigungspunkte ab
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getCard().getAttackPoints()); // erhöht den Damage Counter des Angreifers
        }

        game.setCurrentTurn(game.getUsers().get(0).equals(attacker) ? 1 : 0);

        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", game);
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
        CardInstance attackerCard = attacker.getPlayerState().getHandCards().get(request.getAttackerCardIndex());

        if(attackerCard.getCard().getAttackPoints() > defender.getPlayerState().getLifePoints()){
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getCard().getDefensePoints()); // erhöht den Damage Counter des Angreifers
            attacker.getPlayerState().setWinner(true);
            terminateMatch(request.getGameId(), attacker.getId(), defender.getId());
        }else{
            defender.getPlayerState().setLifePoints((defender.getPlayerState().getLifePoints() - attackerCard.getCard().getAttackPoints())); // zieht Angriffspunkte von Lebenspunkte ab
            attacker.getPlayerState().setDamage(attacker.getPlayerState().getDamage() + attackerCard.getCard().getAttackPoints()); // erhöht den Damage Counter des Angreifers
        }

        game.setCurrentTurn(game.getUsers().get(0).equals(attacker) ? 1 : 0);

        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", game);
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

        List<CardInstance> hand = user.getPlayerState().getHandCards();
        List<CardInstance> field = user.getPlayerState().getFieldCards();
        CardInstance normal1 = field.get(request.getNormalCardsIndex().get(0));
        CardInstance normal2 = field.get(request.getNormalCardsIndex().get(1));
        CardInstance rare = hand.get(request.getRareCardIndex());

        if(rare.getCard().getRarity() == Rarity.RARE){
            return;
        }

        user.getPlayerState().getFieldCards().remove(normal1);
        user.getPlayerState().getFieldCards().remove(normal2);
        user.getPlayerState().getFieldCards().add(rare);
        user.getPlayerState().getHandCards().remove(rare);

        game.setCurrentTurn(game.getUsers().get(0).equals(user) ? 1 : 0);

        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", game);
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
        List<CardInstance> hand = user.getPlayerState().getHandCards();
        List<CardInstance> field = user.getPlayerState().getFieldCards();
        CardInstance card1 = field.get(request.getNormalCardsIndex().get(0));
        CardInstance card2 = field.get(request.getNormalCardsIndex().get(1));
        CardInstance card3 = field.get(request.getNormalCardsIndex().get(2));
        CardInstance legendary = hand.get(request.getLegendaryCardIndex());

        if(legendary.getCard().getRarity() == Rarity.LEGENDARY){
            return;
        }

        user.getPlayerState().getFieldCards().remove(card1);
        user.getPlayerState().getFieldCards().remove(card2);
        user.getPlayerState().getFieldCards().remove(card3);
        user.getPlayerState().getFieldCards().add(legendary);
        user.getPlayerState().getHandCards().remove(legendary);

        game.setCurrentTurn(game.getUsers().get(0).equals(user) ? 1 : 0);

        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", game);
        }


    }


    public void terminateMatch(Long gameId, Long userA, Long userB) {
        Optional<Game> optionalGame = gameRepository.findById(gameId);
        Optional<UserAccount> optionalUserA = userAccountRepository.findById(userA);
        Optional<UserAccount> optionalUserB = userAccountRepository.findById(userB);

        if(optionalGame.isEmpty() || optionalUserA.isEmpty() || optionalUserB.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        UserAccount user1 = optionalUserA.get();
        UserAccount user2 = optionalUserB.get();

        if(user1.getPlayerState().getWinner()){
            user1.setSepCoins(user1.getSepCoins() + 100);
            user1.setLeaderboardPoints(user1.getLeaderboardPoints() + Math.max(50, user2.getLeaderboardPoints() - user1.getLeaderboardPoints()));
            user2.setLeaderboardPoints(user2.getLeaderboardPoints() - Math.max(50, (user2.getLeaderboardPoints() - user1.getLeaderboardPoints()) / 2 ));
        }else{
            user2.setSepCoins(user2.getSepCoins() + 100);
            user2.setLeaderboardPoints(user2.getLeaderboardPoints() + Math.max(50, user1.getLeaderboardPoints() - user2.getLeaderboardPoints()));
            user1.setLeaderboardPoints(user1.getLeaderboardPoints() - Math.max(50, (user1.getLeaderboardPoints() - user2.getLeaderboardPoints()) / 2 ));
        }
        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/game", game);
        }

        gameRepository.delete(game);

        //TODO: Alle Daten zurücksetzten (Deck, Cards, etc)

    }


}
