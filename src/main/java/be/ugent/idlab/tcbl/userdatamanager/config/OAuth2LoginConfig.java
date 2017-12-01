package be.ugent.idlab.tcbl.userdatamanager.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.ArrayList;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Configuration
public class OAuth2LoginConfig {

	@EnableWebSecurity
	protected static class OAuth2LoginSecurityConfiguration extends WebSecurityConfigurerAdapter {
		private Environment environment;

		public OAuth2LoginSecurityConfiguration(Environment environment) {
			this.environment = environment;
		}

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
					.and()
					// next exceptionHandling replaces .oauth2Login()
					// see also https://spring.io/guides/tutorials/spring-boot-oauth2/, "Unauthenticated users are re-directed to the home page"
					// see also https://github.com/spring-projects/spring-security-oauth/issues/786
					.exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/loginrequired"))
			;

			//this.registerUserNameAttributeNames(http.oauth2Login());

		}

		@Bean
		public ClientRegistrationRepository clientRegistrationRepository() {
			return new InMemoryClientRegistrationRepository(this.oidcClientRegistration());
		}

		private ClientRegistration oidcClientRegistration() {
			Binder binder = Binder.get(this.environment);
			OAuth2ClientProperties clientProperties = binder.bind(
					"spring.security.oauth2.client", Bindable.of(OAuth2ClientProperties.class)).get();

			// NOTE: in this case, we assume only one client registration at a time, since there is only one production server!
			OAuth2ClientProperties.Registration registrationProperties = clientProperties.getRegistration().values().iterator().next();
			OAuth2ClientProperties.Provider providerProperties = clientProperties.getProvider().get(registrationProperties.getProvider());
			ClientRegistration.Builder clientRegistrationBuilder = ClientRegistration.withRegistrationId(registrationProperties.getClientId());
			return clientRegistrationBuilder
					.clientId(registrationProperties.getClientId())
					.clientName(registrationProperties.getClientName())
					.clientSecret(registrationProperties.getClientSecret())
					.authorizationGrantType(new AuthorizationGrantType(registrationProperties.getAuthorizationGrantType()))
					.clientAuthenticationMethod(new ClientAuthenticationMethod(registrationProperties.getClientAuthenticationMethod()))
					.scope(new ArrayList<>(registrationProperties.getScope()).toArray(new String[registrationProperties.getScope().size()]))
					.redirectUriTemplate(/*registrationProperties.getRedirectUri()*/"{baseUrl}/login/oauth2/code/{registrationId}")

					.authorizationUri(providerProperties.getAuthorizationUri())
					.jwkSetUri(providerProperties.getJwkSetUri())
					.tokenUri(providerProperties.getTokenUri())
					.userInfoUri(providerProperties.getUserInfoUri())
					//.userNameAttributeName()
					.build();
		}

	}

}
