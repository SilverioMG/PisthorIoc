package pisthorioc.test.dependencyfactory;

import net.atopecode.pisthorioc.dependencyfactory.DependencyFactory;
import net.atopecode.pisthorioc.exceptions.IocDependencyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyFactoryUnitTest {

    @Test
    @DisplayName("Se crea un objeto 'DepdencyFactory' con parámetros 'null' o 'vacíos'.")
    public void createWithParamsNullOrEmpty(){
        assertThrows(IocDependencyException.class, () -> {
           DependencyFactory df = new DependencyFactory<Object>(null, (dr) -> new Object());
        });

        assertThrows(IocDependencyException.class, () -> {
            DependencyFactory df = new DependencyFactory<Object>("name", null);
        });

        assertThrows(IocDependencyException.class, () -> {
            DependencyFactory<Object> df = new DependencyFactory("", (dr) -> new Object());
        });

        assertThrows(IocDependencyException.class, () -> {
            DependencyFactory<Object> df = new DependencyFactory("     ", (dr) -> new Object());
        });

        assertThrows(IocDependencyException.class, () -> {
            DependencyFactory<Object> df = new DependencyFactory(null, null);
        });

        assertThrows(IocDependencyException.class, () -> {
            DependencyFactory<Object> df = new DependencyFactory("", null);
        });

        assertThrows(IocDependencyException.class, () -> {
            DependencyFactory<Object> df = new DependencyFactory("     ", null);
        });
    }

    @Test
    @DisplayName("Se crea un objeto 'DependencyFactory' sin enviar parámetro 'DependencyType' y se comprueba que el valor" +
            "por defecto es 'SINGLETON'.")
    public void createWithoutDependencyTypeParameterAndCheckThatIsSingleton(){
        DependencyFactory<Object> df = new DependencyFactory<>("name", (dr) -> new Object());
        DependencyFactory.DependencyType type = df.getType();

        assertEquals(type, DependencyFactory.DependencyType.SINGLETON);
    }

    @Test
    @DisplayName("Se comprueba que los métodos que verifican el tipo de dependencia (Singleton o Prototype) funcionan correctamente.")
    public void checkTypeDependencieMethods(){
        DependencyFactory<Object> df = new DependencyFactory<>(
                "name",
                DependencyFactory.DependencyType.SINGLETON,
                (dr) -> new Object());
        assertTrue(df.isTypeSingleton());
        assertFalse(df.isTypePrototype());

        df = new DependencyFactory<>(
                "name",
                DependencyFactory.DependencyType.PROTOTYPE,
                (dr) -> new Object());
        assertTrue(df.isTypePrototype());
        assertFalse(df.isTypeSingleton());
    }
}
