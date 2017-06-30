package be.ugent.idlab.tcbl.userdatamanager;

import be.ugent.idlab.tcbl.userdatamanager.model.InMemoryTCBLUserRepository;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@SpringBootApplication
public class OAuth2LoginApplication {

	@Bean
	public TCBLUserRepository tcblUserRepository() {
		return new InMemoryTCBLUserRepository();
	}

	@Bean
	public Converter<String, TCBLUser> tcblUserConverter() {
		return new Converter<String, TCBLUser>() {
			@Nullable
			@Override
			public TCBLUser convert(String userName) {
				return tcblUserRepository().find(userName);
			}
		};
	}

	/*public OAuth2LoginApplication() {
	}  */

	public static void main(String[] args) {
		SpringApplication.run(OAuth2LoginApplication.class, args);
	}
}
