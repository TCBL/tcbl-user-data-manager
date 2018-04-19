package be.ugent.idlab.tcbl.userdatamanager.config;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.MyAuthenticationDetails;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import java.util.Collection;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@Configuration
public class OAuth2LoginConfig {

	@EnableWebSecurity
	protected static class OAuth2LoginSecurityConfiguration extends WebSecurityConfigurerAdapter {

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
							"/logout",
							"/gluulogout",
							// mappings under /
							"/",
							"/index",
							"/loginrequired",
							"/services",
							"/error",	// needed for @ExceptionHandler
							// mappings under /user
							"/user/register",
							"/user/registered",
							"/user/confirm/*",
							"/user/resetpw",
							"/user/resetpwform",
							"/user/resetpwform/*",
							"/user/passwordset",
							// mappings under /p
							"/p/**",
							// mappings under /stats
							"/stats/users"
							// mappings under /rest
							//not at this time: "/rest/*"
					).permitAll()
					.anyRequest().authenticated()
					.and()
					.oauth2Login()
					.and()
					.logout()
						.logoutSuccessUrl("/index")
						.logoutSuccessHandler((request, response, authentication) -> {
							Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
							String endSessionUri = null;
							for (GrantedAuthority authority : authorities) {
								if (authority instanceof OidcUserAuthority) {
									OidcIdToken idToken = ((OidcUserAuthority) authority).getIdToken();
									String idTokenStr = idToken.getTokenValue();
									String redirecturl = request.getRequestURL().toString().replace("/logout", "");
									// compose end session uri
									String op = ((DefaultOidcUser) authentication.getPrincipal()).getAttributes().get("iss").toString();
									String userName;
									try {
										// See UserController.userinfo() to see how the authentication details were set
										MyAuthenticationDetails myAuthenticationDetails = (MyAuthenticationDetails) authentication.getDetails();
										userName = myAuthenticationDetails.getUserName();
									} catch (Exception e) {
										userName = "";
									}
									endSessionUri = "/gluulogout?op=" + op +"&id_token_hint=" + idTokenStr + "&post_logout_redirect_uri=" + redirecturl + "&un=" + userName;
								}
							}
							String targetUrl = endSessionUri != null ? endSessionUri : "/index";
							request.getRequestDispatcher(targetUrl).forward(request, response);
						})
					.and()
					// next exceptionHandling replaces .oauth2Login()
					// see also https://spring.io/guides/tutorials/spring-boot-oauth2/, "Unauthenticated users are re-directed to the home page"
					// see also https://github.com/spring-projects/spring-security-oauth/issues/786
					.exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/loginrequired"))
			;
		}
	}

}
