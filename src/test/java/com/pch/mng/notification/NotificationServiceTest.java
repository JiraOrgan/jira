package com.pch.mng.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pch.mng.user.UserAccount;
import com.pch.mng.user.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    @Mock
    private NotificationProperties props;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender javaMailSender;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        NotificationProperties.Slack slack = new NotificationProperties.Slack();
        NotificationProperties.Mail mail = new NotificationProperties.Mail();
        mail.setFrom("notify@test.com");
        when(props.getSlack()).thenReturn(slack);
        when(props.getMail()).thenReturn(mail);
        notificationService =
                new NotificationService(
                        props, userAccountRepository, mailSenderProvider, new ObjectMapper());
    }

    @Test
    void skipsWhenDisabled() {
        when(props.isEnabled()).thenReturn(false);
        CommentMentionNotificationEvent event =
                new CommentMentionNotificationEvent(
                        "P-1", "PROJ", 1L, "A", "hi", Set.of(2L));
        notificationService.notifyCommentMentions(event);
        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    void sendsEmailToMentionedUserWhenMailSenderAvailable() {
        when(props.isEnabled()).thenReturn(true);
        when(mailSenderProvider.getIfAvailable()).thenReturn(javaMailSender);

        UserAccount target =
                UserAccount.builder()
                        .email("t@ex.com")
                        .password("x")
                        .name("Target")
                        .build();
        ReflectionTestUtils.setField(target, "id", 2L);
        when(userAccountRepository.findById(2L)).thenReturn(Optional.of(target));

        CommentMentionNotificationEvent event =
                new CommentMentionNotificationEvent(
                        "P-1", "PROJ", 1L, "Author", "hello @Target", Set.of(2L));

        notificationService.notifyCommentMentions(event);

        ArgumentCaptor<SimpleMailMessage> cap = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(javaMailSender).send(cap.capture());
        assertThat(cap.getValue().getTo()).containsExactly("t@ex.com");
        assertThat(cap.getValue().getSubject()).contains("PROJ").contains("P-1");
    }
}
