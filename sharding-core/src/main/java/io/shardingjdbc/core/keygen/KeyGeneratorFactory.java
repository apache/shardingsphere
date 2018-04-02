package io.shardingjdbc.core.keygen;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ServiceLoader;


/**
 * Key generator factory.
 *
 * @author nianjun.sun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyGeneratorFactory {
    /**
     * Create key generator.
     *
     * @param keyGeneratorClassName key generator class name
     * @return key generator instance
     */
    public static KeyGenerator newInstance(final String keyGeneratorClassName) {
        KeyGenerator keyGenerator = null;
        ServiceLoader<KeyGenerator> loaders = ServiceLoader.load(KeyGenerator.class);
        for (KeyGenerator generator : loaders){
            if(null != generator){
                if(generator.getClass().getName().equalsIgnoreCase(keyGeneratorClassName)){
                    keyGenerator = generator;
                }
            }
        }

        if(null == keyGenerator){
            throw new IllegalArgumentException(String.format("Class %s should have public privilege and no argument constructor", keyGeneratorClassName));
        }

        return keyGenerator;
    }
}
