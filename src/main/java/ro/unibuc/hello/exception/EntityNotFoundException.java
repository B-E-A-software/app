package ro.unibuc.hello.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends StoreException {

    private static final String entityNotFoundTemplate = "Entity: %s was not found";

    public EntityNotFoundException(String entity) {
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage(String.format(entityNotFoundTemplate, entity));
    }

    public EntityNotFoundException(){
        this.setHttpStatus(HttpStatus.BAD_REQUEST);
        this.setMessage("entity not found");
    }
}
