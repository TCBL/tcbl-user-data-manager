package org.springframework.boot.autoconfigure.security.oauth2.client;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

//import org.springframework.security.oauth2.client.registration.ClientRegistrationProperties;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(ClientRegistrationRepository.class)
//@ConditionalOnMissingBean(ClientRegistrationRepository.class)
@AutoConfigureBefore(SecurityAutoConfiguration.class)
public class ClientRegistrationAutoConfiguration {
	private static final String CLIENT_ID_PROPERTY = "client-id";
	//private static final String CLIENTS_DEFAULTS_RESOURCE = "META-INF/oauth2-clients-defaults.yml";
	public static final String CLIENT_PROPERTY_PREFIX = "spring.security.oauth2.client.registration";

	@Configuration
	@Conditional(ClientPropertiesAvailableCondition.class)
	protected static class ClientRegistrationConfiguration {
		private final Environment environment;

		protected ClientRegistrationConfiguration(Environment environment) {
			this.environment = environment;
		}

		@Bean
		public ClientRegistrationRepository clientRegistrationRepository() {
			//MutablePropertySources propertySources = ((ConfigurableEnvironment) this.environment).getPropertySources();
			/*Properties clientsDefaultProperties = /*this.getClientsDefaultProperties() null;
			if (clientsDefaultProperties != null) {
				propertySources.addLast(new PropertiesPropertySource("oauth2ClientsDefaults", clientsDefaultProperties));
			}*/
			Binder binder = Binder.get(this.environment);
			OAuth2ClientProperties clientProperties = binder.bind(
					"spring.security.oauth2.client", Bindable.of(OAuth2ClientProperties.class)).get();

			// NOTE: in this case, we assume only one client registration at a time, since there is only one production server!
			OAuth2ClientProperties.Registration registrationProperties = clientProperties.getRegistration().values().iterator().next();
			OAuth2ClientProperties.Provider providerProperties = clientProperties.getProvider().get(registrationProperties.getProvider());
			ClientRegistration.Builder clientRegistrationBuilder = ClientRegistration.withRegistrationId(registrationProperties.getClientId());
			ClientRegistration clientRegistration = clientRegistrationBuilder
					.clientId(registrationProperties.getClientId())
					.clientName(registrationProperties.getClientName())
					.clientSecret(registrationProperties.getClientSecret())
					.authorizationGrantType(new AuthorizationGrantType(registrationProperties.getAuthorizationGrantType()))
					.clientAuthenticationMethod(new ClientAuthenticationMethod(registrationProperties.getClientAuthenticationMethod()))
					.scope(new ArrayList<>(registrationProperties.getScope()).toArray(new String[registrationProperties.getScope().size()]))
					.redirectUriTemplate(registrationProperties.getRedirectUri())

					.authorizationUri(providerProperties.getAuthorizationUri())
					.jwkSetUri(providerProperties.getJwkSetUri())
					.tokenUri(providerProperties.getTokenUri())
					.userInfoUri(providerProperties.getUserInfoUri())
					//.userNameAttributeName()
					.build();


			/*List<ClientRegistration> clientRegistrations = new ArrayList<>();
			Set<String> clientPropertyKeys = resolveClientPropertyKeys(this.environment);
			for (String clientPropertyKey : clientPropertyKeys) {
				String fullClientPropertyKey = CLIENT_PROPERTY_PREFIX + "." + clientPropertyKey;
				if (!this.environment.containsProperty(fullClientPropertyKey + "." + CLIENT_ID_PROPERTY)) {
					continue;
				}
				OAuth2ClientProperties clientProperties = binder.bind(
						"spring.security.oauth2.client", Bindable.of(OAuth2ClientProperties.class)).get();
				//clientProperties.getRegistration().values()
				ClientRegistration.Builder clientRegistrationBuilder = ClientRegistration.withRegistrationId(clientPropertyKey);


				//ClientRegistration.Builder clientRegistrationBuilder = ClientRegistration.withRegistrationId(clientPropertyKey);
				//clientRegistrationBuilder

				/*lientRegistrationProperties clientRegistrationProperties = binder.bind(
						fullClientPropertyKey, Bindable.of(ClientRegistrationProperties.class)).get();
				ClientRegistration clientRegistration = new ClientRegistration.Builder(clientRegistrationProperties).build();*/
				//clientRegistrations.add(clientRegistration);
			//}

			return new InMemoryClientRegistrationRepository(clientRegistration);
		}

		@Bean
		public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
			return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
		}

		/*private Properties getClientsDefaultProperties() {
			ClassPathResource clientsDefaultsResource = new ClassPathResource(CLIENTS_DEFAULTS_RESOURCE);
			if (!clientsDefaultsResource.exists()) {
				return null;
			}
			YamlPropertiesFactoryBean yamlPropertiesFactory = new YamlPropertiesFactoryBean();
			yamlPropertiesFactory.setResources(clientsDefaultsResource);
			return yamlPropertiesFactory.getObject();
		}*/
	}

	public static Set<String> resolveClientPropertyKeys(Environment environment) {
		Binder binder = Binder.get(environment);
		BindResult<Map<String, Object>> result = binder.bind(
				CLIENT_PROPERTY_PREFIX, Bindable.mapOf(String.class, Object.class));
		return result.get().keySet();
	}

	private static class ClientPropertiesAvailableCondition extends SpringBootCondition implements ConfigurationCondition {

		@Override
		public ConfigurationCondition.ConfigurationPhase getConfigurationPhase() {
			return ConfigurationPhase.PARSE_CONFIGURATION;
		}

		@Override
		public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
			ConditionMessage.Builder message = ConditionMessage.forCondition("OAuth2 Client Properties");
			Set<String> clientPropertyKeys = resolveClientPropertyKeys(context.getEnvironment());
			if (!CollectionUtils.isEmpty(clientPropertyKeys)) {
				return ConditionOutcome.match(message.foundExactly("OAuth2 Client(s) -> " +
						clientPropertyKeys.stream().collect(Collectors.joining(", "))));
			}
			return ConditionOutcome.noMatch(message.notAvailable("OAuth2 Client(s)"));
		}
	}
}
