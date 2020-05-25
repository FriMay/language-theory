package arbina.sps.api.services;

import arbina.infra.localization.Locales;
import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.entity.Template;
import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Stream;

@Service
public class FcmService {


    public List<ApiFuture<BatchResponse>> sendMessage(Template template,
                                                      Stream<DeviceToken> deviceTokens,
                                                      FirebaseApp app) {

        List<MulticastMessage> messages = getMulticastMessageList(template, deviceTokens);

        List<ApiFuture<BatchResponse>> futures = new ArrayList<>();

        for (MulticastMessage message : messages) {
            futures.add(sendAsyncMessage(message, app));
        }

        return futures;
    }

    private ApiFuture<BatchResponse> sendAsyncMessage(MulticastMessage message,
                                                      FirebaseApp app) {

        return FirebaseMessaging
                .getInstance(app)
                .sendMulticastAsync(message);
    }

    private AndroidConfig getAndroidConfig() {

        String randomTopic = UUID.randomUUID().toString();

        return AndroidConfig.builder()
                .setTtl(Duration.ofMinutes(2).toMillis())
                .setCollapseKey(randomTopic)
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(
                        AndroidNotification
                                .builder()
                                .setSound(NotificationParameter.SOUND.getValue())
                                .setColor(NotificationParameter.COLOR.getValue())
                                .setTag(randomTopic)
                                .build())
                .build();
    }

    private List<MulticastMessage> getMulticastMessageList(Template template,
                                                           Stream<DeviceToken> deviceTokens) {

        Map<String, MulticastMessage.Builder> localeMulticastMessage = new HashMap<>();

        AndroidConfig androidConfig = getAndroidConfig();

        deviceTokens.forEach((deviceToken) -> {

            String[] locales = {deviceToken.getLocaleIso(), Locales.defaultLanguageCode.getName(), template.getLocalizations().get(0).getLocaleIso()};

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

                        multicastMessage = getPreconfiguredMulticastMessageBuilder(localization, androidConfig);

                        multicastMessage.addToken(deviceToken.getToken());

                        multicastMessage.putAllData(template.getParams());

                        localeMulticastMessage.put(localization.getLocaleIso(), multicastMessage);

                        break;
                    }
                }

                if (isAdd) {
                    break;
                }

            }
        });

        List<MulticastMessage> messages = new ArrayList<>();

        for (Map.Entry<String, MulticastMessage.Builder> entry : localeMulticastMessage.entrySet()) {
            messages.add(entry.getValue().build());
        }

        return messages;
    }

    private MulticastMessage.Builder getPreconfiguredMulticastMessageBuilder(Localization localization,
                                                                             AndroidConfig androidConfig) {

        Notification.Builder notification = Notification.builder()
                .setTitle(localization.getTitle())
                .setBody(localization.getBody());

        return MulticastMessage.builder()
                .setAndroidConfig(androidConfig)
                .setNotification(notification.build());
    }
}