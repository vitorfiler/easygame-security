package br.com.una.easygamesecurity.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import javax.servlet.http.HttpServletResponse;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("authenticationManagerBean")
    private AuthenticationManager authenticationManager;

    public AuthorizationServerConfig(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        //TODO: parametrizar
        int accessTokenValidity = 24 * 60 * 60; //5 * 60;
        int refreshTokenValidity = 24 * 60 * 60; //7 * 24 * 60 * 60;

        clients.inMemory()
            .withClient("web_app")
            .secret(passwordEncoder.encode("changeit"))
            .scopes("openid")
            .autoApprove(true)
            .authorizedGrantTypes("implicit","refresh_token","password","authorization_code")
            .accessTokenValiditySeconds(accessTokenValidity)
            .refreshTokenValiditySeconds(refreshTokenValidity)
        .and()
            .withClient("internal")
            .secret(passwordEncoder.encode("internal"))
            .scopes("web-app")
            .authorities("ROLE_ADMIN")
            .autoApprove(true)
            .authorizedGrantTypes("client_credentials")
            .accessTokenValiditySeconds(accessTokenValidity)
            .refreshTokenValiditySeconds(refreshTokenValidity);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        Collection<TokenEnhancer> tokenEnhancers = applicationContext.getBeansOfType(TokenEnhancer.class).values();
        TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(new ArrayList<>(tokenEnhancers));
        endpoints
            .authenticationManager(authenticationManager)
            .tokenStore(tokenStore())
            .tokenEnhancer(tokenEnhancerChain)
            .reuseRefreshTokens(false);
    }

    @Bean
    public JwtTokenStore tokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter());
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        KeyPair keyPair = new KeyStoreKeyFactory(
            new ClassPathResource("config/tls/keystore.p12"), "password".toCharArray())
            .getKeyPair("selfsigned");
        converter.setKeyPair(keyPair);
        return converter;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security
            .tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()");
    }

    @EnableResourceServer
    public static class ResourceServerConfig extends ResourceServerConfigurerAdapter {

        private final TokenStore tokenStore;

        private final CorsFilter corsFilter;

        public ResourceServerConfig(TokenStore tokenStore, CorsFilter corsFilter) {
            this.tokenStore = tokenStore;
            this.corsFilter = corsFilter;
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            .and()
                .csrf()
                .disable()
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .headers()
                .frameOptions()
                .disable()
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers("/api/register").permitAll()
                .antMatchers("/api/activate").permitAll()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers("/api/account/reset-password/init").permitAll()
                .antMatchers("/api/account/reset-password/finish").permitAll()
                .antMatchers("/api/**").authenticated()
                .antMatchers("/management/health").permitAll()
                .antMatchers("/management/**").hasAuthority("ROLE_ADMIN")
                .antMatchers("/v2/api-docs/**").permitAll()
                .antMatchers("/swagger-resources/configuration/ui").permitAll()
                .antMatchers("/swagger-ui/index.html").hasAuthority("ROLE_ADMIN");
        }

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
            resources.resourceId("easygame-security").tokenStore(tokenStore);
        }
    }
}
