package arbina.app.template;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "arbina")
@DataJpaTest
public class BaseWebContext {

}