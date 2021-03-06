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

package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import be.ugent.idlab.tcbl.userdatamanager.model.TCBLUser;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * <p>Activity logging support.</p>
 *
 * @author Martin Vanbrabant
 */
@Component
public class ActivityLogger {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final String endpoint;
	private final String jwtKey;
	private final boolean enabled;
	// see https://spring.io/guides/gs/consuming-rest/
	// and http://www.baeldung.com/rest-template
	private final RestTemplate restTemplate;

	public ActivityLogger(Environment environment) {
		String endpoint = null;
		String jwtKey = null;
		boolean enabled = false;
		try {
			endpoint = environment.getRequiredProperty("tudm.activity-logging.endpoint");
			jwtKey = environment.getRequiredProperty("tudm.activity-logging.jwtkey");
			log.info(String.format("Activity logging is enabled; endpoint: '%s', jwtKey: '%s'", endpoint, jwtKey));
			enabled = true;
		} catch (Exception e) {
			log.info("Activity logging is not enabled.");
		}
		this.endpoint = endpoint;
		this.jwtKey = jwtKey;
		this.enabled = enabled;

		// With default HttpClient: this.restTemplate = new RestTemplate();
		// Below: create our restTemplate based on a custom configured Apache HttpComponents HttpClient:
		//   HttpComponentsClientHttpRequestFactory is a ClientHttpRequestFactory implementation
		//   that uses Apache HttpComponents HttpClient to create requests.
		//   Found at http://www.baeldung.com/rest-template.
		int timeout = environment.getProperty("tudm.activity-logging.timeout", Integer.class, 0); // ms
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.setSocketTimeout(timeout)
				.build();
		CloseableHttpClient client = HttpClientBuilder
				.create()
				.setDefaultRequestConfig(config)
				.build();
		this.restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory(client));
	}

	public void log(TCBLUser tcblUser, ActivityLoggingType logType) {
		log(tcblUser, logType, null);
	}

	@Async
	public void log(TCBLUser tcblUser, ActivityLoggingType logType, Object extraData) {
		if (tcblUser == null) {
			log.error(String.format("Activity logging - not logging event %s for null user", logType.getValue()));
		} else {
			String userName = tcblUser.getUserName();
			if (tcblUser.isAllowedMon()) {
				send(new ActivityLoggingDataToSend(userName, logType.getValue(), extraData));
			} else {
				log.debug(String.format("Activity logging not allowed for user %s.", userName));
			}
		}
	}

	private void send(ActivityLoggingDataToSend record) {
		if (enabled) {
			log.debug(String.format("Activity logging - sending %s.", record.toString()));
			try {
				HttpHeaders headers = new HttpHeaders();
				headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwtKey);
				// All other needed headers (such as Accept and Content-Type) are added nicely by RestTemplate.
				// To check the finally constructed request right before it is sent, set a breakpoint
				// in RestTemplate.java, method "protected <T> T doExecute(...)", statement "response = request.execute();".
				// To see the physical payload, set in application.yml: logging.level.org.apache.http.wire debug.
				HttpEntity<ActivityLoggingDataToSend> request = new HttpEntity<>(record, headers);

				ResponseEntity<ActivityLoggingDataReturned> response = restTemplate.postForEntity(endpoint, request, ActivityLoggingDataReturned.class);
				if (log.isDebugEnabled()) {
					// We're not using the data returned here unless for debugging...
					HttpStatus status = response.getStatusCode();
					ActivityLoggingDataReturned result = response.getBody();
					log.debug(String.format("Activity logging - sent %s: status %d (%s); uuid '%s'.", record.toString(), status.value(), status.getReasonPhrase(), result.getUuid()));
				}
			} catch (Exception e) {
				log.error(String.format("Activity logging - failed sending %s: %s.", record.toString(), e.getMessage()));
			}
		} else {
			log.debug(String.format("Activity logging - disabled; cannot send %s.", record.toString()));
		}
	}
}
