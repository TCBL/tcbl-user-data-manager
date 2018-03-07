package be.ugent.idlab.tcbl.userdatamanager;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.ProfilePictureStorage;
import be.ugent.idlab.tcbl.userdatamanager.controller.support.ProfilePictureStorageOnFileSystem;
import be.ugent.idlab.tcbl.userdatamanager.model.ScimTCBLUserRepository;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
 * @author Gerald Haesendonck
 */
@SpringBootApplication
@EnableScheduling
public class TCBLUserDataManager {

	@Bean
	public TCBLUserRepository tcblUserRepository(final Environment environment) throws Exception {
		return new ScimTCBLUserRepository(environment);
	}

	@Bean
	public ProfilePictureStorage profilePictureStorageService(final Environment environment) throws Exception {
		return new ProfilePictureStorageOnFileSystem(environment);
	}

	@Bean
	public Converter<String, TCBLUser> tcblUserConverter(final Environment environment) {
		return new Converter<String, TCBLUser>() {
			@Nullable
			@Override
			public TCBLUser convert(String id) {
				try {
					return tcblUserRepository(environment).find(id);
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(TCBLUserDataManager.class, args);
	}
}
