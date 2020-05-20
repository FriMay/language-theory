package arbina.sps.config;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Configuration
public class ApnsClientConfig {

    @Bean
    public ApnsClient apnsClient() throws NoSuchAlgorithmException, InvalidKeyException, IOException {

        //TODO edit APNS config path
        InputStream is = new FileInputStream(new File("input_stream_path"));

        //TODO edit team_id and key_id
        return new ApnsClientBuilder()
                .setSigningKey(ApnsSigningKey.loadFromInputStream(is, "team_id", "key_id"))
                .setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                .build();
    }
}
