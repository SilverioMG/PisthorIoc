package net.atopecode.pisthorioc.exceptions;

/**
 * Se utiliza esta 'Exception' cuando se intenta resolver una dependencia y el valor devuelto después de ejecutar
 * su correspondiente 'Factory' es 'null'.
 */
public class IocDependencyNotFoundException extends IocDependencyException {

    public IocDependencyNotFoundException(String message){
        super(message);
    }
}
