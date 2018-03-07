package be.ugent.idlab.tcbl.userdatamanager.controller.support;

public class BadContentException extends RuntimeException {

    public BadContentException(String message) {
        super(message);
    }

    public BadContentException(String message, Throwable cause) {
        super(message, cause);
    }
}
