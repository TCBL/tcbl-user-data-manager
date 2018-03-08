package be.ugent.idlab.tcbl.userdatamanager.controller.support;

import be.ugent.idlab.tcbl.userdatamanager.model.NavLink;
import be.ugent.idlab.tcbl.userdatamanager.model.Status;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * <p>Defines handlers for exceptions.</p>
 * <p>
 * <p>Copyright 2018 IDLab (Ghent University - imec)</p>
 *
 * @author Martin Vanbrabant
 */
@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, Model model) {
		ConfirmationTemplate ct = new ConfirmationTemplate("Maxium upload size exceeded");
		ct.setUtext("<p>You tried to upload a file larger than the configured limit.</p>");
		ct.setStatus(new Status(Status.Value.ERROR, "Maxium upload size exceeded."));
		return ct.getPreparedPath(model);
	}

}