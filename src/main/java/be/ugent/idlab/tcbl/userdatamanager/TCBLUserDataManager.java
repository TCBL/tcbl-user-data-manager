package be.ugent.idlab.tcbl.userdatamanager;

import be.ugent.idlab.tcbl.userdatamanager.controller.support.PictureStorage;
import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import be.ugent.idlab.tcbl.userdatamanager.model.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author Gerald Haesendonck
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class TCBLUserDataManager {
	private final UserRepository userRepository;

	public TCBLUserDataManager(UserRepository userRepository) {
		this.userRepository = userRepository;
		userRepository.synchronise();
	}

	@Bean
	public PictureStorage pictureStorage(final Environment environment) throws Exception {
		return new PictureStorage(environment);
	}

	@Bean
	public Converter<String, TCBLUser> tcblUserConverter(final Environment environment) {
		return new Converter<String, TCBLUser>() {
			@Nullable
			@Override
			public TCBLUser convert(String id) {
				try {
					return userRepository.find(id);
				} catch (Exception e) {
					throw new IllegalArgumentException(e);
				}
			}
		};
	}

	// see https://spring.io/guides/gs/async-method/ and
	// https://stackoverflow.com/questions/30431776/using-scheduled-and-enablescheduling-but-gives-nosuchbeandefinitionexception#30913795 to explain
	// the next 2 beans
	@Bean
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.initialize();
		return executor;
	}

	@Bean
	public TaskScheduler taskScheduler() {
		return new ConcurrentTaskScheduler();
	}

	public static void main(String[] args) {
		SpringApplication.run(TCBLUserDataManager.class, args);
	}
}
