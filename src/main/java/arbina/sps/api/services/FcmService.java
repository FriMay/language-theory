package arbina.sps.api.services;

import arbina.infra.localization.Locales;
import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.entity.Template;
import com.google.api.core.ApiFuture;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.neovisionaries.i18n.CountryCode;
import com.neovisionaries.i18n.LanguageCode;
import com.neovisionaries.i18n.LocaleCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FcmService {

    public List<ApiFuture<BatchResponse>> sendMessage(Template template,
                                                      Stream<DeviceToken> deviceTokens,
                                                      FirebaseApp app) {


        return getMulticastMessageList(template, deviceTokens, app);
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

    private List<ApiFuture<BatchResponse>> getMulticastMessageList(Template template,
                                                                   Stream<DeviceToken> deviceTokens,
                                                                   FirebaseApp app) {
        //Key - LocalCode string name
        Map<String, DeviceMessage> deviceMessages =
                new HashMap<>();

        List<ApiFuture<BatchResponse>> futures = new ArrayList<>();

        AndroidConfig androidConfig = getAndroidConfig();

        //Init all DeviceMessages base on localizations
        for (Localization localization : template.getLocalizations()) {

            MulticastMessage.Builder builder = getPreconfiguredMulticastMessageBuilder(localization, androidConfig);

            builder.putAllData(template.getParams());

            //Checking for the wrong locale occurs in the controller
            LocaleCode localeCode = LocaleCode.valueOf(localization.getLocaleIso());

            deviceMessages.put(localeCode.toString(),
                    DeviceMessage.builder()
                            .isExist(true)
                            .message(Pair.of(builder, new Counter(0)))
                            .localization(localization)
                            .build());
        }

        deviceTokens.forEach((deviceToken) -> {

            List<LocaleCode> locales = Locales
                    .convertAcceptLanguageToLocaleCodes(deviceToken.getAcceptLanguage())
                    .collect(Collectors.toList());

            //Adding the locale of the first localization to the end so that the notification is sent exactly
            locales.add(LocaleCode.valueOf(template.getLocalizations().get(0).getLocaleIso()));

            for (LocaleCode locale : locales) {

                DeviceMessage deviceMessage = deviceMessages.get(locale.toString());

                if (deviceMessage != null) {

                    //If the locale doesn't have a MulticastMessage
                    if (!deviceMessage.getIsExist()) {
                        continue;
                    }

                    addTokenToMulticastMessage(deviceToken.getToken(),
                            deviceMessage,
                            androidConfig,
                            futures,
                            app);

                    break;
                }

                boolean isAdd = false;

                for (Localization localization : template.getLocalizations()) {

                    LanguageCode localeLanguageCode = locale.getLanguage();

                    CountryCode localeCountryCode = locale.getCountry();

                    LocaleCode localeIsoCode = LocaleCode.valueOf(localization.getLocaleIso());

                    LanguageCode localeIsoLanguageCode = localeIsoCode.getLanguage();

                    CountryCode localeIsoCountryCode = localeIsoCode.getCountry();

                    if (localeLanguageCode.equals(localeIsoLanguageCode)) {

                        if (localeCountryCode == null || localeCountryCode.equals(localeIsoCountryCode)) {

                            isAdd = true;

                            deviceMessage = deviceMessages
                                    .get(LocaleCode.valueOf(localization.getLocaleIso()).toString());

                            addTokenToMulticastMessage(deviceToken.getToken(),
                                    deviceMessage,
                                    androidConfig,
                                    futures,
                                    app);

                            deviceMessages.put(locale.toString(), deviceMessage);

                            break;
                        }
                    }
                }

                if (isAdd) {
                    break;
                }

                deviceMessages.put(locale.toString(), DeviceMessage.builder().isExist(false).build());
            }
        });

        for (Map.Entry<String, DeviceMessage> entry : deviceMessages.entrySet()) {

            if (entry.getValue().getIsExist()) {

                DeviceMessage deviceMessage = entry.getValue();

                Counter counter = deviceMessage.getMessage().getValue();

                if (counter.getCount() > 0 && counter.getCount() < 500) {

                    MulticastMessage.Builder message = deviceMessage.getMessage().getKey();

                    futures.add(sendAsyncMessage(message.build(), app));
                }
            }
        }

        return futures;
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

    private void addTokenToMulticastMessage(String token,
                                            DeviceMessage deviceMessage,
                                            AndroidConfig androidConfig,
                                            List<ApiFuture<BatchResponse>> futures,
                                            FirebaseApp app) {

        Pair<MulticastMessage.Builder, Counter> pair =
                deviceMessage.getMessage();

        Counter counter = pair.getValue();

        MulticastMessage.Builder message = pair.getKey();

        if (counter.getCount() < 500) {

            message.addToken(token);

            counter.increment();

        } else {

            futures.add(sendAsyncMessage(message.build(), app));

            MulticastMessage.Builder builder =
                    getPreconfiguredMulticastMessageBuilder(deviceMessage.getLocalization(), androidConfig);

            builder.addToken(token);

            deviceMessage.setMessage(Pair.of(builder, new Counter(1)));
        }
    }
}

class Counter {

    private Integer count;

    public Counter(Integer count) {
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public void increment() {
        this.count++;
    }
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DeviceMessage {

    private Boolean isExist;

    private Pair<MulticastMessage.Builder, Counter> message;

    private Localization localization;
}