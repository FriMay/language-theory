package arbina.sps.firebase.service;

import arbina.infra.exceptions.BadRequestException;
import arbina.sps.firebase.NotificationParameter;
import arbina.sps.store.entity.Template;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

@Service
public class FCMService {

    private final Logger logger = LoggerFactory.getLogger(FCMService.class);

    public void sendMessage(Template template)
            throws InterruptedException, ExecutionException {

        Message message = getPreconfiguredMessageWithData(template);

        String response = sendAndGetResponse(message);

//        logger.info("Sent message with data. Topic: " + request.getTopic() + ", " + response);
    }

    private String sendAndGetResponse(Message message) throws InterruptedException, ExecutionException {

        return FirebaseMessaging
                .getInstance()
                .sendAsync(message)
                .get();
    }

    private AndroidConfig getAndroidConfig(String topic) {

        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder().setSound(NotificationParameter.SOUND.getValue())
                        .setColor(NotificationParameter.COLOR.getValue()).setTag(topic).build()).build();
    }

    private ApnsConfig getApnsConfig(String topic) {

        return ApnsConfig.builder()
                .setAps(Aps
                        .builder()
                        .setCategory(topic)
                        .setThreadId(topic)
                        .build())
                .build();
    }

    private Message getPreconfiguredMessageWithData(Template template) {

        //TODO generate topic
        return getPreconfiguredMessageBuilder(template)
                .putAllData(template.getParams())
//                .setTopic(request.getTopic())
                .build();
    }

    private Message.Builder getPreconfiguredMessageBuilder(Template template) {

        //TODO generate topic.
        String sameTopic = "topic";

        AndroidConfig androidConfig = getAndroidConfig(sameTopic);

        ApnsConfig apnsConfig = getApnsConfig(sameTopic);

        //TODO choice language and take title from it
        if (template.getLocalizations().isEmpty()) {
            throw new BadRequestException("Unable to send a notification, add at least one localization to your template");
        }

        Notification.Builder notification = Notification.builder()
                .setTitle(template.getLocalizations().get(0).getTitle())
                .setBody(template.getLocalizations().get(0).getBody());

        return Message.builder()
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(androidConfig)
                .setNotification(notification.build());
    }


}