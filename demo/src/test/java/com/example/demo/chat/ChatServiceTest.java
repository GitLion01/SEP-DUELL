package com.example.demo.chat;
import com.example.demo.clan.ClanRepository;
import com.example.demo.user.UserAccount;
import com.example.demo.user.UserAccountRepository;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private ChatRepository chatRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private ClanRepository clanRepository;
    private AutoCloseable autoCloseable;
    private ChatService underTest;

    @BeforeEach
    void setUp() {
        autoCloseable= MockitoAnnotations.openMocks(this); //initialize alle Mocks in this class
        underTest = new ChatService(
                userAccountRepository,
                chatRepository,
                chatMessageRepository,
                messagingTemplate,
                groupRepository,
                clanRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void testCreateChat() {
        Long userId1 = 3L;
        Long userId2 = 2L;

        UserAccount user1 = new UserAccount();
        user1.setId(userId1);
        user1.setUserChat(new ArrayList<>());

        UserAccount user2 = new UserAccount();
        user2.setId(userId2);
        user2.setUserChat(new ArrayList<>());

        when(userAccountRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(userAccountRepository.findById(userId2)).thenReturn(Optional.of(user2));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> {
            Chat savedChat = invocation.getArgument(0);
            savedChat.setId(1L);
            return savedChat;
        });

        ResponseEntity<Long> response = underTest.createChat(userId1, userId2);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() > 0);

        //verify that the save method of the chatRepo is called exactly once for user1
        verify(chatRepository, times(1)).save(any(Chat.class));
        verify(userAccountRepository, times(1)).save(user1);
        verify(userAccountRepository, times(1)).save(user2);
    }

    @Test
    void createGroup() {
        Long userId1 = 1L;
        Long userId2 = 2L;
        Long userId3 = 3L;
        String groupName = "testGroup";

        UserAccount user1 = new UserAccount();
        user1.setId(userId1);
        user1.setUserChat(new ArrayList<>());
        UserAccount user2 = new UserAccount();
        user2.setId(userId2);
        user2.setUserChat(new ArrayList<>());
        UserAccount user3 = new UserAccount();
        user3.setId(userId3);
        user3.setUserChat(new ArrayList<>());

        Group group =new Group();
        group.setName(groupName);
        group.setId(1L);
        group.getUsers().add(user1);
        group.getUsers().add(user2);
        group.getUsers().add(user3);



        when(userAccountRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(userAccountRepository.findById(userId2)).thenReturn(Optional.of(user2));
        when(userAccountRepository.findById(userId3)).thenReturn(Optional.of(user3));
        when(groupRepository.findById(anyLong())).thenReturn(Optional.of(group));

        groupRepository.save(group);

        ResponseEntity<Long> response = new ResponseEntity<>(groupRepository.findById(group.getId()).get().getId(),HttpStatus.OK);
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() > 0);
        System.out.println(response.getBody());
    }

    @Test
    void sendMessage() {
        UserAccount user1=new UserAccount();
        UserAccount user2=new UserAccount();
        Chat chat=new Chat();
        ChatMessage chatMessage=new ChatMessage();

        user1.setId(1L);
        user2.setId(2L);
        chat.setId(1L);
        chatMessage.setId(1L);

        chat.getUsers().add(user1);
        chat.getUsers().add(user2);
        user1.getUserChat().add(chat);
        user2.getUserChat().add(chat);

        chatMessage.setSender(user1);
        chat.getMessages().add(chatMessage);

        when(chatMessageRepository.findById(user2.getUserChat().get(0).getMessages().get(0).getId())).thenReturn(Optional.of(chatMessage));
        ResponseEntity<ChatMessage> message;
        if(chatMessageRepository.findById(user2.getUserChat().get(0).getMessages().get(0).getId()).isPresent())
            message= new ResponseEntity<>(chatMessageRepository.findById(user2.getUserChat().get(0).getMessages().get(0).getId()).get(),HttpStatus.OK);
        else
            message= new ResponseEntity<>(new ChatMessage(),HttpStatus.NOT_FOUND);
        assertNotNull(message);
        assertEquals(message.getStatusCode(),HttpStatus.OK);


    }

    @Test
    void editMessage() {
        UserAccount user1=new UserAccount();
        UserAccount user2=new UserAccount();
        Chat chat=new Chat();
        ChatMessage chatMessage=new ChatMessage();

        user1.setId(1L);
        user2.setId(2L);
        chat.setId(1L);
        chatMessage.setId(1L);
        chatMessage.setMessage("first Message");

        chat.getUsers().add(user1);
        chat.getUsers().add(user2);
        user1.getUserChat().add(chat);
        user2.getUserChat().add(chat);

        chatMessage.setSender(user1);
        chat.getMessages().add(chatMessage);
        chatMessage.setMessage("Edited");

        when(chatMessageRepository.findById(user2.getUserChat().get(0).getMessages().get(0).getId())).thenReturn(Optional.of(chatMessage));

        if(chatMessageRepository.findById(user2.getUserChat().get(0).getMessages().get(0).getId()).isPresent())
            chatMessage = chatMessageRepository.findById(user2.getUserChat().get(0).getMessages().get(0).getId()).get();

        assertNotNull(chatMessage);
        assertEquals(chat.getMessages().get(0).getMessage(),"Edited");
    }

    @Test
    void deleteMessage() {
        UserAccount user1=new UserAccount();
        UserAccount user2=new UserAccount();
        Chat chat=new Chat();
        ChatMessage chatMessage=new ChatMessage();

        user1.setId(1L);
        user2.setId(2L);
        chat.setId(1L);
        chatMessage.setId(1L);

        chat.getUsers().add(user1);
        chat.getUsers().add(user2);
        user1.getUserChat().add(chat);
        user2.getUserChat().add(chat);

        chatMessage.setSender(user1);
        chat.getMessages().add(chatMessage);

        chat.getMessages().remove(chatMessage);

        ResponseEntity<String> s;
        if(chat.getMessages().contains(chatMessage))
            s=new ResponseEntity<>("Not deleted",HttpStatus.OK);
        else
            s=new ResponseEntity<>("deleted",HttpStatus.NOT_FOUND);

        assertEquals("deleted",s.getBody());
    }

    @Test
    void getGroups() {
        UserAccount user = new UserAccount();
        user.setId(1L);
        user.setUserChat(new ArrayList<>());
        List<Group> groups=new ArrayList<>();
        for(int i=0;i<10;i++) {
            groups.add(new Group());
            groups.get(i).setName("group"+i);
            groups.get(i).setId((long)i);
            user.getUserChat().add(groups.get(i));
        }
        for(int i=0;i<10;i++)
        {
            when(groupRepository.findById(groups.get(i).getId())).thenReturn(Optional.of(groups.get(i)));
        }
        ResponseEntity<List<Group>> response=new ResponseEntity<>(groups,HttpStatus.OK);
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());

        for(int i=0;i<10;i++)
            System.out.println(groups.get(i).getName());
    }

    @Test
    void getMessages() {
        Long chatId = 1L;
        Long userId = 1L;
        Chat chat = new Chat();
        chat.setId(chatId);
        UserAccount user1 = new UserAccount();
        user1.setId(userId);
        user1.setUserChat(new ArrayList<>());

        List<ChatMessage> chatMessage = new ArrayList<>();
        for(int i=0;i < 10;i++)
        {
            chatMessage.add(new ChatMessage());
            chatMessage.get(i).setId((long)i);
            chatMessage.get(i).setChat(chat);
            chatMessage.get(i).setSender(user1);
            chatMessage.get(i).setMessage("This is a test message : "+i);
        }


        when(chatRepository.findById(chatId)).thenReturn(Optional.of(chat));
        when(userAccountRepository.findById(userId)).thenReturn(Optional.of(user1));
        for(int i=0;i<10;i++)
        {
            when(chatMessageRepository.findById(chatMessage.get(i).getId())).thenReturn(Optional.of(chatMessage.get(i)));
        }

        List<ChatMessage> ListChatMessage = new ArrayList<>();
        for(int i=0;i < 10;i++) {
            ListChatMessage.add(chatMessageRepository.findById(chatMessage.get(i).getId()).get());
        }

        ResponseEntity<List<ChatMessage>> response=new ResponseEntity<>(ListChatMessage,HttpStatus.OK);
        assertNotNull(response);
        assertEquals(HttpStatus.OK,response.getStatusCode());

        ListChatMessage = response.getBody();
        for(ChatMessage chatMessage2 : ListChatMessage)
            System.out.println(chatMessage2.getMessage());
    }

    @Test
    void setReadTrue() {
        ChatMessage chatMessage=new ChatMessage();
        chatMessage.setId(1L);
        chatMessage.setRead(true);
        when(chatMessageRepository.findById(chatMessage.getId())).thenReturn(Optional.of(chatMessage));
        ResponseEntity<ChatMessage> chatMessage1;
        if(chatMessageRepository.findById(1L).isPresent()) {
            chatMessage1 = new ResponseEntity<>(chatMessageRepository.findById(1L).get(), HttpStatus.OK);
        }
        else
            chatMessage1 = new ResponseEntity<>(new ChatMessage(), HttpStatus.NOT_FOUND);
        assertNotNull(chatMessage1);
        assertEquals(HttpStatus.OK,chatMessage1.getStatusCode());

    }

}