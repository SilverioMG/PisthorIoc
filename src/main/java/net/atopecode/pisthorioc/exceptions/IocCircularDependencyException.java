package net.atopecode.pisthorioc.exceptions;

/**
 * Exception que se lanza cuando se intenta resolver una dependencia que se registró como 'dependencia circular' de otra.
 */
public class IocCircularDependencyException extends IocDependencyException {

    public IocCircularDependencyException(String message){
        super(message);
    }
}
