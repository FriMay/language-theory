package arbina.sps;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;

@EnableFeignClients("arbina.infra.services")
@ImportAutoConfiguration({ FeignAutoConfiguration.class })
public class FeignConfiguration {
}
