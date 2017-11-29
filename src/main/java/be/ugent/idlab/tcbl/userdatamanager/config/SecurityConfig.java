package be.ugent.idlab.tcbl.userdatamanager.config;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.ClientRegistrationAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
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
@EnableWebSecurity
//@ConditionalOnClass(EnableWebSecurity.class)
//@ConditionalOnMissingBean(WebSecurityConfiguration.class)
@ConditionalOnBean(ClientRegistrationRepository.class)
@AutoConfigureBefore(SecurityAutoConfiguration.class)
@AutoConfigureAfter(ClientRegistrationAutoConfiguration.class)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String CLIENT_ID_PROPERTY = "client-id";
	private static final String USER_INFO_URI_PROPERTY = "user-info-uri";
	private static final String USER_NAME_ATTR_NAME_PROPERTY = "user-name-attribute-name";
	private final Environment environment;

	public SecurityConfig(Environment environment) {
		this.environment = environment;
	}


	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
				.antMatchers(
						"/favicon.ico",
						"/css/*",
						"/assets/*",
						"/oiclogin",
						"/loginrequired",
						"/",
						"/index",
						"/user/register",
						"/user/registered",
						"/user/confirm/*",
						"/user/resetpw",
						"/user/resetpwform",
						"/user/resetpwform/*",
						"/user/passwordset",
						"/stats/users",
						"/rest/*"
				).permitAll()
				.anyRequest().authenticated()
				.and()
				// next exceptionHandling replaces .oauth2Login()
				// see also https://spring.io/guides/tutorials/spring-boot-oauth2/, "Unauthenticated users are re-directed to the home page"
				// see also https://github.com/spring-projects/spring-security-oauth/issues/786
				.exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/loginrequired"))
		;
		this.registerUserNameAttributeNames(http.oauth2Login());
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
				if (userInfoUriValue != null && userNameAttributeNameValue != null) {
					//oauth2LoginConfigurer.userInfoEndpoint().userNameAttributeName(userNameAttributeNameValue, URI.create(userInfoUriValue));
					System.out.println("boe");
				}
		}
	}
}
