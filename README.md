<div style="width:1000px; height:500px;">
![PisthorIoc](http://www.atopecode.net/content/images/size/w2000/2022/02/pisthor---color---1280x640.png)
</div>

![Made with Java](https://img.shields.io/badge/made%20with-java-orange)
![Ioc - Depdendency Injection](https://img.shields.io/badge/dependency%20injection-ioc-yellow)
![Licencse - Apache 2.0](https://img.shields.io/badge/license-apache%202.0-blue)
![GitHub Release (Lasted by date)](https://img.shields.io/github/v/release/SilverioMG/PisthorIoc)
<!--Botón para donaciones en BuyMeACoffee:-->
<a href="https://www.buymeacoffee.com/atopecode" target="_blank">
    <img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" >
</a>


# PisthorIoc - Contenedor de Dependencias sencillo y rápido

PisthorIoc es un Contenedor para Inyección de Dependencias para proyectos Java.

- Es simple de utilizar y ligero.
- No utiliza reflexión. No hay magia, solo código.
- ThreadSafe.
- Inyección de dependencias vía constructor.
- FluentApi.
- Permite registrar dependencias de tipo *Singleton* y *Prototype*.
- Es ideal para utilizar en microframeworks como <a href="https://javalin.io/" target="_blank">Javalin</a> o <a href="https://sparkjava.com/" target="_blank">Spark</a>.
- Cada dependencia se registra con un identificador único de tipo 'String' que será el mismo que se utilice a la hora de resolver/inyectar cada dependencia.
- Detecta errores comunes como intentar utilizar dependencias no registradas, registro de dependencias cíclicas, error en conversión de tipos al inyectar dependencias, aviso al sobreescribir el registro de alguna dependencia...
- Permite resolver las dependencias a medida que se van necesitando en la ejecución de nuestro código (lazy load) o realizar una carga completa cuando inicia nuestro proyecto de todas las dependencias registradas (recomendado).
- A la hora de hacer *tests* se puede sobreescribir directamente el registro de cualquier dependencia (servicio, repositorio...) para sustituirla por un *mock* o la correspondiente implementación de pruebas.

```
final String REPOSITORY = "repository";
final String SERVICE1 = "service1";
final String SERVICE2 = "service2";
final String CONTROLLER = "controller";

Logger logger = LoggerFactory.getLogger(IocContainer.class);
IocContainer iocContainer = IocContainerFactory.singleton()
	.setLogger(logger);
	
iocContainer
    .register(
        REPOSITORY,  
        (dr) -> new Repository())
    .register(
        SERVICE1,
        (dr) -> new Service1(dr.resolve(REPOSITORY , IRepository.class)))
    .register(
        SERVICE2,
        (dr) -> new Service2(dr.resolve(REPOSITORY , IRepository.class)))
    .register(
        CONTROLLER,
        (dr) -> new Controller(
            dr.resolve(SERVICE1, IService.class),
            dr.resolve(SERVICE2, IService.class)))
    .loadContent();
	
Controller controller = iocContainer.resolve(CONTROLLER, Controller.class);
```


## Importar librería PisthorIoc en tu proyecto:

### Compilar librería en local con Maven
Por el momento la librería *PisthorIoc* no está subida a ningún repositorio público como *MavenCentral* por lo que tendrás que compilar la librería e instalarla en su repositorio local.

Primero debes descargar la última versión del proyecto [PisthorIoc](https://github.com/silveriomg/pisthorioc/releases/latest).
A continuación, ejecuta el siguiente comando maven dentro del directorio raíz del proyecto:
```
mvn clean install
```
El proyecto se compilará y guardará el archivo *.jar* generado en tu directorio maven local *.m2*.


### Añadir librería en tu proyecto

Si tu proyecto utiliza Maven:
```
<dependency>  
    <groupId>net.atopecode</groupId>  
    <artifactId>pisthorioc</artifactId>  
    <version>1.0.0</version>  
</dependency>
```

Si tu proyecto utiliza  Gradle:
```
implementation "net.atopecode:pisthorioc:1.0.0"
```


### Uso de Logger para mostrar mensajes

PisthorIoc permite utilizar logger (o no) según tus necesidades. En caso de no utilizar logger, la librería funciona igualmente pero no muestra advertencias ni mensajes de error (pero seguirá lanzando las correspondientes *Exceptions* en caso de error).

Es aconsejable utilizar siempre un logger para ver los mensajes y detectar posibles errores a medida que se registran y resuelven las dependencias.

PisthorIoc necesita un logger que implemente la interfaz *slf4j*.
En este ejemplo utilizaremos el logger *logback* para que nuestro contenedor de dependencias haga uso de él.
Añade las siguientes dependencias en tu proyecto:


Si tu proyecto utiliza Maven:
```
<dependency>  
    <groupId>ch.qos.logback</groupId>  
    <artifactId>logback-classic</artifactId>  
    <version>1.2.10</version>  
</dependency>  
<dependency>  
    <groupId>ch.qos.logback</groupId>  
    <artifactId>logback-core</artifactId>  
    <version>1.2.10</version>  
</dependency>
```

Si tu proyecto utiliza  Gradle:
```
implementation "ch.qos.logback:logback-classic:1.2.10"
implementation "ch.qos.logback:logback-core:1.2.10"
```

### Importar módulo Java de la librería PisthorIoc en tu proyecto
La librería PisthorIoc está implementada utilizando *<a href="https://www.oracle.com/es/corporate/features/understanding-java-9-modules.html" target="_blank">módulos de Java</a>*.
Si tu proyecto utiliza *módulos de java* debes añadir la siguiente línea en tu archivo *module-info.java*:
```
requires net.atopecode.pisthorioc.module;
```

Si tu proyecto no utiliza *módulos de java* no debes hacer nada especial para utilizar PisthorIoc, 
simplemente importa el paquete *net.atopecode.pisthorioc.ioccontainer.** en tu código como harías normalmente.


## Utilizando PisthorIoc en tu proyecto:
El uso del contendor de dependencias *PisthorIoc* se puede dividir en los siguientes pasos:
1. [Crear contenedor de dependencias.](#1-Crear-contenedor-de-dependencias)
2. [Registrar todas las dependencias necesarias.](#2-Registrar-todas-las-dependencias-necesarias)
3. [Carga automática del contenedor con todas las dependencias registradas.](#3-Carga-automática-del-contenedor-con-todas-las-dependencias-registradas)
4. [Uso del contenedor para resolver dependencias.](#4-Uso-del-contenedor-para-resolver-dependencias)

### 1. Crear contenedor de dependencias
#### Crear un contenedor como *Singleton* (el mismo objeto en memoria durante toda la ejecución del programa):
```
Logger logger = LoggerFactory.getLogger(IocContainer.class);
IocContainer container = IocContainerFactory.singleton()
    .setLogger(logger);
```
El método static *'IocContainerFactory.singleton()'* siempre nos devolverá la misma instancia (mismo objeto) de nuestro contenedor de dependencias.
Por lo tanto, siempre podremos acceder a nuestro contenedor singleton desde cualquier parte de nuestro código utilizando el método static *'IocContainerFactory.singleton()'* sin necesidad de guardar el objeto contenedor en
un campo o ir pasándolo como parámetro de una parte del código a otra:
```
IocContainerFactory.singleton()
    .register(...);
	
IocContainerFactory.singleton()
    .resolve(...);
```

#### Crear un contenedor nuevo si es necesario (un nuevo objeto en memoria):
``` 
Logger logger = LoggerFactory.getLogger(IocContainer.class);
IocContainer container = IocContainerFactory.newInstance()
    .setLogger(logger);
```
El método static *'IocContainerFactory.newInstance()'* siempre nos devolverá una nueva instancia (distinto objeto) de un contenedor de dependencias.


#### Usando un Logger:
En los ejemplos anteriores hemos creado siempre nuestro contenedor asignándole un logger.
Solo se permite asignar un logger a cada IocContainer una vez, es decir, si un IocContainer ya tiene un logger asignado y se intenta asignar de nuevo un logger, se produce una Exception.

Se puede comprobar si un contenedor tiene un logger ya asignado en cualquier momento:
```
IocContainer container = IocContainerFactory.singleton()
container.hasLogger(); //Devuelve 'false'.
container.setLogger(logger);
container.hasLogger(); //Devuelve 'true'.

//Se produce una 'Exception' porque ya se ha asignado previamente un logger al contenedor.
container.setLogger(logger); 
```

#### Sin usar Logger:
```
IocContainer container1 = IocContainerFactory.singleton();
IocContainer container2 = IocContainerFactory.newInstance();
```
Al no asignar *logger* al contenedor, su funcionamiento será el mismo, pero no se mostrarán mensajes de advertencia o de errores (pero se siguen lanzando las correspondientes Exceptions).



### 2. Registrar todas las dependencias necesarias
*PisthorIoc* utiliza inyección de dependencias vía constructor. Por lo que debe diseñar todos los objetos que vaya a utilizar como dependencias (controladores, servicios, repositorios...) para que sus constructores reciban las dependencias necesarias en cada momento.

Para nuestro ejemplo vamos a definir las siguientes clases (con sus correspondientes interfaces) que actuarán como dependencias dentro de nuestro contenedor y que se inyectarán unas en otras usando sus constructores:
``` 
public interface IRepository {
    public void methodRepository();
}
``` 
``` 
public interface IService {
    public void methodService();
}
``` 
``` 
public class Repository implements IRepository {
    public Repository(){
    }

    @Override
    public void methodRepository(){
    }
}
``` 
``` 
public class Service1 implements IService {
    private final IRepository repository;

    public Service1(IRepository repository){
        this.repository = repository;
    }
 
    @Override
    public void methodService(){
    }
}
``` 
``` 
public class Service2 implements IService {
    private final IRepository repository;

    public Service2(IRepository repository){
        this.repository = repository;
    }
 
    @Override
    public void methodService(){
    }
}
``` 
``` 
public class Controller {
    private final IService service1;
    private final IService service2;
	
    public Controller(IService service1, IService service2){
        this.service1 = service1;
        this.service2 = service2;
    }
    
    ...
}
``` 

Una vez definidas las clases para nuestras dependencias vamos a registrarlas dentro del contenedor de dependencias indicando como debe ser la construcción de cada una de ellas e inyectando las dependencias necesarias en cada caso.

*Registrar* una dependencia en *PisthorIoc* consiste en indicarle al contenedor como debe crearse un objeto
y las dependencias que necesita para ello. Cuando se necesite *resolver* (recuperar) un objeto, el contenedor sabrá como debe crearlo inyectando las dependencias que necesita en su constructor.

```
final String REPOSITORY = "repository";
final String SERVICE1 = "service1";
final String SERVICE2 = "service2";
final String CONTROLLER = "controller";

IocContainerFactory.singleton()
    .register(  
        REPOSITORY ,  
        (dr) -> new Repository())
    .register(  
        SERVICE1,  
        (dr) -> new Service1(dr.resolve(REPOSITORY , IRepository.class)))
    .register(  
        SERVICE2,  
        (dr) -> new Service2(dr.resolve(REPOSITORY , IRepository.class)))
    .register(  
        CONTROLLER,  
        (dr) -> new Controller(
            dr.resolve(SERVICE1, IService.class),
            dr.resolve(SERVICE2, IService.class)));
```



### 3. Carga automática del contenedor con todas las dependencias registradas
Partiendo del apartado anterior, ahora mismo en nuestro contenedor están registradas todas las dependencias que necesitamos, pero aún no se ha creado ningún objeto. El contenedor simplemente tiene las instrucciones necesarias para crear los objetos y sus dependencias cuando sea necesario recuperarlas (resolverlas).

PisthorIoc utiliza un enfoque *Lazy Load* por defecto para resolver las dependencias.
Cada vez que se intente resolver (recuperar) una dependencia para utilizarla en nuestro código, en ese justo momento el contenedor intentará crear el objeto y todas sus dependencias necesarias.

Utilizando el enfoque *Lazy Load*, es posible que se produzcan errores a la hora de resolver alguna dependencia debido a que se ha registrado de forma incorrecta (no se ha registrado alguna dependencia necesaria para su construcción, registro de dependencias cíclicas...). Con lo cual puede que la inyección falle de forma no controlada sin saber justo el momento en el que se producirá el error debido a que nuestro programa ya lleva un tiempo ejecutándose.

Para evitar esto, **es preferible realizar una carga automática de todas las dependencias justo al finalizar el registro**.
Con la carga automática evitamos que se produzcan errores inesperados, ya que podemos controlar justo el momento en el que se pueden producir.
Lo normal es que el registro de las dependencias y la carga automática se hagan al inicio de nuestro programa o servicio.

Realizar carga automática después del registro de dependencias:
```
IocContainerFactory.singleton()
    .register(  
        "repository",  
        (dr) -> new Repository())
    .register(  
        "service1",  
        (dr) -> new Service1(dr.resolve("repository", IRepository.class)))
    .loadContent();
```

Añadimos el método *loadContent()* justo al finalizar el registro de las dependencias.

### 4. Uso del contenedor para resolver dependencias
Una vez que tenemos nuestras *dependencias registradas* en el punto 2, pasamos a resolver las dependencias para utilizarlas en nuestro proyecto (independientemente de si hemos decidido realizar la *carga automática* del punto 3 o no).

Obtenemos el objeto correspondiente a la dependencia registrada con el nombre "controller":
```
IocContainer container = IocContainerFactory.singleton();
Controller controller = container.resolve("controller", Controller.class);
```

La primera vez que se intenta resolver la dependencia, a su vez, se crean automáticamente el resto de dependencias necesarias para inyectarlas en su constructor y finalmente se devuelve el objeto de tipo 'Controller' con todas sus dependencias inyectadas.

Al estar registrado como *Singleton*, la próxima vez que se intente resolver la dependencia de tipo "controller", el contenedor devolverá siempre el mismo objeto en vez de crear uno nuevo.

A continuación resolvemos el objeto registrado como singleton con el nombre "service1":
```
IService service1 = container.resolve("service1", IService.class);
```
El contenedor nos devuelve el mismo objeto de tipo 'Service1' que se creó cuando resolvimos el objeto con nombre "controller", ya que hizo falta el 'Service1' para inyectarlo en el constructor de 'Controller' y nuestro contenedor tuvo que crear el objeto en ese momento.



## Uso avanzado:
#### Las dependencias se pueden registrar como 'Singleton' (por defecto) o 'Prototype':
- Singleton: El objeto se crea una vez y siempre que se recupere su valor se devolverá la misma instancia (el mismo objeto).
- Prototype: Cada vez que se quiera recuperar un objeto, se crea una nueva instancia (nuevo objeto).

*Sino se indica nada, por defecto todos los objetos se registran como dependencias de tipo *Singleton*.*

Registrar un objeto como *Singleton*:
```
//Por defecto sino se indica nada, se registra el objeto como 'Singleton'.
container
    .register(  
        "repository",  
        (dr) -> new Repository());

//Indicando de forma explícita el registro como 'Singleton'.
container
    .register(  
        "repository",
        DependencyFactory.DependencyType.SINGLETON,
        (dr) -> new Repository());
```
Registrar un objeto como *Prototype*:
```
//Siempre hay que indicar forma explícita el registro como 'Prototype'.
container
    .register(  
        "repository",
        DependencyFactory.DependencyType.PROTOTYPE,
        (dr) -> new Repository());
```

#### No importa el orden de registro de nuestras dependencias:
A la hora de registrar las dependencias en nuestro contenedor no importa el orden en el que lo hagamos.
Lo único importante es que a la hora de resolver alguna dependencia, esta haya sido previamente registrada además de todas las demás dependencias que puedan ser necesarias para su construcción.

Por lo tanto podemos registrar las dependencias en cualquier orden:
```
final String REPOSITORY = "repository";
final String SERVICE1 = "service1";
final String SERVICE2 = "service2";
final String CONTROLLER = "controller";

IocContainerFactory.singleton()
    .register(  
        CONTROLLER,  
        (dr) -> new Controller(
            dr.resolve(SERVICE1, IService.class),
            dr.resolve(SERVICE2, IService.class)))
    .register(  
        SERVICE2,  
        (dr) -> new Service2(
            dr.resolve(REPOSITORY , IRepository.class)))
    .register(  
        SERVICE1,  
        (dr) -> new Service1(
            dr.resolve(REPOSITORY , IRepository.class)))
    .register(  
        REPOSITORY ,  
        (dr) -> new Repository())
    .loadContent();

Controller controller = IocContainerFactory.singleton()
    .resolve(CONTROLLER, Controller.class);
```

#### Mostrar las dependencias que guarda el contenedor:
La primera vez que se intenta *resolver o inyectar* una dependencia registrada como *singleton*, el contenedor crea su correspondiente objeto y lo almacena en su interior.
Para las dependencias registradas como *prototype* esto no sucede, ya que el contenedor crea un nuevo objeto cada vez que se intentan *resolver o inyectar* y por lo tanto no las almacena en su interior.

El método *showContent()* hace logging de todas las dependencias registradas como *singleton* que un contenedor tiene almacenadas. Es necesario que el contenedor tenga un *logger* asignado.
```
Logger logger = LoggerFactory.getLogger(IocContainer.class);
IocContainerFactory.singleton()
    .setLogger(logger)
    .showContent();
```

#### Sobreescribir el registro de una dependencia:
```
container
    .register(
        "repository",
        (dr) -> new Repository())
    .register(
        "repository",
        (dr) -> new RepositoryMock())
    .loadContent();
    
RepositoryMock repository = container.resolver("repository", IRepository.class);
```

Si registramos 2 o más veces el mismo objeto (utilizamos el mismo identificador de tipo string) como en el ejemplo anterior que hemos registrado 2 veces una dependencia con el mismo nombre *"repository"*, no se produce ningún error, pero el contenedor muestra un mensaje de log como aviso (en caso de que hayamos utilizado un logger al crear el contenedor).
La dependencia con el nombre "repository" quedará registrada con el último registro realizado, sobreescribiendo los registros anteriores.

*Para que el nuevo registro sobreescriba a sus antecesores y se aplique correctamente a la hora de resolver la dependencia, no se debe haber intentado resolver (recuperar) ninguna dependencia del contenedor ni haber realizado la carga automática antes del registro.
Ya que una vez que se crea el objeto de dicha dependencia, se guarda dentro del contenedor y este a su vez puede
ser una dependencia de otro objeto. Y aún cuando la dependencia puede estar declarada como Prototype y no se guarde dentro del contenedor, siempre puede haber sido inyectada en otra dependencia de tipo Singleton que siempre guardará una versión del objeto prototype que no corresponderá con el nuevo registro.*

Sobreescribir el registro de un objeto es realmente útil para realizar tests.
Podemos utilizar el mismo registro de dependencias de nuestro proyecto y en los tests sobreescribir solo aquellas dependencias que necesitamos mockear o sustituir su comportamiento.


#### Uso del contenedor para Testing (sobreescribiendo el registro de las dependencias):
Supongamos que en nuestro proyecto vamos a utilizar las siguientes clases y sus correspondientes interfaces:

```
public interface IEmailService {
    public void sendEmail();
}

public interface ICustomerService {
    public void sendEmailToCustomer();
}

public class EmailService implements IEmailService{
    @Override
    public void sendEmail(){
        //Código que envía un email de verdad.
        ...
    }
}

public class CustomerService implements ICustomerService{
    private final IEmailService emailService;
	
    public CustomerService(IEmailService emailService){
        this.emailService = emailService;
    }
	
    @Override
    public void sendEmailToCustomer(){
        emailService.sendEmail();
    }
}

public class EmailServiceMock implements IEmailService {
    @Override
    public void sendEmail(){
        //No hace nada.
        return;
    }
}
```

Utilizamos una clase 'IocContext' que inicialice nuestro contenedor 
y que se encargue de realizar el registro de todas las dependencias necesarias:
```
public class IocContext {
    public static void init(IocContainer iocContainer, boolean loadContent){
        Logger logger = LoggerFactory.getLogger(IocContainer.class);
        iocContainer
            .setLogger(logger)
            .register(
                "emailService",
                (dr) -> new EmailService())
            .register(
                "customerService",
                (dr) -> new CustomerService(dr.resolve("emailService", IEmailService.class)));
		
        if(loadContent) iocContainer.loadContent();
    }
}
```

*Es importante remarcar que en la línea donde se registra 'customerService', a la hora de resolver 'emailService' 
para inyectarlo en su constructor, el objeto de tipo 'class' que se envía como parámetro es del tipo de la interfaz 'IEmailService', 
no de la clase 'EmailService'. Se hace así porque a la hora de sobreescribir el registro de 'emailService' en nuestro test para 
utilizar 'EmailServiceMock', cuando el contenedor intente inyectarle a 'customerService' la dependencia de "emailService", esta será 
del tipo 'EmailServiceMock' y hará casting contra la interfaz 'IEmailService' por haber usado el parámetro correcto 'IEmailService.class' 
(si se usa como parámetro 'EmailService.class', se haría casting del objeto de tipo 'EmailServiceMock' contra el tipo 'EmailService' y fallaría).*

Al inicio de nuestro proyecto (no en los tests) lo normal sería utilizar la clase 'IocContext' definida anteriormente para registrar todas las dependencias necesarias
y también forzaremos la *carga automática* para comprobar que el registro es correcto.
En nuestro proyecto utilizaremos el contenedor de dependencias como Singleton, pero en los tests será mejor
crear una nueva instancia del contenedor por cada ejecución.
```
//Al inicio de nuestro proyecto:
IocContext.init(IocContainerFactory.singleton(), true);
```

A la hora de hacer un test necesitamos cambiar el comportamiento de la dependencia "emailService".
En producción se utilizará 'EmailService' para enviar los emails necesarios, pero a la hora de ejecutar nuestros tests no queremos que envíe ningún email y utilizaremos 'EmailServiceMock' en su lugar.

Al inicio del test sustituiremos el registro de la dependencia para el servicio de email por nuestro mock, así a la hora de resolver el servicio de email, el contenedor devolverá siempre nuestro mock.

Es importante remarcar que para que la sobrescritura del registro funcione correctamente, **en el test no se debe realizar la carga automática hasta que no se haya registrado nuestro servicio mock.**
Además, por cada tests que hagamos, es conveniente utilizar una nueva instancia del contenedor y realizar de nuevo el registro.
```
@Test
public void emailServiceTest(){
    IocContainer iocContainer = IocContainerFactory.newInstance(); //Nueva instancia del contenedor (no singleton).
    IocContext.init(iocContainer, false); //No se realiza la carga automática.
	
    //Sobreescribimos el registro de "emailService" para utilizar 'EmailServiceMock'.
    iocContainer
        .register(
            "emailService",
            (dr) -> new EmailServiceMock());
		
    //Realizamos la carga automática después de sobreescribir el registro de dependencias.
    iocContainer.loadContent();

    ICustomerService customerService = iocContainer.resolve("customerService", ICustomerService.class);
    customerService.sendEmailToCustomer(); //No hace nada.
	
    IEmailService emailService = iocContainer.resolve("emailService", IEmailService.class);
    assertTrue(emailService instanceof EmailServiceMock);
}
```

## Más ejemplos de uso de PisthorIoc

Para ver más ejemplos sobre el uso de *PisthorIoc* puede consultar los tests de la librería en el proyecto [PisthorIoc_Test](https://github.com/SilverioMG/PisthorIoc_Test).
