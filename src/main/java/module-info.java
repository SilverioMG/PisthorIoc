module net.atopecode.pisthorioc.module {
    requires transitive org.slf4j; //El módulo que 'require' a 'pisthorioc.module' ya no necesita hacer 'require org.slf4j'.
    requires org.apache.commons.lang3;

    exports net.atopecode.pisthorioc.ioccontainer;
    exports net.atopecode.pisthorioc.dependencyfactory;
    exports net.atopecode.pisthorioc.dependencyresolver;
    exports net.atopecode.pisthorioc.exceptions;
}