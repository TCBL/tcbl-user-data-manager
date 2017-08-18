package be.ugent.idlab.tcbl.userdatamanager.background;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

/**
 * <p>Copyright 2017 IDLab (Ghent University - imec)</p>
 *
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

	@Async
	public void send(final String to, final String subject, final String text) {
		log.debug("Preparing mail \"{}\" to {}", subject, to);
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message);
		try {
			helper.setSubject(subject);
			helper.setFrom(from, "TCBL notifications");
			helper.setTo(to);
			helper.setText(text, true);
			mailSender.send(message);
			log.debug("Mail sent to {}", to);
		} catch (Exception e) {
			log.error("Could not send mail to {}. ", to, e);
		}
	}
}
