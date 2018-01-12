package be.ugent.idlab.tcbl.userdatamanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

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
		}
	}

}
