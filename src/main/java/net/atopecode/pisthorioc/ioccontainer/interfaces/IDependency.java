package net.atopecode.pisthorioc.ioccontainer.interfaces;

/**
 * Esta interfaz expone los métodos que debe impletar un objeto que vaya a ser utilizado como 'Depedencia' para registrarlo y posteriormente
 * resolverlo en el contenedor de dependencias 'IocContainer'.
 *
 * El uso de esta interfaz es opcional, se pueden registrar y resolver objetos como dependencias sin que implementen esta interfaz.
 * Pero no podrán hace uso de los métodos 'postContruct()' y 'preDestroy()'.
 */
public interface IDependency {

    /**
     * Se ejecuta este método justo después de haber creado la instancia de la Dependencia (después de que se ejecute su constructor).
     */
    void postContruct();

    /**
     * Se ejecuta este método justamente después de eliminar la instancia de la Dependencia del contenedor de dependencias IocContainer.
     */
    void preDestory();
}
