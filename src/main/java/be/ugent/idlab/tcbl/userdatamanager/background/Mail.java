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

package be.ugent.idlab.tcbl.userdatamanager.background;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.util.Arrays;

/**
 * @author Gerald Haesendonck
 */
@Component
public class Mail {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final JavaMailSender mailSender;
	private final String from;

	public Mail(JavaMailSender mailSender, Environment environment) {
		this.mailSender = mailSender;
		from = environment.getProperty("spring.mail.from", "no-reply@tcbl.eu");
	}

	public void send(final String to, final String subject, final String text) {
		send(new String[]{to}, subject, text);
	}

	@Async
	public void send(final String[] to, final String subject, final String text) {
		log.debug("Preparing mail \"{}\" to {}", subject, to);
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			helper.setSubject(subject);
			helper.setFrom(from, "TCBL notifications");
			if (to.length == 1) {
				helper.setTo(to);
			} else {
				helper.setBcc(to);
			}
			helper.setText(text, true);
			mailSender.send(message);
			if (log.isDebugEnabled()) {
				log.debug("Mail sent to {}", Arrays.toString(to));
			}
		} catch (Exception e) {
			log.error("Could not send mail to {}. ", to, e);
		}
	}
}
