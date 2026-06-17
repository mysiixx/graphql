package ee.joeltek.match_me.chat;

import ee.joeltek.match_me.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static ee.joeltek.match_me.support.ChatFixtures.createAcceptedConnectionAndChat;
import static ee.joeltek.match_me.support.ChatFixtures.createChat;
import static ee.joeltek.match_me.support.ChatFixtures.sendMessage;
import static ee.joeltek.match_me.support.ConnectionFixtures.acceptConnection;
import static ee.joeltek.match_me.support.ConnectionFixtures.createPendingConnection;
import static ee.joeltek.match_me.support.ProfileBioFixtures.HIGH_MATCH_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.LOW_MATCH_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.MAXED_OUT_ANSWERS;
import static ee.joeltek.match_me.support.ProfileBioFixtures.completeProfileAndBio;
import static ee.joeltek.match_me.support.RecommendationFixtures.generateRecommendations;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatControllerIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void createChatForAcceptedConnectionReturnsCreated() throws Exception {
        RegisteredUser initiator = registerAndLogin();
        RegisteredUser target = registerAndLogin();

        completeProfileAndBio(mockMvc, initiator.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, target.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, initiator);
        createPendingConnection(mockMvc, initiator, target);

        Long initiatorId = initiator.id();
        acceptConnection(mockMvc, initiatorId, target);

        mockMvc.perform(post("/chats")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(initiator))
                        .content("""
                                {
                                  "targetUserId": %d
                                }
                                """.formatted(target.id())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.otherUserId").value(target.id()))
                .andExpect(jsonPath("$.lastMessageAt").exists())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    void createChatWithoutAcceptedConnectionReturns403() throws Exception {
        RegisteredUser initiator = registerAndLogin();
        RegisteredUser target = registerAndLogin();

        completeProfileAndBio(mockMvc, initiator.accessToken(), "Tallinn", MAXED_OUT_ANSWERS);
        completeProfileAndBio(mockMvc, target.accessToken(), "Tallinn", HIGH_MATCH_ANSWERS);
        generateRecommendations(mockMvc, initiator);

        mockMvc.perform(post("/chats")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(initiator))
                        .content("""
                                {
                                  "targetUserId": %d
                                }
                                """.formatted(target.id())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createChatTwiceReturnsSameChat() throws Exception {
        RegisteredUser initiator = registerAndLogin();
        RegisteredUser target = registerAndLogin();

        Long firstChatId = createAcceptedConnectionAndChat(mockMvc, initiator, target);
        Long secondChatId = createChat(mockMvc, initiator, target);

        org.junit.jupiter.api.Assertions.assertEquals(firstChatId, secondChatId);
        org.junit.jupiter.api.Assertions.assertEquals(1, chatRepository.findAll().size());
    }

    @Test
    void sendMessagePersistsMessageAndUpdatesInboxOrdering() throws Exception {
        RegisteredUser userA = registerAndLogin();
        RegisteredUser userB = registerAndLogin();
        RegisteredUser userC = registerAndLogin();

        Long olderChatId = createAcceptedConnectionAndChat(mockMvc, userA, userB);
        Long newerChatId = createAcceptedConnectionAndChat(mockMvc, userA, userC);

        sendMessage(mockMvc, userA, olderChatId, "older chat ping");

        Thread.sleep(5);

        sendMessage(mockMvc, userA, newerChatId, "newer chat ping");

        mockMvc.perform(get("/chats")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(userA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(newerChatId))
                .andExpect(jsonPath("$[0].otherUserId").value(userC.id()))
                .andExpect(jsonPath("$[1].id").value(olderChatId))
                .andExpect(jsonPath("$[1].otherUserId").value(userB.id()));
    }

    @Test
    void getMessagesReturnsPaginatedLatestFirst() throws Exception {
        RegisteredUser initiator = registerAndLogin();
        RegisteredUser target = registerAndLogin();

        Long chatId = createAcceptedConnectionAndChat(mockMvc, initiator, target);

        sendMessage(mockMvc, initiator, chatId, "first");
        Thread.sleep(5);
        sendMessage(mockMvc, target, chatId, "second");
        Thread.sleep(5);
        sendMessage(mockMvc, initiator, chatId, "third");

        mockMvc.perform(get("/chats/" + chatId + "/messages")
                        .contentType(APPLICATION_JSON)
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", authHeaderValue(initiator)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].content").value("third"))
                .andExpect(jsonPath("$.items[1].content").value("second"));
    }

    @Test
    void outsiderCannotReadOrSendMessages() throws Exception {
        RegisteredUser initiator = registerAndLogin();
        RegisteredUser target = registerAndLogin();
        RegisteredUser outsider = registerAndLogin();

        completeProfileAndBio(mockMvc, outsider.accessToken(), "Tartu", LOW_MATCH_ANSWERS);
        Long chatId = createAcceptedConnectionAndChat(mockMvc, initiator, target);

        mockMvc.perform(get("/chats/" + chatId + "/messages")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(outsider)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/chats/" + chatId + "/messages")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(outsider))
                        .content("""
                                {
                                  "content": "I should not be here"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void getChatsShowsUnreadCountForRecipient() throws Exception {
        RegisteredUser initiator = registerAndLogin();
        RegisteredUser target = registerAndLogin();

        Long chatId = createAcceptedConnectionAndChat(mockMvc, initiator, target);

        sendMessage(mockMvc, initiator, chatId, "hello");
        sendMessage(mockMvc, initiator, chatId, "still unread");

        mockMvc.perform(get("/chats")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(target)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(chatId))
                .andExpect(jsonPath("$[0].otherUserId").value(initiator.id()))
                .andExpect(jsonPath("$[0].unreadCount").value(2));

        org.junit.jupiter.api.Assertions.assertEquals(2, messageRepository.findAll().size());
    }

    @Test
    void markChatAsReadMarksOnlyOtherUsersUnreadMessagesAndReturns204() throws Exception {
        RegisteredUser initiator = registerAndLogin();
        RegisteredUser target = registerAndLogin();

        Long chatId = createAcceptedConnectionAndChat(mockMvc, initiator, target);

        Long firstUnreadId = sendMessage(mockMvc, initiator, chatId, "first unread");
        Long secondUnreadId = sendMessage(mockMvc, initiator, chatId, "second unread");
        Long ownMessageId = sendMessage(mockMvc, target, chatId, "own message stays unread for initiator");

        mockMvc.perform(post("/chats/" + chatId + "/read")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(target)))
                .andExpect(status().isNoContent());

        assertEquals(0, messageRepository.countUnreadMessagesForUserInChat(chatId, target.id()));
        assertEquals(1, messageRepository.countUnreadMessagesForUserInChat(chatId, initiator.id()));
        assertNotNull(messageRepository.findById(firstUnreadId).orElseThrow().getReadAt());
        assertNotNull(messageRepository.findById(secondUnreadId).orElseThrow().getReadAt());
        assertNull(messageRepository.findById(ownMessageId).orElseThrow().getReadAt());
    }

    @Test
    void outsiderCannotMarkChatAsRead() throws Exception {
        RegisteredUser initiator = registerAndLogin();
        RegisteredUser target = registerAndLogin();
        RegisteredUser outsider = registerAndLogin();

        completeProfileAndBio(mockMvc, outsider.accessToken(), "Tartu", LOW_MATCH_ANSWERS);
        Long chatId = createAcceptedConnectionAndChat(mockMvc, initiator, target);

        mockMvc.perform(post("/chats/" + chatId + "/read")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(outsider)))
                .andExpect(status().isForbidden());
    }
}
