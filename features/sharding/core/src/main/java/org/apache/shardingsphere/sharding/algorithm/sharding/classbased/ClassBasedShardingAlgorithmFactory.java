/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.algorithm.sharding.classbased;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.sharding.exception.algorithm.ShardingAlgorithmClassImplementationException;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.Properties;

/**
 * ShardingSphere class based algorithm factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassBasedShardingAlgorithmFactory {
    
    /**
     * Create sharding algorithm.
     *
     * @param shardingAlgorithmClassName sharding algorithm class name
     * @param superShardingAlgorithmClass sharding algorithm super class
     * @param props properties
     * @param <T> class generic type
     * @return sharding algorithm instance
     * @throws ShardingAlgorithmClassImplementationException sharding algorithm class implementation exception
     */
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public static <T extends ShardingAlgorithm> T newInstance(final String shardingAlgorithmClassName, final Class<T> superShardingAlgorithmClass, final Properties props) {
        Class<?> algorithmClass = loadClass(shardingAlgorithmClassName);
        if (!superShardingAlgorithmClass.isAssignableFrom(algorithmClass)) {
            throw new ShardingAlgorithmClassImplementationException(shardingAlgorithmClassName, superShardingAlgorithmClass);
        }
        T result = (T) algorithmClass.getDeclaredConstructor().newInstance();
        result.init(convertToStringTypedProperties(props));
        return result;
    }
    
    private static Properties convertToStringTypedProperties(final Properties props) {
        Properties result = new Properties();
        props.forEach((key, value) -> result.setProperty(key.toString(), null == value ? null : value.toString()));
        return result;
    }
    
    private static Class<?> loadClass(final String className) throws ClassNotFoundException {
        ClassLoader[] classLoaders = new ClassLoader[]{
                Thread.currentThread().getContextClassLoader(),
                ClassBasedShardingAlgorithmFactory.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };
        for (ClassLoader each : classLoaders) {
            if (null != each) {
                try {
                    return Class.forName(className, true, each);
                } catch (final ClassNotFoundException ex) {
                    // Try next classloader
                }
            }
        }
        throw new ClassNotFoundException("Could not load class: " + className);
    }
}
