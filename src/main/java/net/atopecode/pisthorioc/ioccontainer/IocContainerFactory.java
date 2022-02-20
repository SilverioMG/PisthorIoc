package net.atopecode.pisthorioc.ioccontainer;

import org.slf4j.Logger;

/**
 * Clase de tipo 'Factory' para crear instancias del tipo 'IocContainer'.
 */
public class IocContainerFactory {

    private static IocContainer iocContainer;

    protected IocContainerFactory(){
        //Empty Constructor.
    }

    /**
     * Crea una nueva instancia (nuevo objeto) de un 'IocContainer'.
     * @param logger
     * Implementación de logger SLF4J (como LogBack por ejemplo).
     * Si es 'null', el 'IocContainer' se creará y funcionará correctamente pero no se mostrará ningún mensaje de log.
     * @return
     * Devuelve una nueva instancia (nuevo objeto) de un 'IocContainer'.
     */
    public static IocContainer newInstance(Logger logger){
        return new IocContainer(logger);
    }

    /**
     * La primera vez que se ejecuta este método se crea una nueva instancia (nuevo objeto) de un 'IocContainer' y
     * las sucesivas veces se devuelve siempre el mismo objeto creado (misma dirección de memoria).
     * Este método es 'ThreadSafe'.
     * @param logger
     * Implementación de logger SLF4J (como LogBack por ejemplo).
     * Si es 'null', el 'IocContainer' se creará y funcionará correctamente, pero no se mostrará ningún mensaje de log.
     * @return
     * Devuelve siempre la misma instancia (mismo objeto) de un 'IocContainer'.
     */
    public synchronized static IocContainer singleton(Logger logger){
        if(iocContainer == null){
            iocContainer = newInstance(logger);
        }

        return iocContainer;
    }
}
