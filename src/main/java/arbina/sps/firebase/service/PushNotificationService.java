package arbina.sps.firebase.service;

import arbina.sps.store.entity.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class PushNotificationService {

    private final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    private final FCMService fcmService;

    public PushNotificationService(FCMService fcmService) {
        this.fcmService = fcmService;
    }

    public void sendPushNotification(Template template) {

        try {
            fcmService.sendMessage(template);
        } catch (InterruptedException | ExecutionException e) {
            logger.error(e.getMessage());
        }
    }

}
