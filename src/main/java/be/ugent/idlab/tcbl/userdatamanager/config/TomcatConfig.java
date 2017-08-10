package be.ugent.idlab.tcbl.userdatamanager.config;

import org.apache.catalina.connector.Connector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * See https://github.com/spring-projects/spring-boot/issues/1647
 */
@Configuration
@AutoConfigureBefore(ServletWebServerFactoryAutoConfiguration.EmbeddedTomcat.class)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class TomcatConfig {
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@ConditionalOnClass(value = ServerProperties.Tomcat.class)
	@Bean
	public ServletWebServerFactory servletWebServerFactory() {
		// TODO configure!
		log.warn("***** init AJP connector in embedded tomcat");
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		final Connector ajpConnector = new Connector("AJP/1.3");
		ajpConnector.setPort(8445);
		ajpConnector.setScheme("https");
		ajpConnector.setProxyName("tcblsso2.ilabt.imec.be");
		ajpConnector.setProxyPort(8445);
		//ajpConnector.setDomain("");
		ajpConnector.setSecure(true);
		tomcat.addAdditionalTomcatConnectors(ajpConnector);
		log.warn("***** init AJP connector in embedded tomcat done.");
		return tomcat;
	}

}