package arbina.sps.api.services;

import arbina.infra.exceptions.BadRequestException;
import arbina.sps.store.entity.Client;
import arbina.sps.store.entity.DeviceToken;
import arbina.sps.store.entity.Template;
import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.ApnsPushNotification;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.BatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Service
public class PushNotificationService {

    private final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    private final FcmService fcmService;

    private final ApnsService apnsService;

    private static volatile Map<String, ApnsClient> apnsClients = new HashMap<>();

    public PushNotificationService(FcmService fcmService, ApnsService apnsService) {
        this.fcmService = fcmService;
        this.apnsService = apnsService;
    }

    public void sendPushNotification(Template template,
                                     Client client) {

        Stream<DeviceToken> deviceTokenStream = client.getTokens().stream();

        if (client.getFcm() != null) {

            FirebaseApp app = initOrGetFirebaseApp(client);

            List<ApiFuture<BatchResponse>> androidFutureResponses =
                    fcmService.sendMessage(template, deviceTokenStream, app);

            androidFutureResponses.forEach((future) -> {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
        } else {

            ApnsClient apnsClient = initOrGetApnsClient(client);

            List<PushNotificationFuture<ApnsPushNotification, PushNotificationResponse<ApnsPushNotification>>>
                    appleFutureResponses = apnsService
                    .sendMessage(template, deviceTokenStream, apnsClient, client.getTopic());

            appleFutureResponses.forEach(CompletableFuture::join);
        }
    }

    private FirebaseApp initOrGetFirebaseApp(Client client) {

        FirebaseApp app;

        try {

            app = FirebaseApp.getInstance(client.getClientId());

        } catch (IllegalStateException ise) {

            try {

                InputStream is = new ByteArrayInputStream(client.getFcm().getConfig().getBytes());

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(is))
                        .build();

                return FirebaseApp.initializeApp(options, client.getClientId());

            } catch (IOException ioe) {

                throw new BadRequestException(String.format("FCM client configuration " +
                        "with name \"%s\" can't be exist.", client.getClientId()));

            }
        }

        return app;
    }

    private ApnsClient initOrGetApnsClient(Client client) {

        ApnsClient apnsClient = apnsClients.get(client.getClientId());

        if (apnsClient == null) {

            try {

                InputStream is = new ByteArrayInputStream(client.getApns().getApnsCertificate().getBytes());

                apnsClient = new ApnsClientBuilder()
                        .setSigningKey(ApnsSigningKey.loadFromInputStream(is, client.getApns().getTeamId(), client.getApns().getKeyId()))
                        .setApnsServer(client.getApns().getIsDevGate()
                                ? ApnsClientBuilder.DEVELOPMENT_APNS_HOST
                                : ApnsClientBuilder.PRODUCTION_APNS_HOST)
                        .build();

                apnsClients.put(client.getClientId(), apnsClient);

            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {

                throw new BadRequestException(String.format("APNS client configuration " +
                        "with name \"%s\" can't be exist.", client.getClientId()));

            }
        }

        return apnsClient;
    }
}
