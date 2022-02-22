package net.atopecode.pisthorioc.ioccontainer;

import net.atopecode.pisthorioc.exceptions.IocDependencyException;
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
     * @return
     * Devuelve una nueva instancia (nuevo objeto) de un 'IocContainer'.
     */
    public static IocContainer newInstance(){
        return new IocContainer();
    }

    /**
     * La primera vez que se ejecuta este método se crea una nueva instancia (nuevo objeto) de un 'IocContainer' y
     * las sucesivas veces se devuelve siempre el mismo objeto creado (misma dirección de memoria).
     * Este método es 'ThreadSafe'.
     *
     * @return
     * Devuelve siempre la misma instancia (mismo objeto) de un 'IocContainer'.
     */
    public synchronized static IocContainer singleton(){
        if(iocContainer == null){
            iocContainer = newInstance();
        }

        return iocContainer;
    }
}
