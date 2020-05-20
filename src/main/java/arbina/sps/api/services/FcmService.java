package arbina.sps.api.services;

import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.entity.Template;
import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class FcmService {


    public List<ApiFuture<BatchResponse>> sendMessage(Template template, Stream<DeviceToken> deviceTokens) {

        //TODO generate topic
        String topic = "sameTopic";

        List<MulticastMessage> messages = getMulticastMessageList(template, deviceTokens, topic);

        List<ApiFuture<BatchResponse>> futures = new ArrayList<>();

        for (MulticastMessage message: messages){
            futures.add(sendAsyncMessage(message));
        }

        return futures;
    }

    private ApiFuture<BatchResponse> sendAsyncMessage(MulticastMessage message) {

        return FirebaseMessaging
                .getInstance()
                .sendMulticastAsync(message);
    }

    private AndroidConfig getAndroidConfig(String topic) {

        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis()).setCollapseKey(topic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder().setSound(NotificationParameter.SOUND.getValue())
                        .setColor(NotificationParameter.COLOR.getValue()).setTag(topic).build()).build();
    }

    private ApnsConfig getApnsConfig(Template template, String topic) {

        return ApnsConfig.builder()
                .setAps(Aps
                        .builder()
                        .setBadge(template.getBadge())
                        .setCategory(topic)
                        .setThreadId(topic)
                        .build())
                .build();
    }

    private List<MulticastMessage> getMulticastMessageList(Template template, Stream<DeviceToken> deviceTokens, String topic) {

        Map<String, MulticastMessage.Builder> localeMulticastMessage = new HashMap<>();

        ApnsConfig apnsConfig = getApnsConfig(template, topic);

        AndroidConfig androidConfig = getAndroidConfig(topic);

        deviceTokens.forEach((deviceToken) -> {

            String[] locales = {deviceToken.getLocaleIso(), "en", template.getLocalizations().get(0).getLocaleIso()};

            for (String locale : locales) {

                MulticastMessage.Builder multicastMessage = localeMulticastMessage.get(locale);

                if (multicastMessage != null) {

                    multicastMessage.addToken(deviceToken.getToken());

                    break;
                }

                boolean isAdd = false;

                for (Localization localization : template.getLocalizations()) {

                    if (locale.equals(localization.getLocaleIso())) {

                        isAdd = true;

                        multicastMessage = getPreconfiguredMulticastMessageBuilder(localization, apnsConfig, androidConfig);

                        multicastMessage.addToken(deviceToken.getToken());

                        multicastMessage.putAllData(template.getParams());

                        multicastMessage.putData("badge", template.getBadge().toString());

                        localeMulticastMessage.put(localization.getLocaleIso(), multicastMessage);

                        break;
                    }
                }

                if (isAdd){
                    break;
                }

            }
        });

        List<MulticastMessage> messages = new ArrayList<>();

        for (Map.Entry<String, MulticastMessage.Builder> entry: localeMulticastMessage.entrySet()){
            messages.add(entry.getValue().build());
        }

        return messages;
    }

    private MulticastMessage.Builder getPreconfiguredMulticastMessageBuilder(Localization localization, ApnsConfig apnsConfig, AndroidConfig androidConfig) {

        Notification.Builder notification = Notification.builder()
                .setTitle(localization.getTitle())
                .setBody(localization.getBody());

        return MulticastMessage.builder()
                .setApnsConfig(apnsConfig)
                .setAndroidConfig(androidConfig)
                .setNotification(notification.build());
    }

}