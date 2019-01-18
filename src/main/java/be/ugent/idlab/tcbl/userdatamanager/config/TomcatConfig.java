package be.ugent.idlab.tcbl.userdatamanager.config;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

/**
 * See https://github.com/spring-projects/spring-boot/issues/1647
 *
 * Adds an AJP connector (and thus endpoint) to the embedded Tomcat servlet container.
 * This enables efficient cooperation with the Apache HTTP server configured as reverse proxy.
 *
 * @author Gerald Haesendonck
 */
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class TomcatConfig {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private boolean ajpEnabled = false;
	private int port;
	private String scheme;
	private String proxyName;
	private int proxyPort;
	private boolean secure;

	public TomcatConfig(Environment environment) {
		// check if ajp enabled; the port *should* be configured!
		String ajp = environment.getProperty("server.ajp.port");
		if (ajp != null) {
			ajpEnabled = true;
			port = Integer.parseInt(environment.getProperty("server.ajp.port", "-1"));
			scheme = environment.getProperty("server.ajp.scheme");
			proxyName = environment.getProperty("server.ajp.proxy-name");
			proxyPort = Integer.parseInt(environment.getProperty("server.ajp.proxy-port", "-1"));
			secure = Boolean.parseBoolean(environment.getProperty("server.ajp.secure", "false"));
		} else {
			log.info("No property server.ajp.port found, AJP will not be enabled.");
		}
	}

	@ConditionalOnClass(value = ServerProperties.Tomcat.class)
	@Bean
	public ServletWebServerFactory servletWebServerFactory() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		if (log.isDebugEnabled()) {
			log.debug(this.toString());
		}
		if (ajpEnabled) {
			log.info("AJP configuration found. Enabling AJP/1.3");
			final Connector ajpConnector = new Connector("AJP/1.3");
			ajpConnector.setPort(port);
			if (scheme != null) {
				ajpConnector.setScheme(scheme);
			}
			if (proxyName != null) {
				ajpConnector.setProxyName(proxyName);
			}
			if (proxyPort != -1) {
				ajpConnector.setProxyPort(proxyPort);
			}
			ajpConnector.setSecure(secure);
			tomcat.addAdditionalTomcatConnectors(ajpConnector);
		} else {
			log.info("No AJP configuration found.");
		}
		return tomcat;

	}

	@Override
	public String toString() {
		return "TomcatConfig{" +
				"ajpEnabled=" + ajpEnabled +
				", port=" + port +
				", scheme='" + scheme + '\'' +
				", proxyName='" + proxyName + '\'' +
				", proxyPort=" + proxyPort +
				", secure=" + secure +
				'}';
	}
}