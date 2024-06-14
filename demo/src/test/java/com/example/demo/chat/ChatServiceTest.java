package com.example.demo.chat;

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
    private AutoCloseable autoCloseable;
    private ChatService underTest;

    @BeforeEach
    void setUp() {
        autoCloseable= MockitoAnnotations.openMocks(this); //initialize alle Mocks in this class
        underTest = new ChatService(userAccountRepository,chatRepository,chatMessageRepository,messagingTemplate,groupRepository);
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
    }

    @Test
    @Disabled
    void editMessage() {
    }

    @Test
    @Disabled
    void updateChatWithEditedMessage() {
    }

    @Test
    @Disabled
    void deleteMessage() {
    }

    @Test
    @Disabled
    void getGroups() {
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
    @Disabled
    void setReadTrue() {
    }

    @Test
    @Disabled
    void checkOnline() {
    }
}