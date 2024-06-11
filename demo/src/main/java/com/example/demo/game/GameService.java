package com.example.demo.game;
import com.example.demo.cards.Card;
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
            throw new IllegalStateException("User A is already in a game");
        }

        // Check if userB is already associated with a game
        if (gameRepository.existsByUsersContaining(userB)) {
            throw new IllegalStateException("User B is already in a game");
        }

        Game newGame = new Game();
        newGame.getUsers().add(userA);
        newGame.getUsers().add(userB);
        System.out.println("vor speichern");
        gameRepository.save(newGame);
        System.out.println("Game gespeichert");


            for(UserAccount user : newGame.getUsers()) {
                messagingTemplate.convertAndSendToUser(user.getId().toString(), "/queue/create", newGame);
            }
    }








    public void selectDeck(DeckSelectionRequest request) {
        Optional<Game> optionalGame = gameRepository.findById(request.getGameId());
        Optional<Deck> optionalDeck = deckRepository.findById(request.getDeckId());

        if(optionalGame.isEmpty() || optionalDeck.isEmpty()) {
            return;
        }

        Game game = optionalGame.get();
        Deck deck = optionalDeck.get();
        List<Card> cards = deck.getCards();
        Collections.shuffle(cards); // mischt das Deck
        deck.setCards(cards); // neu gemischte Liste an Karten setzen
        UserAccount user = deck.getUser();
        user.getPlayerState().setDeck(deck);
        user.getPlayerState().setReady(true);

        // setzt initial 5 Karten aus dem gemischten Deck auf die Hand
        for(int i = 0; i<5; i++){
            user.getPlayerState().getHandCards().add(deck.getCards().get(i));
        }

        userAccountRepository.save(user);

        // überprüft ob in beiden PlayerStates der Spieler ready auf true gesetzt ist
        for(UserAccount userAccount : game.getUsers()) {
            if(!userAccount.getPlayerState().getReady()){
                break;
            }
            game.setReady(true);
        }
        gameRepository.save(game);

        for(UserAccount player : game.getUsers()) {
            messagingTemplate.convertAndSendToUser(player.getId().toString(), "/queue/selectDeck", game);
        }

    }

    public void playCard(PlayCardRequest request) {

    }

    public Game endTurn(EndTurnRequest request) {
        return null;
    }

    public Game attackCard(AttackCardRequest request) {
        return null;
    }

    public Game attackUser(AttackUserRequest request) {
       return null;
    }

    public Game swapForRare(RareSwapRequest request) {
        // Implementierung
        return null;
    }

    public Game swapForLegendary(LegendarySwapRequest request) {
        // Implementierung
        return null;
    }

    public Game doNothing(Long userID) {
        // Implementierung
        return null;
    }

    public static void terminateMatch(Long gameID) {
        // Implementierung
    }


}
