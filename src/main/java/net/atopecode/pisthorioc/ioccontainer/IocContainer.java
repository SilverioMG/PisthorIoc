package net.atopecode.pisthorioc.ioccontainer;

import net.atopecode.pisthorioc.dependencyfactory.DependencyFactory;
import net.atopecode.pisthorioc.dependencyresolver.DependencyResolver;
import net.atopecode.pisthorioc.dependencyresolver.IDependencyResolver;
import net.atopecode.pisthorioc.exceptions.IocDependencyException;
import net.atopecode.pisthorioc.normalizername.NormalizerName;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;


/**
 * Esta clase actúa como contenedor para Inyección de Dependencias. (Di, Ioc).
 *
 * Permite registrar la dependencias en forma de 'factories' que indican como se crea cada objeto y las dependencias
 * que necesita via 'constructor' para su creación. Se pueden registrar dependencias de tipo 'Singleton' o 'Prototype'.
 *
 * Una vez registradas las dependencias, se pueden resolver y obtener el objeto necesario.
 * Las dependencias de tipo 'Singleton' se almacenan dentro del contenedor y solo se crean una vez devolviendo siempre
 * la misma instancia (dirección de memoria) para el mismo objeto cada vez que se intenta resolver o se inyecte a su vez
 * en otra dependencia.
 * Las dependencias de tipo 'Prototype' no se almacenan dentro del contenedor. Se crea una nueva instancia (nuevo objeto)
 * cada vez que se intentan resolver o inyectar a su vez en otra dependencia.
 *
 * Las dependencias registradas se van creando a medida que se intentan resolver.
 * Pueden producirse varios errores debido a un mal uso del contenedor, por haber registrado mal alguna dependencia o porque se está intentando
 * resolver de forma incorrecta.
 * Error al intentar resolver una dependencia que no ha sido registrada previamente.
 * Error debido a que se quiere resolver alguna dependencia que ha sido registrada como dependencias cícicla de otra.
 * Error en la conversión de tipos (casting) al intentar resolver o inyectar una dependencia de un tipo concreto en una variable/campo de
 * un tipo incompatible.
 * Estos posibles errores se producen siempre a la hora de resolver una dependencia (se lanza una RuntimeException para cada tipo de error).
 * Para evitar que se produzcan errores inesperados mientras se está ejecutando el programa es aconsejable no esperar a que se intente resolver
 * una dependencia y resolverlas todas a la vez justo depués de haberlas registrado. (utilizar el método 'IocContainer.loadContent()').
 *
 * El funcionamiento de esta clase es 'ThreadSafe'.
 */
public class IocContainer {
    public final Logger LOGGER;

    private final Map<String, Object> mapObjects;
    private final Map<String, DependencyFactory> mapFactory;

    public IocContainer(Logger logger){
        this.mapObjects = new TreeMap<>(); //Podría ser perfectamente un 'HashMap', no importa el orden de los objetos, pero se utiliza 'TreeMap' para que en el método 'logContent()' se muestren por orden de resolución.
        this.mapFactory = new HashMap<>();
        this.LOGGER = logger;
    }

    /**
     * Se registra la creación de un objeto como dependencia y las posibles dependencias que necesita inyectar via constructor
     * para su creación.
     * @param name
     *  Nombre para identificar al objeto dependencia.
     * @param factory
     * 'Function' que debe devolver la creación del objeto dependencia. En la construcción de dicho objeto se deben
     *  inyectar las otras dependencias necesarias por medio del objeto 'IDependenciaResolver' recibido como parámetro
     *  en la 'Function'.
     * @param <TResult>
     * Tipo de dato del objeto depedencia.
     * @return
     * El objeto 'IocContainer' para poder hacer programación 'fluentApi'.
     */
    public <TResult> IocContainer register(String name, Function<IDependencyResolver, TResult> factory){
        return register(new DependencyFactory<TResult>(name, factory));
    }

    /**
     * Se registra la creación de un objeto como dependencia y las posibles dependencias que necesita inyectar via constructor
     * para su creación.
     * @param name
     *  Nombre para identificar al objeto dependencia.
     * @param type
     *  Indica si la dependencia será de tipo 'Singleton' o 'Prototype'.
     * @param factory
     *  'Function' que debe devolver la creación del objeto dependencia. En la construcción de dicho objeto se deben
     *  inyectar las otras dependencias necesarias por medio del objeto 'IDependenciaResolver' recibido como parámetro
     *  en la 'Function'.
     * @param <TResult>
     *  Tipo de dato del objeto depedencia.
     * @return
     *  El objeto 'IocContainer' para poder hacer programación 'fluentApi'
     */
    public <TResult> IocContainer register(String name, DependencyFactory.DependencyType type, Function<IDependencyResolver, TResult> factory){
        return register(new DependencyFactory<>(name, type, factory));
    }

    /**
     * Se registra la creación de un objeto como dependencia y las posibles dependencias que necesita inyectar via constructor
     * para su creación.
     * @param factory
     *  Objeto del tipo 'DependencyFactory' para indicar como se debe crear la 'dependencia'.
     * @return
     *  El objeto 'IocContainer' para poder hacer 'fluentApi'.
     */
    private synchronized IocContainer register(DependencyFactory<? extends Object> factory) {
        if (factory == null) {
            throw new IocDependencyException("Parameter 'factories' must not be null.");
        }

        if (mapFactory.get(factory.getName()) != null) {
            logWarn("Override register for ioc dependency with name '" + factory.getName() + "'");
        }

        String name = NormalizerName.normalize(factory.getName());
        mapFactory.put(name, factory);

        return this;
    }

    /**
     * Se resuelve una dependencia a partir del nombre con el que fué registrada.
     * El objeto registrado como dependencia debe ser del mismo tipo o heredar del tipo de dato en el que se quiere
     * asignar.
     * Este método es 'ThreadSafe'.
     * @param name
     *  Nombre que se utilizó para regitrar la dependencia.
     * @param classResult
     *  Objeto 'Class' del tipo de dato de la dependencia registrada que se intenta resolver. El tipo de dato debe de ser
     *  compatible (casting) con el tipo de dato de la variable donde se asignará.
     * @param <T>
     *  Tipo de dato de la dependencia registrada que se intenta resolver. El tipo de dato debe de ser
     *  compatible (casting) con el tipo de dato de la variable donde se asignará.
     * @return
     *  Objeto dependencia registrado con el nombre indicado como parámetro.
     */
    public synchronized  <T> T resolve(String name, Class<? extends T> classResult){
        if(StringUtils.isBlank(name)){
            throw new IocDependencyException("Parameter 'name' must be not null.");
        }

        if(classResult == null){
            throw new IocDependencyException("Parameter 'classObject' must be not null.");
        }

        //Se crea un nuevo objeto 'DependencyResolver' cada vez para poder detectar posibles 'dependencias circulares'.
        DependencyResolver resolver = new DependencyResolver(mapObjects, mapFactory);
        return resolver.resolve(name, classResult);
    }

    /**
     * Este método resuelve todas las dependencias registradas en el 'iocContainer'.
     * Se utiliza para evitar que se produzcan errores durante la ejecución del servicio ya que las dependencias se van
     * inyectando a medida que se resuelven y se pueden producir errores de 'dependicas no registradas', 'dependencias circular' o 'casting'.
     * Es aconsejable ejecutar este método justo después de resgitrar todas las dependencias para asegurarnos que se regitraron
     * correctamente.
     *
     * Las depedencias registradas como 'Singleton' quedan guardadas en memoria y así se gana en velocidad cada vez que se
     * intenten resolver.
     * @param verbose
     *  Indica si se muestra la info de las dependencias que se itentan resolver durante el proceso de carga.
     * @param logContent
     *  Indica si una vez finalizado el proceso de carga debe mostrarse la info de todas las dependencias almacenadas en el contenedor.
     *  Solo se indicarán las dependencias de tipo 'singleton' ya que las de tipo 'prototype' no se almacenan en el contenedor (se crean de nuevo en cada resolución).
     * @return
     *  El objeto 'IocContainer' para poder hacer 'fluentApi'.
     */
    public IocContainer loadContent(boolean verbose, boolean logContent){
        logInfo("");
        logInfo("Loading IocContainer:");
        logInfo("---------------------");

        if(mapFactory.size() > 0){
            mapFactory.keySet().forEach((String name) -> {
                if(verbose) logInfo(MessageFormat.format("Resolving {0} dependency with name: {1} ...", mapFactory.get(name).getType(), name));
                resolve(name, Object.class);
            });

            if(logContent) showContent();
        }
        else{
            logInfo("Nothing to load: There is not dependency factories registered for IocContainer!!!");
        }

        return this;
    }

    /**
     * Se hace Logging de todas las dependencias 'resueltas' (no las registradas, solo las que ya se han inyectado)
     * dentro dentro del contenedor de dependencias.
     * Solo se muestrans las dependencias resueltas de tipo 'SINGLETON' ya que las de tipo 'PROTOTYPE' se crean de nuevo cada vez que se
     * intentan resolver y no se almacenan dentro del contenedor.
     * @return
     *  El objeto 'IocContainer' para poder hacer 'fluentApi'.
     */
    public IocContainer showContent(){
        logInfo("");
        logInfo("IocContainer content:");
        logInfo("---------------------");
        if(mapObjects.size() > 0) {
            mapObjects.entrySet()
                    .forEach((Map.Entry<String, Object> entry) -> {
                        String name = entry.getKey();
                        String objectName = entry.getValue().getClass().getName();
                        String message = MessageFormat.format("For name: \"{0}\" injects object of type \"{1}\"", name, objectName);
                        logInfo(message);
                    });
        }
        else{
            String message = MessageFormat.format("There is not registered pistonioc.test.dependencies or all registered pistonioc.test.dependencies are of type {0}.", DependencyFactory.DependencyType.PROTOTYPE);
            logInfo(message);
        }

        return this;
    }

    private void logInfo(String message){
        if(LOGGER != null){
            LOGGER.info(message);
        }
    }

    private void logWarn(String message){
        if(LOGGER != null){
            LOGGER.warn(message);
        }
    }
}
