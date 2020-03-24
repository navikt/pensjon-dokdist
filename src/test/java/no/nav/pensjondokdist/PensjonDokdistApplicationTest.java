package no.nav.pensjondokdist;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class PensjonDokdistApplicationTest {


	@Configuration
	static class ClientConfig {

		@Bean
		OAuth2AuthorizedClientRepository authorizedClients() {
			return new HttpSessionOAuth2AuthorizedClientRepository();
		}
	}

	@Test
	public void contextLoads() {
	}
}
