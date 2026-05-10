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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
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
          .requestCache(AbstractHttpConfigurer::disable)
          .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
          .logout(AbstractHttpConfigurer::disable)
          .anonymous(Customizer.withDefaults())
          .authorizeHttpRequests(
              r ->
                  r.requestMatchers(POST, API_V1_PREFIX + "/session/login")
                      .permitAll()
                      .requestMatchers(API_V1_PREFIX + "/accounts/**")
                      .hasAnyAuthority(
                          "ROLE_INTERNAL", "SCOPE_openid", "SCOPE_email", "SCOPE_profile")
                      .requestMatchers(POST, API_V1_PREFIX + "/uploads/**")
                      .hasAnyAuthority(
                          "ROLE_INTERNAL", "SCOPE_openid", "SCOPE_email", "SCOPE_profile")
                      .requestMatchers(GET, API_V1_PREFIX + "/download/**")
                      .hasAnyAuthority(
                          "ROLE_INTERNAL", "SCOPE_openid", "SCOPE_email", "SCOPE_profile")
                      .anyRequest()
                      .authenticated())
          .httpBasic(
              customizer -> customizer.authenticationEntryPoint(basicAuthenticationEntryPoint))
          .oauth2ResourceServer(
              oauth2 ->
                  oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
          .exceptionHandling(eh -> eh.accessDeniedHandler(customAccessDeniedHandler))
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
      authoritiesConverter.setAuthorityPrefix("ROLE_");
      authoritiesConverter.setAuthoritiesClaimName("roles");

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
  public JwtDecoder jwtDecoder(
      @Value("${security.oauth2.client.registration.google.issuer-uri}") String issuerUri,
      @Value("${security.oauth2.client.registration.google.client-id}") String clientId) {

    // Initialize decoder
    NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

    // Verify audience
    OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(clientId);
    // Verify issuer
    OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);

    OAuth2TokenValidator<Jwt> withAudience =
        new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
    jwtDecoder.setJwtValidator(withAudience);

    return jwtDecoder;
  }

  static class AudienceValidator implements OAuth2TokenValidator<Jwt> {
    private final String audience;

    AudienceValidator(String audience) {
      this.audience = audience;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
      if (jwt.getAudience() != null && jwt.getAudience().contains(audience)) {
        return OAuth2TokenValidatorResult.success();
      }
      return OAuth2TokenValidatorResult.failure(
          new OAuth2Error("invalid_token", "Error while verifying token: wrong audience", null));
    }
  }
}
