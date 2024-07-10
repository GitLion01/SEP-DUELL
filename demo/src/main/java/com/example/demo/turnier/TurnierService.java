package com.example.demo.turnier;


import com.example.demo.clan.Clan;
import com.example.demo.clan.ClanRepository;
import com.example.demo.duellHerausforderung.Notification;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class TurnierService {

    private final TurnierRepository turnierRepository;
    private final ClanRepository clanRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserAccountRepository userAccountRepository;
    private final RundeRepository rundeRepository;
    private final MatchRepository matchRepository;

    private final BetRepository betRepository;

    public void turnierStart(Long clanId) {
        Clan clan = clanRepository.findById(clanId).get();
        if(clan.getUsers().size()<2)
            return;
        if(clan.getTurnier() == null) {
            Turnier turnier = new Turnier();
            clan.setTurnier(turnier);
            turnier.setClan(clan);

            turnierRepository.save(turnier);
            clanRepository.save(clan);

            for(UserAccount user : clan.getUsers()) {
                Notification notification = new Notification("turnier");
                messagingTemplate.convertAndSendToUser(user.getId().toString(),"/queue/notifications", notification);
            }
        }
    }

    public void turnierAkzeptieren(Long userId) {
        UserAccount user = userAccountRepository.findById(userId).get();
        Turnier turnier = user.getClan().getTurnier();

        //wenn alle akzeptiert haben
        if(turnier.getAkzeptierteUsers().contains(user)) {
            turnierIsReady(turnier.getId());
        }
        turnier.getAkzeptierteUsers().add(user);
        turnierIsReady(turnier.getId());
    }

    public void turnierAblehnen(Long userId) {
        UserAccount user = userAccountRepository.findById(userId).get();
        Turnier turnier = user.getClan().getTurnier();
        user.getClan().setTurnier(null);
        clanRepository.save(user.getClan());
        turnierRepository.delete(turnier);

        for(UserAccount userx : user.getClan().getUsers()) {
            Notification notification = new Notification("turnierDeleted");
            messagingTemplate.convertAndSendToUser(userx.getId().toString(),"/queue/notifications", notification);
        }
    }

    public boolean turnierIsReady(Long turnierId){
        Optional<Turnier> turnierCheck = turnierRepository.findById(turnierId);
        if(turnierCheck.isPresent()) {
            Turnier turnier = turnierCheck.get();

            Clan clan = clanRepository.findById(turnier.getClan().getId()).get();

            if(turnier.getAkzeptierteUsers().size() == clan.getUsers().size()){
                if(turnier.getRunde().isEmpty()){
                    System.out.println("Turnier is ready");
                    Runde runde = new Runde();
                    runde.setRundeName("Runde 1");

                    verteilung(turnier, runde, turnier.getAkzeptierteUsers());

                    turnier.getRunde().add(runde);
                    rundeRepository.save(runde);
                    turnierRepository.save(turnier);
                }

                for (UserAccount userInClan : clan.getUsers()) {
                    Notification notification = new Notification("turnierReady");
                    messagingTemplate.convertAndSendToUser(userInClan.getId().toString(), "/queue/notifications", notification);
                }
                return true;
            }
        }
        return false;
    }

    public ResponseEntity<List<Match>> getTurnierMatches(Long clanId) {
        Clan clan = clanRepository.findById(clanId).get();
        if(clan.getTurnier()!=null) {
            Turnier turnier = clan.getTurnier();
            System.out.println("Runde hier "+turnier.getRunde().get(turnier.getRunde().size()-1).getRundeName());
            return ResponseEntity.ok(turnier.getRunde().get(turnier.getRunde().size()-1).getMatch());
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ArrayList<>());
    }

    public void SetUserInTurnier(Long userId){
        UserAccount user = userAccountRepository.findById(userId).get();
        user.setInTurnier(true);
    }

    public void GewinnerSpeichernMitId(Long userId) {
        GewinnerSpeichern(userAccountRepository.findById(userId).get());
    }

    public void GewinnerSpeichern(UserAccount user){
        Clan clan = clanRepository.findById(user.getClan().getId()).get();
        if(clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners().contains(user))
            return;

        clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners().add(user);

        //check if the list is complete -> make new Runde
        if(clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners().size()==clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getMatch().size()){

            //der Gewinner
            if(clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners().size()==1){
                user.setSepCoins(user.getSepCoins()+700);
                userAccountRepository.save(user);
                distributeWinnings(user);  // Gewinne verteilen <-- Aufruf der Methode distributeWinnings für Turnierwette Özgür
                deleteBetsForClan(clan); // Wetten löschen
                deleteTurniereData(clan.getTurnier());
                return;
            }

            NeueRunde(user,clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners());

            for(UserAccount users : clan.getUsers()) {
                Notification notification = new Notification("neueRunde");
                messagingTemplate.convertAndSendToUser(users.getId().toString(),"/queue/notifications", notification);
            }
        }
    }

    private void NeueRunde(UserAccount LastGewinnerDerRunde,List<UserAccount> gewinners) {

        Turnier turnier = turnierRepository.findById(LastGewinnerDerRunde.getClan().getTurnier().getId()).get();
        Runde runde = new Runde();
        runde.setRundeName("Runde " + (turnier.getRunde().size()+1));

        verteilung(turnier,runde,gewinners);
    }

    private void verteilung(Turnier turnier,Runde runde,List<UserAccount> players){

        //überprüfe wie viel Matches muss erstellt werden
        int playersToPlay =  players.size();
        if(playersToPlay%2==1)
            playersToPlay++;
        for(int i=0;i<(playersToPlay/2);i++){
            runde.getMatch().add(new Match());
        }

        for(UserAccount userX : players) {
            for (Match match : runde.getMatch()) {
                if (match.getPlayer1() == null) {
                    match.setPlayer1(userX.getId());
                    match.setUserName1(userX.getUsername());
                    matchRepository.save(match);
                    break;
                }
                if (match.getPlayer2() == null) {
                    match.setPlayer2(userX.getId());
                    match.setUserName2(userX.getUsername());
                    matchRepository.save(match);
                    break;
                }
            }
        }

        turnier.getRunde().add(runde);
        rundeRepository.save(runde);
        turnierRepository.save(turnier);
    }

    private void deleteTurniereData(Turnier turnier) {
        Clan clan = clanRepository.findById(turnier.getClan().getId()).get();
        clan.setTurnier(null);
        clanRepository.save(clan);

        turnier.setClan(null);
        turnier.setAkzeptierteUsers(null);

        for(Runde runde : turnier.getRunde()){
            matchRepository.deleteAll(runde.getMatch());
            runde.setMatch(null);
            turnier.setRunde(null);
            rundeRepository.delete(runde);
        }
        turnierRepository.delete(turnier);
    }

    public boolean checkAccepted(Long turnierId, Long userId) {
        Turnier turnier = turnierRepository.findById(turnierId).get();
        UserAccount userAccount = userAccountRepository.findById(userId).get();
        return turnier.getAkzeptierteUsers().contains(userAccount);
    }

    public ResponseEntity<Long> getTurnierId(Long clanId) {
        Clan clan = clanRepository.findById(clanId).get();
        if(clan.getTurnier()!=null)
            return ResponseEntity.ok(clan.getTurnier().getId());
        return ResponseEntity.notFound().build();
    }

    public List<Long> getGewinner(Long clanId) {
        Clan clan = clanRepository.findById(clanId).get();
        List<UserAccount> userAccounts= clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners();
        List<Long> gewinnerIds = new ArrayList<>();
        for(UserAccount userAccount : userAccounts){
            gewinnerIds.add(userAccount.getId());
        }
        return gewinnerIds;
    }





    //Turnierwetten ab hier



    public ResponseEntity<String> placeBet(Long bettorId, Long betOnId) {
        Optional<UserAccount> bettorOpt = userAccountRepository.findById(bettorId);
        Optional<UserAccount> betOnOpt = userAccountRepository.findById(betOnId);

        if (bettorOpt.isPresent() && betOnOpt.isPresent()) {
            UserAccount bettor = bettorOpt.get();
            UserAccount betOn = betOnOpt.get();

            if (bettor.getSepCoins() < 50) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Insufficient SEP-Coins to place the bet.");
            }

            for (Bet bet : bettor.getBets()) {
                if (bet.getBetOn().equals(betOn)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Bet already placed on this user.");
                }
            }

            bettor.setSepCoins(bettor.getSepCoins() - 50);
            Bet bet = new Bet(bettor, betOn);
            bettor.getBets().add(bet);
            betRepository.save(bet);
            userAccountRepository.save(bettor);

            return ResponseEntity.ok("Bet placed successfully.");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }



    public void distributeWinnings(UserAccount winner) {
        List<Bet> bets = betRepository.findByBetOn(winner);

        for (Bet bet : bets) {
            UserAccount bettor = bet.getBettor();
            bettor.setSepCoins(bettor.getSepCoins() + 300); //Lootbox plus Einsatz 250 + 50 SEP Coins
            //Logik für Lootbox
            bet.setWinner(true);
            bet.setCompleted(true); // Markiere Wette als abgeschlossen
            userAccountRepository.save(bettor);
            betRepository.save(bet); // Speichere die aktualisierte Wette
        }
    }

        private void deleteBetsForClan(Clan clan){
            for (UserAccount user : clan.getUsers()) {
                for (Bet bet : user.getBets()) {
                    betRepository.delete(bet);
                }
                user.getBets().clear();
                userAccountRepository.save(user);
            }
        }












}
