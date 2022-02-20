package net.atopecode.pisthorioc.exceptions;

/**
 * Se utiliza esta 'Exception' cuando a la hora de resolver una dependencia, no se encuentra ninguna 'Factory'
 * registrada con el mismo nombre con el que se intenta resolver.
 */
public class IocDependencyFactoryNotFoundException extends IocDependencyException {

    public IocDependencyFactoryNotFoundException(String message){
        super(message);
    }
}
