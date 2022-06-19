package net.atopecode.pisthorioc.dependencyresolver;

import net.atopecode.pisthorioc.dependencyfactory.DependencyFactory;
import net.atopecode.pisthorioc.exceptions.*;
import net.atopecode.pisthorioc.ioccontainer.interfaces.IDependency;
import net.atopecode.pisthorioc.normalizername.NormalizerName;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Esta clase se utiliza para resolver una dependencia registrada y/o almacenada dentro del contenedor.
 * Utiliza programación recursiva para ir creando las dependencias necesarias a medida que se van resolviendo/inyectando
 * unas en otras vía constructor.
 *
 * El contenedor de dependencias debe crear una nueva instancia de esta clase cada vez que intente resolver una dependencia para
 * poder ejecutar correctamente la lógica que detecta si se está intentado resolver una dependencia cicurlar
 * (se produciría un bucle infinito en caso de no detectarla).
 *
 * Se lanzan 'RuntimeExceptions' personalizadas para los posibles errores:
 * -Se intenta resolver una dependencia que no está registrada o se registró devolviendo valor 'null'.
 * -Se intenta resolver una dependencia circular.
 * -Se intenta resolver y asignar una dependencia en una variable de un tipo de dato no compatible (no se puede hacer casting)
 *  con el objeto registrado como dependencia.
 */
public class DependencyResolver implements IDependencyResolver {

    private final Map<String, Object> mapObjects;
    private final Map<String, DependencyFactory> mapFactory;
    private final Set<String> pendingToInject; //Se usa para comprobar que no se produzca inyección de dependencias circulares (bluce infinito).

    public  DependencyResolver(Map<String, Object> mapObjects,
                               Map<String, DependencyFactory> mapFactory){
        this.mapObjects = requireNonNull(mapObjects);
        this.mapFactory = requireNonNull(mapFactory);
        this.pendingToInject = new HashSet<>();
    }

    /**
     * Resuelve una dependencia por medio del nombre con el que fué registrada.
     * Las dependencias de tipo 'Singleton' se crean la primera vez y las veces posteriores se buscan dentro del contenedor
     * devolviendo siempre el mismo objeto/instancia.
     * Las dependencias de tipo 'Prototype' no se almacenan dentro del contenedor y se crean cada vez que se intentan resolver/inyectar.
     * El objeto registrado como dependencia debe ser del mismo tipo o heredar del tipo de dato en el que se quiere
     * asignar.
     * @param name
     * Nombre que se utilizó para regitrar la dependencia.
     * @param classResult
     * Objeto 'Class' del tipo de dato de la dependencia registrada que se intenta resolver. El tipo de dato debe de ser
     * compatible (casting) con el tipo de dato de la variable donde se asignará.
     * @param <T>
     * Tipo de dato de la dependencia registrada que se intenta resolver. El tipo de dato debe de ser
     * compatible (casting) con el tipo de dato de la variable donde se asignará.
     * @return
     * Objeto dependencia registrado con el nombre indicado como parámetro.
     * @throws IocDependencyException
     * @throws IocDependencyFactoryNotFoundException
     * @throws IocDependencyNotFoundException
     * @throws IocDependencyCastingException
     */
    @Override
    public <T> T resolve(String name, Class<? extends T> classResult){
        if(StringUtils.isBlank(name)){
            throw new IocDependencyException("Parameter 'name' must be not null.");
        }

        if(classResult == null){
            throw new IocDependencyException("Parameter 'classObject' must be not null.");
        }

        name = NormalizerName.normalize(name);
        checkCircularDependency(name);

        T object = getObjectFromMap(name, classResult);
        if(object == null){
            //Si el objeto no existe existe en 'mapObjects' quiere decir que aún no se inyectó ninguna vez o que es de tipo 'prototipe'. Se utiliza su 'factory' para inyectarlo como dependencia.
            Object objectInjected = doInjection(name); //Posibles llamadas recursivas a este método (si el constructor de la dependencia tiene que resolver otras dependencias).
            object = castObject(objectInjected, classResult, name);
        }

        pendingToInject.remove(name);

        return object;
    }

    //Método que se llama desde el método 'resolve()' cuando una dependencia no está guardada en 'mapObjects' porque es la primera vez que se crea
    //o porque está registrada como tipo 'Prototype' para devolver una nueva instancia del objeto dependencia. Si en el constructor de la dependencia se necesita
    //inyectar otro objeto como dependencia, se hace una llamada recursiva al método 'resolver()'.
    //Se utiliza la recursividad para ir creando los objetos que hay que inyectar via constructor a medida que se van resolviendo/inyectando las dependencias.
    //Un objeto es resuelto/creado después de que de forma recursiva se hayan creado o recuperado todas sus dependencias y recursivamente todas las dependencias
    //de las que depende cada una de sus dependencias :)
    private Object doInjection(String name){
        DependencyFactory<? extends Object> factory = mapFactory.get(name);
        if(factory == null) throw new IocDependencyFactoryNotFoundException("There is no registered factory for dependency with name '" + name + "'");

        //Produce llamadas recursivas al método 'resolve()':
        Object object = factory.getFactory().apply(this);
        if(object == null) throw new IocDependencyNotFoundException("Not found dependency with name '" + name + "'");
        postConstruct(object);

        //Se guarda la dependencia solo si es de tipo 'Singleton', sino la próxima vez que se intente inyectar se creará una nueva instancia del objeto.
        if(factory.isTypeSingleton()) mapObjects.put(name, object);

        return object;
    }

    //Recupera un objeto de la hashmap 'mapObjects' si existe, sino devuelve 'null'. Se hace casting del objeto recuperado
    //al tipo de dato indicado como genérico del parámetro 'classObject'.
    private <T> T getObjectFromMap(String name, Class<? extends T> classObject){
        Object object = mapObjects.get(name);
        T result = castObject(object, classObject, name);

        return result;
    }

    //Se hace casting de un objeto al tipo de dato indicado como genérico del parámetro 'classObject'.
    //Si falla el casting se lanza una Excetion.
    //Recordar que siempre se puede hacer casting del valor 'null' a cualquier tipo de dato.
    private <T> T castObject(Object object, Class<? extends T> classObject, String name){
        try{
            T result = classObject.cast(object);
            return result;
        }
        catch(Exception ex){
            throw new IocDependencyCastingException("The ioc dependency with name '" + name + "' is not of type '" + classObject.getName() + "'");
        }
    }

    /**
     *  Este método detecta si se está intentado realizar la inyección de 'dependencias circulares', es decir, que un objeto A necesite
     *  inyectar un objeto B y el objeto B necesite inyectar un objeto A. En ese caso se producirá un bucle infinito mientras uno intenta
     *  obtener una instancia del otro.
     *
     *  En caso de detectar una dependencia circular se lanza una exception del tipo 'IocCircularDependencyException'.
     * @param name
     * @throws IocCircularDependencyException
     */
    private void checkCircularDependency(String name){
        if(pendingToInject.contains(name)){
            String circularObjects = pendingToInject.toString();
            throw new IocCircularDependencyException("Detected circular dependency between objects :" + circularObjects);
        }

        pendingToInject.add(name);
    }

    /**Este método comoprueba si la nueva Dependencia creada/inyectada implementa la interfaz 'IDependency', en cuyo
     * caso ejecuta su método 'postContruct()'.
     * @param object
     */
    private void postConstruct(Object object){
        if(object instanceof IDependency){
            IDependency dependency = (IDependency) object;
            dependency.postContruct();
        }
    }
}
