package arbina.sps.api.services;

import arbina.sps.store.DeviceTokenType;
import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.entity.Template;
import arbina.sps.store.repository.DeviceTokenRepository;
import com.eatthepath.pushy.apns.ApnsPushNotification;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.BatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Service
public class PushNotificationService {

    private final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    private final FcmService fcmService;

    private final ApnsService apnsService;

    private final DeviceTokenRepository deviceTokenRepository;
    
    public PushNotificationService(FcmService fcmService, ApnsService apnsService, DeviceTokenRepository deviceTokenRepository) {
        this.fcmService = fcmService;
        this.apnsService = apnsService;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void sendPushNotification(Template template) {

        Stream<DeviceToken> androidTokenStream = deviceTokenRepository.fetchDeviceTokenStreamByType(DeviceTokenType.FCM.name());

        List<ApiFuture<BatchResponse>> androidFutureResponses = fcmService.sendMessage(template, androidTokenStream);

        Stream<DeviceToken> iosTokenStream = deviceTokenRepository.fetchDeviceTokenStreamByType(DeviceTokenType.IOS.name());

        List<PushNotificationFuture<ApnsPushNotification, PushNotificationResponse<ApnsPushNotification>>> appleFutureResponses
                = apnsService.sendMessage(template, iosTokenStream);

        androidFutureResponses.forEach((future)->{
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        appleFutureResponses.forEach(CompletableFuture::join);

    }

}
