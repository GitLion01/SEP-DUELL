package com.example.demo.clan;


import com.example.demo.chat.ChatRepository;
import com.example.demo.chat.Group;
import com.example.demo.chat.GroupRepository;
import com.example.demo.dto.UserDTO;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class ClanService {
    private final ClanRepository clanRepository;
    private final UserAccountRepository userAccountRepository;
    private final GroupRepository groupRepository;

    public ResponseEntity<Object> createClan(String clanName) {
        Optional<Clan> clanCheck = clanRepository.findByName(clanName);
        if(clanCheck.isPresent()) {
            return new ResponseEntity<>("clan already exists", HttpStatus.FORBIDDEN);
        }
        Clan clan = new Clan();
        clan.setName(clanName);
        clan.getGroup().setName(clanName);
        groupRepository.save(clan.getGroup());
        clanRepository.save(clan);
        return ResponseEntity.ok(clan.getId());
    }

    public ResponseEntity<List<ClanDTO>> getClans() {
        List<Clan> clanList= clanRepository.findAll();
        if(clanList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(convertToClanDTO(clanList), HttpStatus.OK);
    }

    public ResponseEntity<Long> getClanId(Long userId) {
        UserAccount user = userAccountRepository.findById(userId).get();
        if(user.getClan()==null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return ResponseEntity.ok(user.getClan().getId());
    }

    public ResponseEntity<List<UserDTO>> getClanMitglieder(Long clanId) {
        Optional<Clan> clanCheck= clanRepository.findById(clanId);
        return clanCheck.map(clan -> ResponseEntity.ok(convertToUserDTO(clan))).orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    private List<UserDTO> convertToUserDTO(Clan clan) {
        List<UserDTO> userDTOList = new ArrayList<>();
        for(UserAccount user : clan.getUsers())
        {
            userDTOList.add(new UserDTO(user.getUsername(), user.getId(), user.getFirstName(),user.getLastName()));
        }
        return userDTOList;
    }

    private List<ClanDTO> convertToClanDTO(List<Clan> clanList) {
        List<ClanDTO> clanDTOList = new ArrayList<>();
        for(Clan clan : clanList) {
            clanDTOList.add(new ClanDTO(clan.getId(),clan.getName()));
        }
        return clanDTOList;
    }

    public ResponseEntity<String> joinClan(Long clanId, Long userId) {
        UserAccount user = userAccountRepository.findById(userId).get();
        if(user.getClan()!= null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("You already have a clan");
        Clan clan = clanRepository.findById(clanId).get();
        clan.getUsers().add(user);
        clan.getGroup().getUsers().add(user);

        Group group = groupRepository.findById(clan.getGroup().getId()).get();
        group.getUsers().add(user);
        groupRepository.save(group);

        clanRepository.save(clan);
        user.setClan(clan);
        userAccountRepository.save(user);
        return ResponseEntity.ok("You have joined a clan");
    }

    public ResponseEntity<String> leaveClan(Long userId) {
        UserAccount user = userAccountRepository.findById(userId).get();

        Optional<Clan> clanCheck = clanRepository.findById(user.getClan().getId());
        if(clanCheck.isPresent()) {
            Clan clan = clanCheck.get();
            clan.getUsers().remove(user);
            clan.getGroup().getUsers().remove(user);
            clanRepository.save(clan);
            user.setClan(null);
            userAccountRepository.save(user);
            return ResponseEntity.ok("You have left a clan");
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("You are not in a clan");
    }
}
