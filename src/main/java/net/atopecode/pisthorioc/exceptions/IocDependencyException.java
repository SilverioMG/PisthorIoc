package net.atopecode.pisthorioc.exceptions;

/**
 * Exception genérica para errores del 'IocContainer'.
 */
public class IocDependencyException extends RuntimeException {

    public IocDependencyException(String message){
        super(message);
    }
}
