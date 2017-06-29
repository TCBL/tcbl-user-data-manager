package org.springframework.boot.autoconfigure.security.oauth2.client;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.user.converter.AbstractOAuth2UserConverter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.Set;
import java.util.function.Function;

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
	private static final String USER_INFO_CONVERTER_PROPERTY = "user-info-converter";
	private static final String USER_INFO_NAME_ATTR_KEY_PROPERTY = "user-info-name-attribute-key";
	private static final String CLIENT_ID_PROPERTY = "client-id";

	@EnableWebSecurity
	protected static class OAuth2LoginSecurityConfiguration extends WebSecurityConfigurerAdapter {
		private final Environment environment;

		protected OAuth2LoginSecurityConfiguration(Environment environment) {
			this.environment = environment;
		}

		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http
					.authorizeRequests()
					.antMatchers("/favicon.ico").permitAll()
					.anyRequest().authenticated()
					.and()
					.oauth2Login();

			this.registerUserInfoTypeConverters(http.oauth2Login());
		}

		private void registerUserInfoTypeConverters(OAuth2LoginConfigurer<HttpSecurity> oauth2LoginConfigurer) throws Exception {
			Set<String> clientPropertyKeys = resolveClientPropertyKeys(this.environment);
			for (String clientPropertyKey : clientPropertyKeys) {
				String fullClientPropertyKey = CLIENT_PROPERTY_PREFIX + "." + clientPropertyKey;
				if (!this.environment.containsProperty(fullClientPropertyKey + "." + CLIENT_ID_PROPERTY)) {
					continue;
				}
				String userInfoUriValue = this.environment.getProperty(fullClientPropertyKey + "." + USER_INFO_URI_PROPERTY);
				String userInfoConverterTypeValue = this.environment.getProperty(fullClientPropertyKey + "." + USER_INFO_CONVERTER_PROPERTY);
				if (userInfoUriValue != null && userInfoConverterTypeValue != null) {
					Class<? extends Function> userInfoConverterType = ClassUtils.resolveClassName(
							userInfoConverterTypeValue, this.getClass().getClassLoader()).asSubclass(Function.class);
					Function<ClientHttpResponse, ? extends OAuth2User> userInfoConverter = null;
					if (AbstractOAuth2UserConverter.class.isAssignableFrom(userInfoConverterType)) {
						Constructor<? extends Function> oauth2UserConverterConstructor = ClassUtils.getConstructorIfAvailable(userInfoConverterType, String.class);
						if (oauth2UserConverterConstructor != null) {
							String userInfoNameAttributeKey = this.environment.getProperty(fullClientPropertyKey + "." + USER_INFO_NAME_ATTR_KEY_PROPERTY);
							userInfoConverter = (Function<ClientHttpResponse, ? extends OAuth2User>)oauth2UserConverterConstructor.newInstance(userInfoNameAttributeKey);
						}
					}
					if (userInfoConverter == null) {
						userInfoConverter = (Function<ClientHttpResponse, ? extends OAuth2User>)userInfoConverterType.newInstance();
					}
					oauth2LoginConfigurer.userInfoEndpoint().userInfoTypeConverter(userInfoConverter, new URI(userInfoUriValue));
				}
			}
		}
	}
}
