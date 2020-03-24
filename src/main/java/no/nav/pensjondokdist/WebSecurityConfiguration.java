package no.nav.pensjondokdist;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .mvcMatchers("/api/internal/isAlive").permitAll()
                .mvcMatchers("/api/internal/isReady").permitAll()
                .anyRequest().authenticated()
                .and().oauth2Login()
                .and().csrf().disable().cors();
    }
}
