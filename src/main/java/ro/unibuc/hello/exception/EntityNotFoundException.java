package ro.unibuc.hello.exception;

import javax.management.ListenerNotFoundException;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends StoreException {

    private static final String entityNotFoundTemplate = "Entity: %s was not found";

    public EntityNotFoundException(String entity) {
        this.setHttpStatus(HttpStatus.UNAUTHORIZED);
        this.setMessage(String.format(entityNotFoundTemplate, entity));
    }

    public EntityNotFoundException(){
        this.setHttpStatus(HttpStatus.UNAUTHORIZED);
        this.setMessage("entity not found");
    }

    public EntityNotFoundException(int Error){
        this.setHttpStatus(HttpStatus.NOT_FOUND);
        this.setMessage("entity not found (404)");
    }
}
