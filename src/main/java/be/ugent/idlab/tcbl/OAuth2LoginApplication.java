package be.ugent.idlab.tcbl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@SpringBootApplication
public class OAuth2LoginApplication {
	public OAuth2LoginApplication() {
	}

	public static void main(String[] args) {
		SpringApplication.run(OAuth2LoginApplication.class, args);
	}
}
