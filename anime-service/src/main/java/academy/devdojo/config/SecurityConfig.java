package academy.devdojo.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Log4j2
@EnableMethodSecurity
public class SecurityConfig {

  private static final String[] WHITE_LIST = {"/swagger-ui.html", "/v3/**", "/swagger-ui/**", "/csrf"};

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    var user = User.withUsername("takamura")
        .password(passwordEncoder.encode("ippo"))
        .roles("USER")
        .build();

    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Cross Site Request Forgery
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(WHITE_LIST).permitAll()
            .requestMatchers("/v1/animes/**").hasRole("USER")
            .requestMatchers("/v1/producers/**").hasRole("USER")
            .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()
            .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .build();
  }
}
