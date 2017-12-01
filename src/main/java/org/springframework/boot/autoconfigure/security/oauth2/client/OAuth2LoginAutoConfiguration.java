package org.springframework.boot.autoconfigure.security.oauth2.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.Set;

import static org.springframework.boot.autoconfigure.security.oauth2.client.ClientRegistrationAutoConfiguration.CLIENT_PROPERTY_PREFIX;
import static org.springframework.boot.autoconfigure.security.oauth2.client.ClientRegistrationAutoConfiguration.resolveClientPropertyKeys;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(EnableWebSecurity.class)
@ConditionalOnMissingBean(WebSecurityConfiguration.class)
@ConditionalOnBean(ClientRegistrationRepository.class)
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@AutoConfigureAfter(ClientRegistrationAutoConfiguration.class)
public class OAuth2LoginAutoConfiguration {
	private static final String USER_INFO_URI_PROPERTY = "user-info-uri";
	private static final String USER_NAME_ATTR_NAME_PROPERTY = "user-name-attribute-name";
	private static final String CLIENT_ID_PROPERTY = "client-id";

	@EnableWebSecurity
	@Order(10)
	protected static class OAuth2LoginSecurityConfiguration extends WebSecurityConfigurerAdapter {
		private final Environment environment;

		protected OAuth2LoginSecurityConfiguration(Environment environment) {
			this.environment = environment;
		}

		@Autowired
		private ClientRegistrationRepository clientRegistrationRepository;

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.authorizeRequests()
					.antMatchers(
							// static contents:
							"/favicon.ico",
							"/css/*",
							"/assets/*",
							"/oiclogin",
							"/login",
							"/login/*",
							// mappings under /
							"/",
							"/index",
							"/loginrequired",
							"/services",
							// mappings under /user
							"/user/register",
							"/user/registered",
							"/user/confirm/*",
							"/user/resetpw",
							"/user/resetpwform",
							"/user/resetpwform/*",
							"/user/passwordset",
							// mappings under /stats
							"/stats/users"
							// mappings under /stats
							//not at this time: "/rest/*"
							).permitAll()
					.anyRequest().authenticated()
					.and()
						.oauth2Login()
						.clientRegistrationRepository(this.clientRegistrationRepository)
						.authorizedClientService(this.authorizedClientService())
					.and()
					// next exceptionHandling replaces .oauth2Login()
					// see also https://spring.io/guides/tutorials/spring-boot-oauth2/, "Unauthenticated users are re-directed to the home page"
					// see also https://github.com/spring-projects/spring-security-oauth/issues/786
					.exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/loginrequired"))
			;

			//this.registerUserNameAttributeNames(http.oauth2Login());
		}

		@Bean
		public OAuth2AuthorizedClientService authorizedClientService() {
			return new InMemoryOAuth2AuthorizedClientService(this.clientRegistrationRepository);
		}

		private void registerUserNameAttributeNames(OAuth2LoginConfigurer<HttpSecurity> oauth2LoginConfigurer) throws Exception {
			Set<String> clientPropertyKeys = resolveClientPropertyKeys(this.environment);
			for (String clientPropertyKey : clientPropertyKeys) {
				String fullClientPropertyKey = CLIENT_PROPERTY_PREFIX + "." + clientPropertyKey;
				if (!this.environment.containsProperty(fullClientPropertyKey + "." + CLIENT_ID_PROPERTY)) {
					continue;
				}
				String userInfoUriValue = this.environment.getProperty(fullClientPropertyKey + "." + USER_INFO_URI_PROPERTY);
				String userNameAttributeNameValue = this.environment.getProperty(fullClientPropertyKey + "." + USER_NAME_ATTR_NAME_PROPERTY);
				/*if (userInfoUriValue != null && userNameAttributeNameValue != null) {
					oauth2LoginConfigurer.userInfoEndpoint().userNameAttributeName(userNameAttributeNameValue, URI.create(userInfoUriValue));
				} */
			}
		}
	}
}
