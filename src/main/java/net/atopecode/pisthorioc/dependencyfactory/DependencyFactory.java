package net.atopecode.pisthorioc.dependencyfactory;

import net.atopecode.pisthorioc.dependencyresolver.IDependencyResolver;
import net.atopecode.pisthorioc.exceptions.IocDependencyException;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

/**
 * Esta clase indica como se debe crear un objeto dependencia durante el proceso de registro.
 * Se identifica cada dependencia por un nombre.
 * Se indica si la creación del objeto dependencia será del tipo 'Singleton' o 'Prototype'.
 * Se indica por medio de una 'Function' la creación del objeto y las otras dependencias que necesita inyectar via
 * constructor.
 * @param <TResult>
 *     Tipo de dato del objeto dependencia.
 */
public class DependencyFactory<TResult> {

    /**
     * Enumerado que indica el tipo de la dependencia registrada.
     * SINGLETON: Solo se crea la dependencia la primera vez que se intentan resolver y el resto de las veces se devuelve
     * siempre el mismo objeto. Se almacena la dependencia creada dentro del contenedor.
     * PROTOTYPE: Cada vez que se intenta resolver la dependencia se crea un nuevo objeto. No se almacena la dependencia
     * dentro del contenedor.
     */
    public enum DependencyType { SINGLETON, PROTOTYPE };

    private final String name;
    private final DependencyType type;
    private final Function<IDependencyResolver, TResult> factory;

    /**
     * Se indica como debe ser la creación de un objeto dependencia.
     * @param name
     *  Nombre para identificar al objeto dependencia.
     * @param factory
     *  'Function' que debe devolver la creación del objeto dependencia. En la construcción de dicho objeto se deben
     *  inyectar las otras dependencias necesarias por medio del objeto 'IDependenciaResolver' recibido como parámetro
     *  en la 'Function'.
     */
    public DependencyFactory(String name, Function<IDependencyResolver, TResult> factory){
        this(name, DependencyType.SINGLETON, factory);
    }

    /**
     * Se indica como debe ser la creación de un objeto dependencia.
     * @param name
     *  Nombre para identificar al objeto dependencia.
     * @param type
     *  Indica si la dependencia será de tipo 'Singleton' o 'Prototype'.
     * @param factory
     *  'Function' que debe devolver la creación del objeto dependencia. En la construcción de dicho objeto se deben
     *  inyectar las otras dependencias necesarias por medio del objeto 'IDependenciaResolver' recibido como parámetro
     *  en la 'Function'.
     */
    public DependencyFactory(String name, DependencyType type, Function<IDependencyResolver, TResult> factory){
        this.name = name;
        this.type = type;
        this.factory = factory;
        checkParams();
    }

    public String getName() {
        return name;
    }

    public DependencyType getType() {
        return type;
    }

    public Function<IDependencyResolver, TResult> getFactory(){
        return factory;
    }

    public boolean isTypeSingleton(){
        return this.type.equals(DependencyType.SINGLETON);
    }

    public boolean isTypePrototype() {
        return this.type.equals(DependencyType.PROTOTYPE);
    }

    private void checkParams() throws IocDependencyException {
        if(StringUtils.isBlank(name)){
            throw new IocDependencyException("Parameter 'name' must be not null.");
        }

        if(factory == null){
            throw new IocDependencyException("Parameter 'factory' must be not null.");
        }

        if(type == null){
            throw new IocDependencyException("Parameter 'type' must be not null.");
        }
    }

    @Override
    public String toString() {
        return "DependencyFactory{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", factory=" + factory +
                '}';
    }
}
