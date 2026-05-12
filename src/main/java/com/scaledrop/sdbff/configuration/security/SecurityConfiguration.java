package com.scaledrop.sdbff.configuration.security;

import static com.scaledrop.sdbff.configuration.Constants.API_V1_PREFIX;
import static com.scaledrop.sdbff.configuration.security.role.ApiUserRole.DOCUMENTATION;
import static com.scaledrop.sdbff.configuration.security.role.ApiUserRole.INTERNAL;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.crypto.factory.PasswordEncoderFactories.createDelegatingPasswordEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scaledrop.sdbff.configuration.exception.SdBffServiceException;
import com.scaledrop.sdbff.configuration.security.properties.SecurityProperties;
import com.scaledrop.sdbff.configuration.security.properties.SecurityProperties.BasicAuthorization;
import java.util.Optional;
import java.util.function.Function;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final ObjectMapper objectMapper;

  @Configuration
  @RequiredArgsConstructor
  public static class AuthSecurityConfiguration {

    private static final String EXTRACTING_API_CREDENTIALS_ERROR_MESSAGE =
        "Error extracting API credentials";

    private static final String[] DOCS_PATHS = {
      "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**"
    };
    private static final String[] ACTUATOR_PATHS = {"/actuator/**"};
    private final SecurityProperties securityProperties;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomBasicAuthenticationEntryPoint basicAuthenticationEntryPoint;

    @Bean
    @Order(0)
    public SecurityFilterChain securityFilterChainActuator(HttpSecurity http) throws Exception {
      return http.securityMatcher(ACTUATOR_PATHS)
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
          .csrf(AbstractHttpConfigurer::disable) // NOSONAR
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChainDocs(HttpSecurity http) throws Exception {
      return http.securityMatcher(DOCS_PATHS)
          .authorizeHttpRequests(r -> r.requestMatchers(DOCS_PATHS).hasRole(DOCUMENTATION.name()))
          .httpBasic(
              customizer -> customizer.authenticationEntryPoint(basicAuthenticationEntryPoint))
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .exceptionHandling(eh -> eh.accessDeniedHandler(customAccessDeniedHandler))
          .build();
    }

    @Bean
    @Order(2)
    protected SecurityFilterChain securityFilterChainEndpoints(HttpSecurity http) throws Exception {
      return http.cors(AbstractHttpConfigurer::disable)
          .csrf(AbstractHttpConfigurer::disable)
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .authorizeHttpRequests(
              r ->
                  r.requestMatchers(POST, API_V1_PREFIX + "/session/login")
                      .permitAll()
                      .requestMatchers(API_V1_PREFIX + "/accounts/**")
                      .hasRole("USER")
                      .requestMatchers(POST, API_V1_PREFIX + "/upload/**")
                      .hasRole("USER")
                      .requestMatchers(GET, API_V1_PREFIX + "/download/**")
                      .hasRole("USER")
                      .anyRequest()
                      .authenticated())
          .oauth2ResourceServer(
              oauth2 ->
                  oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
          .exceptionHandling(
              eh ->
                  eh.accessDeniedHandler(customAccessDeniedHandler)
                      .authenticationEntryPoint(basicAuthenticationEntryPoint))
          .build();
    }

    @Autowired
    private void configureInMemoryAuthentication(AuthenticationManagerBuilder auth)
        throws Exception {
      BasicAuthorization documentation = securityProperties.getDocumentation();
      BasicAuthorization internal = securityProperties.getInternal();

      auth.inMemoryAuthentication()
          .withUser(getUsername(documentation))
          .password(getEncodedPassword(documentation))
          .roles(DOCUMENTATION.name())
          .and()
          .withUser(getUsername(internal))
          .password(getEncodedPassword(internal))
          .roles(INTERNAL.name());
    }

    private String getUsername(BasicAuthorization basicAuthorization) {
      return getCredential(basicAuthorization, BasicAuthorization::getUsername);
    }

    private String getEncodedPassword(BasicAuthorization basicAuthorization) {
      return createDelegatingPasswordEncoder()
          .encode(getCredential(basicAuthorization, BasicAuthorization::getPassword));
    }

    private String getCredential(
        BasicAuthorization basicAuthorization,
        Function<BasicAuthorization, String> credentialExtractor) {
      return Optional.ofNullable(basicAuthorization)
          .map(credentialExtractor)
          .orElseThrow(() -> new SdBffServiceException(EXTRACTING_API_CREDENTIALS_ERROR_MESSAGE));
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
      JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
      authoritiesConverter.setAuthorityPrefix("");
      authoritiesConverter.setAuthoritiesClaimName("role");

      JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
      converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
      return converter;
    }
  }

  @Bean
  public CustomAccessDeniedHandler customAccessDeniedHandler() {
    return new CustomAccessDeniedHandler(objectMapper);
  }

  @Bean
  public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String secret) {
    byte[] bytes = secret.getBytes();
    SecretKey spec = new SecretKeySpec(bytes, "HmacSHA256");

    NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(spec).build();

    return jwtDecoder;
  }
}
