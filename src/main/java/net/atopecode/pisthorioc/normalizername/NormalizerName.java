package net.atopecode.pisthorioc.normalizername;

import net.atopecode.pisthorioc.exceptions.IocDependencyException;
import org.apache.commons.lang3.StringUtils;

/**
 * Esta clase se utiliza para normalizar el nombre con el que se registra un objeto como dependencia dentro del contenedor.
 * También se utiliza a la hora de resolver la dependencia ya que debe coincidir con el mismo nombre con el que se registró.
 */
public class NormalizerName {

    public static String normalize(String name){
        if(StringUtils.isBlank(name)){
            throw new IocDependencyException("Parameter 'name' must be not null.");
        }

        return name.toLowerCase().trim();
    }
}
