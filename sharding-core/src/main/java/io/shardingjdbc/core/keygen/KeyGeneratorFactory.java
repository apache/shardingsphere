/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

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
            throw new IllegalArgumentException(String.format("there is no Class %s defined in corresponding SPI file", keyGeneratorClassName));
        }

        return keyGenerator;
    }
}
