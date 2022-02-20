package net.atopecode.pisthorioc.exceptions;

/**
 * Se utiliza esta Exception cuando no se puede realiza el 'casting' de un dependencia al tipo de dato de la variable
 * en la que se quiere asignar. Tipos de datos incompatibles.
 */
public class IocDependencyCastingException extends IocDependencyException {

    public IocDependencyCastingException(String message){
        super(message);
    }
}
