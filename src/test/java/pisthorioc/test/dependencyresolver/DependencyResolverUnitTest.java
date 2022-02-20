package pisthorioc.test.dependencyresolver;

import net.atopecode.pisthorioc.dependencyresolver.DependencyResolver;
import net.atopecode.pisthorioc.exceptions.IocDependencyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pisthorioc.test.dependencies.Repository1;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class DependencyResolverUnitTest {

    @Test
    @DisplayName("Se crea un objeto 'dependencyResolver' con parámetros a 'null'")
    public void createWithParamsNull(){
        assertThrows(NullPointerException.class, () -> {
            DependencyResolver dr = new DependencyResolver(null, new HashMap<>());
        });

        assertThrows(NullPointerException.class, () -> {
            DependencyResolver dr = new DependencyResolver(new HashMap<>(), null);
        });

        assertThrows(NullPointerException.class, () -> {
            DependencyResolver dr = new DependencyResolver(null, null);
        });
    }

    @Test
    @DisplayName("Se intenta resolver una dependencia enviando los parámetros 'name' y/o 'classObject' a 'null'.")
    public void resolveWithParametersNull(){
        DependencyResolver dr = new DependencyResolver(new HashMap<>(), new HashMap<>());
        assertThrows(IocDependencyException.class, () -> {
            dr.resolve(null, Repository1.class);
        });

        assertThrows(IocDependencyException.class, () -> {
            dr.resolve("name", null);
        });

        assertThrows(IocDependencyException.class, () -> {
            dr.resolve(null, null);
        });
    }
}
