# PisthorIoc - Contenedor de Dependencias sencillo y rápido

PisthorIoc es un Contenedor para Inyección de Dependencias para proyectos Java.

- Es simple de utilizar y ligero.
- No utiliza reflexión. No hay magia, solo código.
- ThreadSafe.
- Inyección de dependencias vía constructor.
- FluentApi.
- Detecta errores comunes como intentar utilizar dependencias no registradas, registro de dependencias cíclicas, error en conversión de tipos al inyectar dependencias, aviso al sobreescribir el registro de alguna dependencia...
- Permite resolver las dependencias a medida que se van necesitando en la ejecución de nuestro código (lazy load) o realizar una carga completa cuando inicia nuestro proyecto (recomendado) de todas las dependencias registradas.
- Permite registrar dependencias de tipo *Singleton* y *Prototype*.
- Es ideal para utilizar en microfameworks como [Javalin](https://javalin.io/) o [Spark](https://sparkjava.com/).
- Cada dependencia se registra con un identificador único de tipo 'String'  que será el mismo que se utilice a la hora de resolver/inyectar cada dependencia.
- A la hora de hacer *tests* se puede sobreescribir directamente el registro de cualquier dependencia (servicio, repositorio...) para sustituirla por un *mock* o la correspondiente implementación de pruebas.


## Inicio rápido:

### Compilar librería en local con Maven
Por el momento la librería *PisthorIoc* no está subida a ningún repositorio público como *MavenCentral* por lo que tendrá que compilar la librería e instalarla en su repositorio local.

Una vez descargado el proyecto [PisthorIoc](https://github.com/SilverioMG/PisthorIoc) ejecute el siguiente comando maven dentro del directorio raíz del proyecto:
```
mvn clean install
```
El proyecto se compilará y guardará el archivo *.jar* generado en su directorio *.m2* local.


### Añadir librería PisthorIoc en su proyecto

Si su proyecto utiliza Maven:
```
<dependency>  
	<groupId>net.atopecode</groupId>  
	 <artifactId>pisthorioc</artifactId>  
	 <version>1.0.0</version>  
</dependency>
```

Si su proyecto utiliza  Gradle:
```
implementation "net.atopecode:pisthorioc:1.0.0"
```


### Uso de Logger para mostrar mensajes

PisthorIoc da la opción de utilizar logger o no según sus necesidades. En caso de no utilizar logger, la librería funciona igualmente pero no muestra advertencias ni mensajes de error (pero seguirá lanzando las correspondientes *Exceptions*).

Es aconsejable utilizar siempre un logger para ver los mensajes y detectar posibles errores a medida que se registran y resuelven las dependencias.

PisthorIoc necesita un logger que implemente la interfaz *slf4j*.
En este ejemplo utilizaremos el logger *logback* para que nuestro contenedor de dependencias haga uso de él.
Añada las siguientes dependencias en su proyecto:


Si su proyecto utiliza Maven:
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

Si su proyecto utiliza  Gradle:
```
implementation "ch.qos.logback:logback-classic:1.2.10"
implementation "ch.qos.logback:logback-core:1.2.10"
```

### Importar módulo Java de la librería PisthorIoc en su proyecto
La librería PisthorIoc está implementada utilizando *módulos de Java*.
Si su proyecto utiliza *módulos de java* debe añadir la siguiente línea en su archivo *module-info.java*:
```
requires net.atopecode.pisthorioc.module;
```

Si su proyecto no utiliza *módulos de java* no debe hacer nada especial para utilizar PisthorIoc, simplemente importe el paquete *net.atopecode.pisthorioc.ioccontainer.** en su código como haría normalmente.


## Utilizando PisthorIoc en su  proyecto:
El uso del contendor de dependencias *PisthorIoc* se puede dividir en los siguientes pasos:
1. Crear contenedor de dependencias (utilizando logger o no).
2. Registrar todas las dependencias necesarias.
3. Carga automática del contenedor con todas las dependencias registradas (opcional pero recomendado).
4. Uso del contenedor para resolver dependencias.

### 1. Crear contenedor de dependencias (utilizando logger o no)
>#### Crear un contenedor como *Singleton* (el mismo objeto en memoria durante toda la ejecución del programa):
```
Logger logger = LoggerFactory.getLogger(IocContainer.class);
IocContainer container = IocContainerFactory.singleton(logger);
```
El método static *'IocContainerFactory.singleton(logger)'* siempre nos devolverá la misma instancia (mismo objeto) de nuestro contenedor de dependencias.


#### Crear un contenedor nuevo si es necesario (un nuevo objeto en memoria):
``` 
Logger logger = LoggerFactory.getLogger(IocContainer.class);
IocContainer container = IocContainerFactory.newInstance(logger);
```
El método static *'IocContainerFactory.newInstance(logger)'* siempre nos devolverá una nueva instancia (distinto objeto) de un contenedor de dependencias.

#### Sin usar Logger:
```
IocContainer container1 = IocContainerFactory.singleton(null);
IocContainer container2 = IocContainerFactory.newInstance(null);
```
Al no utilizar *logger* en la construcción del contenedor, su funcionamiento será el mismo pero no se mostrarán mensajes de advertencia o de errores (pero se siguen lanzando las correspondientes Exceptions).


### 2. Registrar todas las dependencias necesarias:
*PisthorIoc* utiliza inyección de dependencias vía constructor. Por lo que debe diseñar todos los objetos que vaya a utilizar como dependencias (controladores, servicios, repositorios...) para que sus constructores reciban las dependencias necesarias en cada momento.

*Registrar* una dependencia en *PisthorIoc* consiste en indicarle al contenedor como debe de crearse un objeto
usando y las dependencias que necesita para ello. Así después cuando se necesite *resolver* (recuperar) un objeto, el contenedor sabe como debe de crearlo y las dependencias que necesita a su vez para inyectar en su constructor.

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

```
Logger logger = LoggerFactory.getLogger(IocContainer.class);
IocContainerFactory.singleton(logger)
	.register(  
        "repository",  
		(dr) -> new Repository())
	.register(  
        "service1",  
		(dr) -> new Service1(dr.resolve("repository", Repository.class)))
	.register(  
        "service2",  
		(dr) -> new Service2(dr.resolve("repository", Repository.class)))
	.register(  
        "controller",  
		(dr) -> new Controller(
					dr.resolve("service1", Service1.class))
					dr.resolve("service2", Service2.class)));
```



### 3. Carga automática del contenedor con todas las dependencias registradas (opcional pero recomendado).
Partiendo del apartado anterior, ahora mismo en nuestro contenedor están registradas todas las dependencias que necesitamos, pero aún no se ha creado ningún objeto. El contenedor simplemente tiene las instrucciones necesarias para crear los objetos y sus dependencias cuando sea necesario recuperarlas (resolverlas).

Cada vez que se intente resolver (recuperar) una dependencia para utilizarla en nuestro código, en ese justo momento el contenedor intentará crear el objeto y todas sus dependencias necesarias.

Es posible que se produzcan errores a la hora de resolver alguna dependencia debido a que se ha registrado de forma incorrecta (no se ha registrado alguna dependencia necesaria para su construcción, registro de dependencias cíclicas...). Con lo cual puede que la inyección falle de forma no controlada sin saber justo el momento en el que se producirá el error debido a que nuestro programa ya lleva un tiempo ejecutándose.

Para evitar esto, **es preferible realizar una carga automática de todas las dependencias justo al finalizar el registro**.
Con la carga automática evitamos que se produzcan errores inesperados ya que podemos controlar justo el momento en el que se pueden producir.
Lo normal es que el registro de las dependencias y la carga automática se hagan al inicio de nuestro programa o servicio.

Realizar carga automática después del registro de dependencias:
´´´
Logger logger = LoggerFactory.getLogger(IocContainer.class);
IocContainerFactory.singleton(logger)
.register(  
"repository",  
(dr) -> new Repository())
.register(  
"service1",  
(dr) -> new Service1(dr.resolve("repository", Repository.class)))
.loadContent(true, true);
´´´











## Uso avanzado:
#### Las dependencias se pueden registrar como 'Singleton' (por defecto) o 'Prototype':
- Singleton: El objeto se crea una vez y siempre que se recupere su valor se devolverá la misma instancia (el mismo objeto).
- Prototype: Cada vez que se quiera recuperar un objeto, se crea una nueva instancia (nuevo objeto) del mismo.
-
*Sino se indica nada, por defecto todos los objetos se registran como dependencias de tipo *Singleton*.*

Registrar un objeto como *Singleton*:
```
//Por defecto sino se indica nada, se registra el objeto como 'Singleton'.
IocContainerFactory.singleton(logger)
	.register(  
        "repository",  
		(dr) -> new Repository());

//Indicando de forma explícita que debe registrarse como 'Singleton'.
IocContainerFactory.singleton(logger)
	.register(  
        "repository",
        DependencyFactory.DependencyType.SINGLETON,
		(dr) -> new Repository());
```
Registrar un objeto como *Prototype*:
```
//Siempre hay que indicar forma explícita que debe registrarse como 'Prototype'.
IocContainerFactory.singleton(logger)
	.register(  
        "repository",
        DependencyFactory.DependencyType.PROTOTYPE,
		(dr) -> new Repository());
```


///TODO... No importa el orden de registro de las dependencias. Se pueden registrar unas dependencias,
resolver y volver a registrar. Se pueden sobreescribir dependencias (útil para test). Poner un ejemplo corto al inicio del README.md de la creación, registro y resolución de dependencias del contenedor.

#### Sobreescribir el registro de una dependencia:
Si registramos 2 o más veces el mismo objeto (utilizamos el mismo identificador de tipo string) como en el ejemplo anterior que hemos registrado 3 veces una dependencia con el mismo nombre *"repository"*, no se produce ningún error pero el contenedor muestra un mensaje de log como aviso (en caso de que hayamos utilizado un logger al crear el contenedor).
La dependencia con el nombre "repository" quedará registrada con el último registro realizado sobreescibiendo los registros anteriores.

*Para que el nuevo registro se aplique correctamente a la hora de resolver la dependencia, no se debe haber intentado resolver (recuperar) ninguna dependencia del contenedor ni haber realizado la carga automática después del registro.
Ya que una vez que se crea el objeto de dicha dependencia, se guarda dentro del contenedor y éste a su vez puede
ser una dependencia de otro objeto. Y aún cuando la dependencia puede estar declarada como Prototype y no se guarde dentro del contenedor, siempre puede haber sido inyectada en otra dependencia de tipo Singleton que siempre guardará una versión del objeto que no corresponderá con el nuevo registro.*

Esto es realmente útil para realizar tests. Podemos utilizar el mismo registro de dependencias de nuestro proyecto y en los tests sobreescribir solo aquellas dependencias que necesitamos mockear o sustituir su comportamiento para que no funcione igual que en producción.










Para ver más ejemplos sobre el uso de *PisthorIoc* puede consultar los tests de la librería en el proyecto (PisthorIoc_Test)[https://github.com/SilverioMG/PisthorIoc_Test].