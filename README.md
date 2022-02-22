# PisthorIoc - A simple dependency injection container

PisthorIoc es un Contenedor para Inyección de Dependencias para proyectos Java.

- Es simple de utilizar y ligero.
- No utiliza reflexión. No hay magia, solo código.
- ThreadSafe.
- Inyección de dependencias vía constructor.
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
3. Carga automática del contenedor con todas las dependencias registradas para (opcional pero recomendado).
4. Uso del contenedor para resolver dependencias.

### 1. Crear contenedor de dependencias (utilizando logger o no)
>#### Crear un contenedor como *Singleton* (el mismo objeto en memoria durante toda la ejecución del programa):
>```
>Logger logger = LoggerFactory.getLogger(IocContainer.class);
>IocContainer container = IocContainerFactory.singleton(logger);
>```
>El método static *'IocContainerFactory.singleton(logger)'* siempre nos devolverá la misma instancia (mismo objeto) >de nuestro contenedor de dependencias.


>#### Crear un contenedor nuevo si es necesario (un nuevo objeto en memoria):
>``` 
>Logger logger = LoggerFactory.getLogger(IocContainer.class);
>IocContainer container = IocContainerFactory.newInstance(logger);
>```
>El método static *'IocContainerFactory.newInstance(logger)'* siempre nos devolverá una nueva instancia (distinto >objeto) de un contenedor de dependencias.

>#### Sin usar Logger:
>```
>IocContainer container1 = IocContainerFactory.singleton(null);
>IocContainer container2 = IocContainerFactory.newInstance(null);
>```
>Al no utilizar *logger* en la construcción del contenedor, su funcionamiento será el mismo pero no se mostrarán >mensajes de advertencia o de errores (pero se siguen lanzando las correspondientes Exceptions).


### 2. Registrar todas las dependencias necesarias.
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







Para ver más ejemplos sobre el uso de *PisthorIoc* puede consultar los tests de la librería en el proyecto (PisthorIoc_Test)[https://github.com/SilverioMG/PisthorIoc_Test].