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

    public boolean turnierIsReady(Long turnierId){
        Turnier turnier = turnierRepository.findById(turnierId).get();
        Clan clan = clanRepository.findById(turnier.getClan().getId()).get();

        if(turnier.getAkzeptierteUsers().size() == clan.getUsers().size()){
                System.out.println("Turnier is ready");
                Runde runde = new Runde();
                runde.setRundeName("Runde 1");

                verteilung(turnier, runde, turnier.getAkzeptierteUsers());

                turnier.getRunde().add(runde);
                rundeRepository.save(runde);
                turnierRepository.save(turnier);

                for (UserAccount userInClan : clan.getUsers()) {
                    Notification notification = new Notification("turnierReady");
                    messagingTemplate.convertAndSendToUser(userInClan.getId().toString(), "/queue/notifications", notification);
                }
                return true;
        }
        return false;
    }

    public ResponseEntity<List<Match>> getTurnierMatches(Long clanId) {
        Clan clan = clanRepository.findById(clanId).get();
        if(clan.getTurnier()!=null) {
            Turnier turnier = clan.getTurnier();
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
        clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners().add(user);

        //check if the list is complete -> make new Runde
        if(clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners().size()==clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getMatch().size()){

            //der Gewinner
            if(clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners().size()==1){
                user.setSepCoins(user.getSepCoins()+700);
                userAccountRepository.save(user);
                deleteTurniereData(clan.getTurnier());
                return;
            }

            NeueRunde(user,clan.getTurnier().getRunde().get(clan.getTurnier().getRunde().size()-1).getGewinners());
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

}
