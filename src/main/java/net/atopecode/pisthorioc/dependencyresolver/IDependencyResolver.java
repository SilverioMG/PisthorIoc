package net.atopecode.pisthorioc.dependencyresolver;

public interface IDependencyResolver {

    public <T> T resolve(String name, Class<? extends T> classResult);
}
