/*
 *  Copyright 2019 imec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package be.ugent.idlab.tcbl.userdatamanager.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

/**
 * @author Gerald Haesendonck
 */
@Component
public class UserRepository {
	private final Logger log = LoggerFactory.getLogger(TCBLUser.class);

	private final ScimUserRepository scimUserRepository;
	private final DatabaseUserRepository databaseUserRepository;
	private final boolean syncUserDataAtBoot;

	public UserRepository(Environment environment,
						  ScimUserRepository scimUserRepository,
						  DatabaseUserRepository databaseUserRepository) {
		this.scimUserRepository = scimUserRepository;
		this.databaseUserRepository = databaseUserRepository;
		this.syncUserDataAtBoot = environment.getProperty("tudm.sync-userdata-at-boot", Boolean.class, false);
	}

	public TCBLUser find(String inum) throws  Exception {
		return databaseUserRepository.findByInum(inum);
	}

	public void save(TCBLUser user) throws Exception {
		TCBLUser storedUser = findByName(user.getUserName());
		user.setActiveSince(storedUser.getActiveSince());
		user.setInvited(storedUser.isInvited());
		user.setPasswordReset(storedUser.getPasswordReset());
		user.setLastModified(new Date());
		user.setCreated(storedUser.getCreated());

		scimUserRepository.save(user);
		databaseUserRepository.save(user);
	}
	public TCBLUser findByName(final String userName) {
		Optional<TCBLUser> userOption = databaseUserRepository.findById(userName);
		return userOption.get();
	}
	public TCBLUser create(TCBLUser user) throws Exception {
		TCBLUser resultUser = scimUserRepository.create(user);
		resultUser.setInvited(false);
		databaseUserRepository.save(resultUser);
		return resultUser;
	}
	public void deleteTCBLUser(TCBLUser user) throws Exception {
		scimUserRepository.deleteTCBLUser(user);
		databaseUserRepository.delete(user);
	}
	public Iterable<TCBLUser> findInactive() {
		return databaseUserRepository.findByActive(false);
	}

	public void processTCBLUsers(final TCBLUserProcessor processor) {
		for (TCBLUser tcblUser : databaseUserRepository.findAll()) {
			processor.process(tcblUser);
		}
	}

	@Async
	public void synchronise() {
		if (syncUserDataAtBoot) {
			log.info("Synchronising user data (fron Gluu server to local database)");
			try {
				scimUserRepository.processTCBLUsers(user -> {
					if (!databaseUserRepository.existsById(user.getUserName())) {
						databaseUserRepository.save(user);
					}
				});
				log.info("Database synchronised!");
			} catch (Exception e) {
				log.error("Something went wrong synchronising the databases!", e);
			}
		} else {
			log.info("Not synchronising user data (fron Gluu server to local database)");
		}
	}
}
