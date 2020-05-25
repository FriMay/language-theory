package arbina.sps.api.services;

import arbina.infra.localization.Locales;
import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.entity.Localization;
import arbina.sps.store.entity.Template;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsPushNotification;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ApnsService {

    public List<PushNotificationFuture<ApnsPushNotification, PushNotificationResponse<ApnsPushNotification>>> sendMessage(
            Template template,
            Stream<DeviceToken> deviceTokens,
            ApnsClient apnsClient,
            String topic) {

        List<PushNotificationFuture<ApnsPushNotification, PushNotificationResponse<ApnsPushNotification>>> futures
                = new ArrayList<>();

        deviceTokens.forEach((deviceToken) -> {

            String payload = getPayload(template, deviceToken);

            String token = TokenUtil.sanitizeTokenString(deviceToken.getToken());

            SimpleApnsPushNotification notification = new SimpleApnsPushNotification(token, topic, payload);

            futures.add(apnsClient.sendNotification(notification));

        });

        return futures;
    }

    public String getPayload(Template template, DeviceToken deviceToken) {

        ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();

        Localization localization = getLocalization(template, deviceToken);

        payloadBuilder.setAlertTitle(localization.getTitle());
        payloadBuilder.setAlertSubtitle(localization.getSubtitle());
        payloadBuilder.setAlertBody(localization.getBody());

        return payloadBuilder.build();
    }

    private Localization getLocalization(Template template, DeviceToken deviceToken) {

        String[] locales = {deviceToken.getPreferredLanguage(), Locales.defaultLanguageCode.getName()};

        for (String locale : locales) {

            for (Localization localization : template.getLocalizations()) {

                if (locale.equals(localization.getLocaleIso())) {
                    return localization;
                }
            }
        }

        return template.getLocalizations().get(0);
    }
}
