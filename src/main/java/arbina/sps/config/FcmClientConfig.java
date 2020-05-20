package arbina.sps.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;

@Service
public class FcmClientConfig {

    @PostConstruct
    public void initialize() throws IOException {

        //TODO edit credential path
        InputStream inputStream = new FileInputStream(new File("input_stream_path"));

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(inputStream))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {

            FirebaseApp.initializeApp(options);

        }


    }

}