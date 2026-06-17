package ee.joeltek.match_me.websocket;

import static ee.joeltek.match_me.support.ChatFixtures.createAcceptedConnectionAndChat;
import static ee.joeltek.match_me.support.ChatFixtures.sendMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import ee.joeltek.match_me.chat.ChatAccessService;
import ee.joeltek.match_me.chat.MessageDto;
import ee.joeltek.match_me.support.IntegrationTestSupport;

public class WebSocketIntegrationTests extends IntegrationTestSupport {

    @MockitoSpyBean
    private ChatWebSocketService chatWebSocketService;

    @Autowired
    private ChatAccessService chatAccessService;

    @Test
    void typingStartedDelegatesToWebSocketServiceWithPrincipalUserId() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();
        Long chatId = createAcceptedConnectionAndChat(mockMvc, sender, receiver);

        reset(chatWebSocketService);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(String.valueOf(sender.id()))
                .build();

        new ChatWebSocketController(chatWebSocketService, chatAccessService)
                .typingStarted(chatId, new JwtAuthenticationToken(jwt));

        verify(chatWebSocketService).sendTypingStarted(chatId, sender.id());
    }

    @Test
    void sendingMessageViaRestTriggersWebSocketEvents() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();
        Long chatId = createAcceptedConnectionAndChat(mockMvc, sender, receiver);

        reset(chatWebSocketService);

        sendMessage(mockMvc, sender, chatId, "hello over ws");

        verify(chatWebSocketService).sendMessageSent(
                eq(chatId),
                any(MessageDto.class)
        );
        verify(chatWebSocketService).sendChatUpdated(
                eq(sender.id()),
                eq(chatId),
                eq("hello over ws"),
                any(Instant.class),
                eq(sender.id()),
                eq(0L)
        );
        verify(chatWebSocketService).sendChatUpdated(
                eq(receiver.id()),
                eq(chatId),
                eq("hello over ws"),
                any(Instant.class),
                eq(sender.id()),
                eq(1L)
        );
    }

    @Test
    void markingChatAsReadViaRestTriggersReadAndUserChatUpdatedEvents() throws Exception {
        RegisteredUser sender = registerAndLogin();
        RegisteredUser receiver = registerAndLogin();

        Long chatId = createAcceptedConnectionAndChat(mockMvc, sender, receiver);
        Long firstMessageId = sendMessage(mockMvc, sender, chatId, "first unread");
        Long secondMessageId = sendMessage(mockMvc, sender, chatId, "second unread");

        reset(chatWebSocketService);

        mockMvc.perform(post("/chats/" + chatId + "/read")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", authHeaderValue(receiver)))
                .andExpect(status().isNoContent());

        verify(chatWebSocketService, times(2)).sendMessageRead(eq(chatId), anyLong(), any(Instant.class));
        verify(chatWebSocketService).sendMessageRead(eq(chatId), eq(firstMessageId), any(Instant.class));
        verify(chatWebSocketService).sendMessageRead(eq(chatId), eq(secondMessageId), any(Instant.class));
        verify(chatWebSocketService).sendChatUpdated(
                eq(receiver.id()),
                eq(chatId),
                eq("second unread"),
                any(Instant.class),
                eq(sender.id()),
                eq(0L)
        );
    }
}
