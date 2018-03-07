package be.ugent.idlab.tcbl.userdatamanager.controller.support;

public class BadContentTypeException extends BadContentException {

    public BadContentTypeException(String message) {
        super(message);
    }

    public BadContentTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
