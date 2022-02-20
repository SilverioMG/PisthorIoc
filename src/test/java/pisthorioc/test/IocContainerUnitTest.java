package pisthorioc.test;

import net.atopecode.pisthorioc.dependencyfactory.DependencyFactory;
import net.atopecode.pisthorioc.exceptions.*;
import net.atopecode.pisthorioc.ioccontainer.IocContainer;
import net.atopecode.pisthorioc.ioccontainer.IocContainerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pisthorioc.test.dependencies.*;
import pisthorioc.test.dependencies.interfaces.Controller;
import pisthorioc.test.dependencies.interfaces.IRepository;
import pisthorioc.test.dependencies.interfaces.IService;

import static org.junit.jupiter.api.Assertions.*;

public class IocContainerUnitTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(IocContainer.class);

    public final static String REPOSITORY1 = "repository1";
    public final static String REPOSITORY2 = "repository2";
    public final static String SERVICE1 = "service1";
    public final static String SERVICE2 = "service2";
    public final static String CONTROLLER = "controller";
    public final static String SERVICE_CIRCULAR1 = "serviceCircular1";
    public final static String SERVICE_CIRCULAR2 = "serviceCircular2";
    public final static String SERVICE_CIRCULAR3 ="serviceCircular3";

    @Test
    @DisplayName("Comprobación de la creación de IocContainer por medio de la Factory para nuevas instancias y singletons.")
    public void createIocContainerNewInstanceAndSingletonFromFactory() {
        //Se comprueban las direcciones de memoria de los Objetos:
        IocContainer containerNewInstance1 = IocContainerFactory.newInstance(null);
        IocContainer containerNewInstance2 = IocContainerFactory.newInstance(null);
        assertTrue(containerNewInstance1 != containerNewInstance2);

        IocContainer containerSingleton1 = IocContainerFactory.singleton(null);
        IocContainer containerSingleton2 = IocContainerFactory.singleton(null);
        assertTrue(containerSingleton1 == containerSingleton2);

        assertTrue((containerNewInstance1 != containerSingleton1) && (containerNewInstance2 != containerSingleton1));
    }

    @Test
    @DisplayName("Registrando un objeto con un nombre no normalizado y recuperándolo.")
    public void checkRegistreAndResolveNormalizeName() {
        String dependencyName = "Dependency 1";
        String normalizedDependencyName = dependencyName.trim().toLowerCase();

        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        IRepository repository1 = iocContainer.register(dependencyName, (dr) -> new Repository1())
                .resolve(dependencyName, Repository1.class);

        assertNotNull(repository1);

        IRepository repository2 = iocContainer.resolve(normalizedDependencyName, Repository1.class);
        assertNotNull(repository2);
        assertTrue(repository1 == repository2);
    }

    @Test
    @DisplayName("Registrando como singleton y resolviendo un objeto que no tiene más dependencias.")
    public void resolveObjectWithoutDependenciesSingleton() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer.register(
                REPOSITORY1,
                DependencyFactory.DependencyType.SINGLETON,
                (dr) -> new Repository1()
        );

        IRepository repository = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository);
        assertTrue(repository instanceof IRepository);

        repository = iocContainer.resolve(REPOSITORY1, IRepository.class);
        assertNotNull(repository);
        assertTrue(repository instanceof IRepository);

        Repository1 repository1 = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository1);
        assertTrue(repository1 instanceof IRepository);

        assertTrue(repository == repository1);
    }

    @Test
    @DisplayName("Registrando como singleton y resolviendo un objeto que tiene 1 dependencia.")
    public void resolveObjectWithOneDependencySingleton() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1())
                .register(

                        SERVICE1,
                        (dr) -> new Service1(iocContainer.resolve(REPOSITORY1, Repository1.class))
                );

        IService service1 = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotNull(service1);

        IRepository repository = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository);

        assertTrue(repository == service1.getRepositories().get(0));
    }

    @Test
    @DisplayName("Registrando como singleton y resolviendo un objeto que tiene más de 1 dependencia.")
    public void resolveObjectWithMoreOneDepedencySingleton() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1())
                .register(
                        REPOSITORY2,
                        (dr) -> new Repository2()
                )
                .register(
                        SERVICE2,
                        (dr) -> new Service2(
                                dr.resolve(REPOSITORY1, Repository1.class),
                                dr.resolve(REPOSITORY2, Repository2.class))
                );

        IService service2 = iocContainer.resolve(SERVICE2, Service2.class);
        assertTrue(service2 != null);
        assertTrue((service2.getRepositories() != null) && (service2.getRepositories().size() == 2));

        IRepository repository1 = iocContainer.resolve(REPOSITORY1, Repository1.class);
        IRepository repository2 = iocContainer.resolve(REPOSITORY2, Repository2.class);
        assertTrue(repository1 == service2.getRepositories().get(0));
        assertTrue(repository2 == service2.getRepositories().get(1));
    }

    @Test
    @DisplayName("Registrando como singleton y resolviendo un objeto que tiene varias dependencias y una de ellas se usa en otro objeto.")
    public void resolveObjectWithDependencyAlreadyInjectedInOtherObjectSingleton() {
        IocContainer iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER);

        Controller controller = iocContainer.resolve(CONTROLLER, Controller.class);
        assertNotNull(controller);

        Repository1 repository1 = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository1);

        Service1 service1 = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotNull(service1);
        Service2 service2 = iocContainer.resolve(SERVICE2, Service2.class);
        assertNotNull(service2);

        assertTrue(controller.getRepository1() == repository1);
        assertTrue(controller.getRepository1() == service1.getRepositories().get(0));
        assertTrue(controller.getRepository1() == service2.getRepositories().get(0));

        assertTrue(controller.getService1() == service1);
        assertTrue(controller.getService2() == service2);
    }

    @Test
    @DisplayName("Registrando como prototype y resolviendo un objeto que no tiene más dependencias.")
    public void resolveObjectWithoutDependenciesPrototype() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer.register(
                REPOSITORY1,
                DependencyFactory.DependencyType.PROTOTYPE,
                (dr) -> new Repository1()
        );

        IRepository repository = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository);
        assertTrue(repository instanceof IRepository);

        IRepository repositoryNewInstance = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository);
        assertTrue(repository instanceof IRepository);

        assertTrue(repository != repositoryNewInstance);
    }

    @Test
    @DisplayName("Registrando como prototype un objeto y como singleton el objeto en el que se inyecta.")
    public void resolvePrototypeObjectIntoSingletonObject() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        REPOSITORY1,
                        DependencyFactory.DependencyType.PROTOTYPE,
                        (dr) -> new Repository1())
                .register(
                        SERVICE1,
                        (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class))
                );

        IRepository repository1 = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository1);

        IService service1 = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotNull(service1);

        assertTrue(repository1 != service1.getRepositories().get(0));

        IService service1Singleton = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotNull(service1Singleton);
        assertTrue(service1 == service1Singleton);
    }

    @Test
    @DisplayName("Registrando como singleton un objeto y como prototype el objeto en el que se inyecta.")
    public void resolveSingletonObjectIntoPrototypeObject() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1())
                .register(
                        SERVICE1,
                        DependencyFactory.DependencyType.PROTOTYPE,
                        (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class))
                );

        IRepository repository1 = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository1);

        IService service1 = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotNull(service1);

        assertTrue(repository1 == service1.getRepositories().get(0));

        IService service1Prototype = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotNull(service1Prototype);
        assertTrue(service1 != service1Prototype);
    }

    @Test
    @DisplayName("Se registra un objeto antes que sus dependencias.")
    public void registerObjectBeforeDependencies() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        SERVICE1,
                        (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class))
                )
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1()
                );

        IService service1 = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotNull(service1);
        assertNotNull(service1.getRepositories().get(0));
    }

    @Test
    @DisplayName("Se intenta registrar 2 veces un factory con el mismo nombre.")
    public void registerMultipleTimesSameFactoryName() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1()
                )
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository2()
                );

        IRepository repository2 = iocContainer.resolve(REPOSITORY1, Repository2.class);
        assertNotNull(repository2);
        assertTrue(repository2 instanceof Repository2);
        assertFalse(repository2 instanceof Repository1);
    }

    @Test
    @DisplayName("Se intenta registrar 2 veces un factory con el mismo nombre pero sin utilizar 'Logger' en la creación del " +
    "'IocContainer'. El funcionamiento debe ser el mismo solo que no se muestra mensaje de log avisando de la sobreescritura del " +
    "registro de la dependencia.")
    public void registerMultipleTimesSameFactoryNameButWithoutLogger() {
        IocContainer iocContainer = IocContainerFactory.newInstance(null);
        iocContainer
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1()
                )
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository2()
                );

        IRepository repository2 = iocContainer.resolve(REPOSITORY1, Repository2.class);
        assertNotNull(repository2);
        assertTrue(repository2 instanceof Repository2);
        assertFalse(repository2 instanceof Repository1);
    }

    @Test
    @DisplayName("Se intenta registrar una dependencia enviando como parámetro 'DependencyFactory' a 'null'.")
    public void registerWithParametersNullOrEmpty(){
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register(null, null);
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register(null, null,null);
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register(null, (dr) -> new Object());
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register(null, DependencyFactory.DependencyType.SINGLETON, (dr) -> new Object());
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register("", null,null);
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register("", (dr) -> new Object());
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register("", DependencyFactory.DependencyType.SINGLETON, (dr) -> new Object());
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register(REPOSITORY1, null,(dr) -> new Object());
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register(REPOSITORY1, null);
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.register(REPOSITORY1, DependencyFactory.DependencyType.PROTOTYPE, null);
        });
    }

    @Test
    @DisplayName("Se intenta resolver una dependencia enviando los parámetros 'name' y/o 'classObject' a 'null'.")
    public void resolveWithParametersNull(){
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER)
                .<IRepository>register(
                        REPOSITORY1,
                        (dr) -> new Repository1()
                );

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.resolve(null, Repository1.class);
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.resolve(REPOSITORY1, null);
        });

        assertThrows(IocDependencyException.class, () -> {
            iocContainer.resolve(null, null);
        });
    }

    @Test
    @DisplayName("Se muestran por consola todas las dependencias resueltas dentro del contenedor de dependencias.")
    public void showIocContainerDependenciesContent(){
        IocContainer iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(SERVICE1, Service1.class);
        iocContainer.resolve(SERVICE2, Service2.class);
        iocContainer.resolve(CONTROLLER, Controller.class);

        iocContainer.showContent();
    }

    @Test
    @DisplayName("Se muestran por consola todas las dependencias resueltas dentro del contenedor de dependencias." +
            "No se utiliza 'Logger' en la creación del 'IocContainer', el funcionamiento debe ser el mismo pero sin hacer log de los mensajes.")
    public void showIocContainerDependenciesContentWithoutLogger(){
        IocContainer iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, null);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(SERVICE1, Service1.class);
        iocContainer.resolve(SERVICE2, Service2.class);
        iocContainer.resolve(CONTROLLER, Controller.class);

        iocContainer.showContent();
    }

    private IocContainer getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType dependencyType, Logger logger) {
        return IocContainerFactory.newInstance(logger)
                .<IRepository>register(
                                REPOSITORY1,
                                dependencyType,
                                (dr) -> new Repository1())
                .<IRepository>register(
                                REPOSITORY2,
                                dependencyType,
                                (dr) -> new Repository2()
                )
                .<IService>register(
                                SERVICE1,
                                dependencyType,
                                (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class))
                )
                .<IService>register(
                                SERVICE2,
                                dependencyType,
                                (dr) -> new Service2(
                                        dr.resolve(REPOSITORY1, Repository1.class),
                                        dr.resolve(REPOSITORY2, Repository2.class))
                )
                .<Controller>register(
                                CONTROLLER,
                                dependencyType,
                                (dr) -> new Controller(
                                        dr.resolve(REPOSITORY1, Repository1.class),
                                        dr.resolve(SERVICE1, Service1.class),
                                        dr.resolve(SERVICE2, Service2.class))
                );
    }

    @Test
    @DisplayName("Se prueba la carga automática de todas las dependencias correctamente registradas. Las dependencias se registras como 'singleton'.")
    public void loadContentWithDependenciesRegisteredSingletonOk(){
        IocContainer iocContainer= getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER)
                .loadContent(true, true);

        Controller controller1 = iocContainer.resolve(CONTROLLER, Controller.class);
        Controller controller2 = iocContainer.resolve(CONTROLLER, Controller.class);
        assertEquals(controller1, controller2);
    }

    @Test
    @DisplayName("Se prueba la carga automática de todas las dependencias correctamente registradas. Las dependencias se registras como 'singleton'." +
            "No se utiliza 'Logger' al crear la instancia de 'IocContainer' por lo que el funcionamiento debe ser el mismo pero si mostrar mensajes de Log.")
    public void loadContentWithDependenciesRegisteredSingletonOkWithoutLogger(){
        IocContainer iocContainer= getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, null)
                .loadContent(true, true);

        Controller controller1 = iocContainer.resolve(CONTROLLER, Controller.class);
        Controller controller2 = iocContainer.resolve(CONTROLLER, Controller.class);
        assertEquals(controller1, controller2);
    }

    @Test
    @DisplayName("Se prueba la carga automática de todas las dependencias correctamente registradas. Las dependencias se registran como 'prototype'.")
    public void loadContentWithDependenciesRegisteredPrototypeOk(){
        IocContainer iocContainer= getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.PROTOTYPE, LOGGER)
                .loadContent(true, true);

        Controller controller1 = iocContainer.resolve(CONTROLLER, Controller.class);
        Controller controller2 = iocContainer.resolve(CONTROLLER, Controller.class);
        assertNotEquals(controller1, controller2);
    }

    @Test
    @DisplayName("Se prueba la carga automática de todas las dependencias correctamente registradas. Las dependencias se registran como 'prototype'." +
            "No se utiliza 'Logger' al crear la instancia de 'IocContainer' por lo que el funcionamiento debe ser el mismo pero si mostrar mensajes de Log.")
    public void loadContentWithDependenciesRegisteredPrototypeOkWithoutLogger(){
        IocContainer iocContainer= getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.PROTOTYPE, null)
                .loadContent(true, true);

        Controller controller1 = iocContainer.resolve(CONTROLLER, Controller.class);
        Controller controller2 = iocContainer.resolve(CONTROLLER, Controller.class);
        assertNotEquals(controller1, controller2);
    }

    @Test
    @DisplayName("Se prueba la carga automática de todas las dependencias correctamente registradas. Las dependencias se registran como 'singleton' y 'prototype'.")
    public void loadContentWithDependenciesRegisteredSingletonAndPrototypeOk(){
        IocContainer iocContainer= IocContainerFactory.newInstance(LOGGER)
                .register(
                        REPOSITORY1,
                        DependencyFactory.DependencyType.SINGLETON,
                        (dr) -> new Repository1()
                )
                .register(
                        SERVICE1,
                        DependencyFactory.DependencyType.PROTOTYPE,
                        (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class))
                )
                .loadContent(true, true);

        IRepository repository1 = iocContainer.resolve(REPOSITORY1, Repository1.class);
        IService service1 = iocContainer.resolve(SERVICE1, Service1.class);
        assertEquals(repository1, service1.getRepositories().get(0));

        IService service1Prototype = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotEquals(service1, service1Prototype);
        assertEquals(service1.getRepositories().get(0), service1Prototype.getRepositories().get(0));

        iocContainer= IocContainerFactory.newInstance(LOGGER)
                .register(
                        REPOSITORY1,
                        DependencyFactory.DependencyType.PROTOTYPE,
                        (dr) -> new Repository1()
                )
                .register(
                        SERVICE1,
                        DependencyFactory.DependencyType.SINGLETON,
                        (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class))
                )
                .loadContent(true, true);

        repository1 = iocContainer.resolve(REPOSITORY1, Repository1.class);
        service1 = iocContainer.resolve(SERVICE1, Service1.class);
        assertNotEquals(repository1, service1.getRepositories().get(0));

        IService service1Singleton = iocContainer.resolve(SERVICE1, Service1.class);
        assertEquals(service1, service1Singleton);
        IRepository repository1Prototype = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotEquals(repository1, repository1Prototype);
    }

    @Test
    @DisplayName("Se prueba la carga automática de dependencias que no han sido registradas.")
    public void loadContentWithDependenciesNotRegistered(){
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                    REPOSITORY1,
                    (dr) -> new Repository1()
                )
                .register(
                        SERVICE2,
                        (dr) -> new Service2(
                                dr.resolve(REPOSITORY1, Repository1.class),
                                dr.resolve(REPOSITORY2, Repository2.class))
                );

        assertThrows(IocDependencyFactoryNotFoundException.class, () -> {
            iocContainer.loadContent(true, true);
        });
    }

    @Test
    @DisplayName("Se prueba la carga automática de dependencias circulares registradas.")
    public void loadContentWithCircularDependencies(){
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        SERVICE_CIRCULAR1,
                        (dr) -> new ServiceCircular1(
                                dr.resolve(SERVICE_CIRCULAR2, ServiceCircular2.class))
                )
                .register(
                        SERVICE_CIRCULAR2,
                        (dr) -> new ServiceCircular2((
                                dr.resolve(SERVICE_CIRCULAR1, ServiceCircular1.class))
                ));

        assertThrows(IocCircularDependencyException.class, () -> {
            iocContainer.loadContent(true, true);
        });
    }

    @Test
    @DisplayName("Se prueba la carga automática de dependencias inyectando en el constructor de una de ellas una dependencia " +
    "con tipo de dato incompatible con el esperado (no se puede hacer casting).")
    public void loadContentWithIncompatibleDependencyClassInConstructorInjection(){
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1())
                .register(
                        SERVICE1,
                        (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class)))
                .register(
                        SERVICE2,
                        (dr) -> new Service2(dr.resolve(REPOSITORY1, Repository1.class), dr.resolve(SERVICE1, Repository2.class)));

        //Para que falle se está pasando como segundo parámetro del constructor de 'Service2' la dependencia del 'Service1' cuando debería ser del tipo 'IRepository'.
        assertThrows(IocDependencyCastingException.class, () -> {
            iocContainer.loadContent(true, true);
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //TESTs para comportamiento de la clase 'DependencyResolver' pero se comprueba toda la implementación desde la clase 'IocContainer':
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Test
    @DisplayName("Se registran varios objetos donde existe una dependencia circular (A depende de B y B depende de A) y" +
            "se comprueba que a la hora de resolver dichas dependencias se produce una 'IocCircularDependencyException' " +
            "(en caso contrario se produciría un bucle infinito o 'StackOverflowException' de Java).")
    public void registerCircularDependenciesAndTryToResolve() {
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        SERVICE_CIRCULAR1,
                        (dr) -> new ServiceCircular1(dr.resolve(SERVICE_CIRCULAR2, ServiceCircular2.class))
                )
                .register(
                        SERVICE_CIRCULAR2,
                        (dr) -> new ServiceCircular2(dr.resolve(SERVICE_CIRCULAR1, ServiceCircular1.class))
                );

        assertThrows(IocCircularDependencyException.class, () -> {
            iocContainer.resolve(SERVICE_CIRCULAR1, ServiceCircular1.class);
        });

        iocContainer
                .register(
                        SERVICE_CIRCULAR3,
                        (dr) -> new ServiceCircular3(dr.resolve(SERVICE_CIRCULAR3, ServiceCircular3.class))
                );

        assertThrows(IocCircularDependencyException.class, () -> {
            iocContainer.resolve(SERVICE_CIRCULAR3, ServiceCircular3.class);
        });
    }

    @Test
    @DisplayName("Se registran varios objetos como singletons donde una de las dependencias se inyecte en varios objetos y se " +
            "resuelven las dependencias en distintos órdenes para comprobar que no se detecta una falsa dependencia circular.")
    public void registerNotCircularDependenciesSingletonAndTryToResolve() {
        IocContainer iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(SERVICE2, Service2.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER);
        iocContainer.resolve(SERVICE1, Service1.class);
        iocContainer.resolve(REPOSITORY1, Repository1.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
        iocContainer.resolve(SERVICE1, Service1.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER);
        iocContainer.resolve(SERVICE2, Service2.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(REPOSITORY1, Repository1.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(SERVICE1, Service1.class);
        iocContainer.resolve(SERVICE2, Service2.class);
        iocContainer.resolve(CONTROLLER, Controller.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.SINGLETON, LOGGER);
        iocContainer.resolve(CONTROLLER, Controller.class);
        iocContainer.resolve(SERVICE2, Service2.class);
        iocContainer.resolve(SERVICE1, Service1.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
    }

    @Test
    @DisplayName("Se registran varios objetos como prototypes donde una de las dependencias se inyecte en varios objetos y se " +
            "resuelven las dependencias en distintos órdenes para comprobar que no se detecta una falsa dependencia circular.")
    public void registerNotCircularDependenciesPrototypeAndTryToResolve() {
        IocContainer iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.PROTOTYPE, LOGGER);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(SERVICE2, Service2.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.PROTOTYPE, LOGGER);
        iocContainer.resolve(SERVICE1, Service1.class);
        iocContainer.resolve(REPOSITORY1, Repository1.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.PROTOTYPE, LOGGER);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
        iocContainer.resolve(SERVICE1, Service1.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.PROTOTYPE, LOGGER);
        iocContainer.resolve(SERVICE2, Service2.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(REPOSITORY1, Repository1.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.PROTOTYPE, LOGGER);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(SERVICE1, Service1.class);
        iocContainer.resolve(SERVICE2, Service2.class);
        iocContainer.resolve(CONTROLLER, Controller.class);

        iocContainer = getNewContainerWithAllDepenciesRegistered(DependencyFactory.DependencyType.PROTOTYPE, LOGGER);
        iocContainer.resolve(CONTROLLER, Controller.class);
        iocContainer.resolve(SERVICE2, Service2.class);
        iocContainer.resolve(SERVICE1, Service1.class);
        iocContainer.resolve(REPOSITORY2, Repository2.class);
        iocContainer.resolve(REPOSITORY1, Repository1.class);
    }

    @Test
    @DisplayName("Se intenta resolver una dependencia que no se ha registrado previamente.")
    public void resolveDependencyNotRegistered() {
        assertThrows(IocDependencyFactoryNotFoundException.class, () -> {
            IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
            iocContainer.resolve(REPOSITORY1, Repository1.class);
        });

        assertThrows(IocDependencyFactoryNotFoundException.class, () -> {
            IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
            iocContainer
                    .register(
                            REPOSITORY1,
                            (dr) -> new Repository1())
                    .register(
                            SERVICE1,
                            (dr) -> null);
            iocContainer.resolve(SERVICE2, Service2.class);
        });
    }

    @Test
    @DisplayName("Se intenta resolver una dependencia que se ha registrado previamente pero con valor a 'null'.")
    public void resolveDependencyRegisteredWithNullValue() {
        assertThrows(IocDependencyNotFoundException.class, () -> {
            IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
            iocContainer.register(
                    REPOSITORY1,
                    (dr) -> null);
            iocContainer.resolve(REPOSITORY1, Repository1.class);
        });

        assertThrows(IocDependencyNotFoundException.class, () -> {
            IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
            iocContainer
                    .register(
                        REPOSITORY1,
                        (dr) -> null)
                    .register(
                        SERVICE1,
                            (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class))
                    );
            iocContainer.resolve(SERVICE1, Service1.class);
        });

        assertThrows(IocDependencyNotFoundException.class, () -> {
            IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
            iocContainer
                    .register(
                            REPOSITORY1,
                            (dr) -> new Repository1())
                    .register(
                            SERVICE1,
                            (dr) -> null);
            iocContainer.resolve(SERVICE1, Service1.class);
        });

        assertThrows(IocDependencyNotFoundException.class, () -> {
            IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
            iocContainer
                    .register(
                            REPOSITORY1,
                            (dr) -> new Repository1())
                    .register(
                            REPOSITORY2,
                            (dr) -> new Repository2())
                    .register(
                            SERVICE2,
                            (dr) -> null);
            iocContainer.resolve(SERVICE2, Service2.class);
        });
    }

    @Test
    @DisplayName("Se registra una dependencia con un tipo de clase y se resuelve como objeto de la misma clase o de una clase/interfaz " +
            "de la que hereda para  comprobar que se realiza el casting correctamente.")
    public void resolveRegisteredObjectIntoObjectSameClassOrParentClass(){
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1());

        IRepository repository = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository);

        repository = iocContainer.resolve(REPOSITORY1, IRepository.class);
        assertNotNull(repository);

        Repository1 repository1 = iocContainer.resolve(REPOSITORY1, Repository1.class);
        assertNotNull(repository1);

        //Es necesario hacer un casting (unboxing) para este caso:
        repository1 = (Repository1)iocContainer.resolve(REPOSITORY1, IRepository.class);
        assertNotNull(repository1);
    }

    @Test
    @DisplayName("Se registra una dependencia con un tipo de clase y se intenta resolver como un objeto con un tipo de clase incompatible " +
            "en la que no se puede hacer 'casting' (no hereda de esa clase o no implementa dicha interfaz).")
    public void resolveRegisteredObjectIntoOtherObjectType(){
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .register(
                        REPOSITORY1,
                        (dr) -> new Repository1());

        assertThrows(IocDependencyCastingException.class, () -> {
            IService notServiceDependency = iocContainer.resolve(REPOSITORY1, Service1.class);
        });


        assertThrows(IocDependencyCastingException.class, () -> {
            iocContainer.resolve(REPOSITORY1, Service1.class);
        });

        assertThrows(IocDependencyCastingException.class, () -> {
            IService notServiceDependency = iocContainer.resolve(REPOSITORY1, IService.class);
        });

        assertThrows(IocDependencyCastingException.class, () -> {
            iocContainer.resolve(REPOSITORY1, IService.class);
        });
    }

    @Test
    @DisplayName("Se registra una dependencia con un tipo de clase y se intenta resolver como un objeto con un tipo de clase incompatible " +
            "en la que no se puede hacer 'casting' (no hereda de esa clase o no implementa dicha interfaz) a la hora de inyectarlo via constructor " +
            "en otra dependencia.")
    public void resolveRegisteredObjectIntoOtherObjectTypeWhenConstructorInjection(){
        IocContainer iocContainer = IocContainerFactory.newInstance(LOGGER);
        iocContainer
                .<IRepository>register(
                        REPOSITORY1,
                        (dr) -> new Repository1())
                .<IRepository>register(
                        REPOSITORY2,
                        (dr) -> new Repository2())
                .<Service1>register(
                        SERVICE1,
                        (dr) -> new Service1(dr.resolve(REPOSITORY1, Repository1.class)))
                .<Service2>register(
                        SERVICE2,
                        (dr) -> new Service2(dr.resolve(REPOSITORY1, Repository1.class), dr.resolve(SERVICE1, Repository2.class)));

        //Para que falle se está pasando como segundo parámetro del constructor de 'Service2' la dependencia del 'Service1' cuando debería ser del tipo 'IRepository'.
        assertThrows(IocDependencyCastingException.class, () -> {
            IService notServiceDependency = iocContainer.resolve(SERVICE2, Service2.class);
        });
    }

}
