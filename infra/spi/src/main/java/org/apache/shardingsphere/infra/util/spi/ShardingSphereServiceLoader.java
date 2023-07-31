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

package org.apache.shardingsphere.infra.util.spi;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ShardingSphere service loader.
 * 
 * @param <T> type of service
 */
public final class ShardingSphereServiceLoader<T> {
    
    private static final Map<Class<?>, ShardingSphereServiceLoader<?>> LOADERS = new ConcurrentHashMap<>();
    
    private static final int LOAD_LOCKS_COUNT = 16;
    
    private static final Object[] LOAD_LOCKS = new Object[LOAD_LOCKS_COUNT];
    
    private final Class<T> serviceInterface;
    
    @Getter
    private final Collection<T> services;
    
    static {
        for (int i = 0; i < LOAD_LOCKS_COUNT; i++) {
            LOAD_LOCKS[i] = new Object();
        }
    }
    
    private ShardingSphereServiceLoader(final Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
        validate();
        services = load();
    }
    
    private void validate() {
        Preconditions.checkNotNull(serviceInterface, "SPI interface is null.");
        Preconditions.checkArgument(serviceInterface.isInterface(), "SPI interface `%s` is not interface.", serviceInterface);
    }
    
    private Collection<T> load() {
        Collection<T> result = new LinkedList<>();
        for (T each : ServiceLoader.load(serviceInterface)) {
            result.add(each);
        }
        return result;
    }
    
    /**
     * Get service instances.
     *
     * @param serviceInterface service interface
     * @param <T> type of service interface
     * @return service instances
     * @see <a href="https://bugs.openjdk.java.net/browse/JDK-8161372">JDK-8161372</a>
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> getServiceInstances(final Class<T> serviceInterface) {
        ShardingSphereServiceLoader<?> result = LOADERS.get(serviceInterface);
        if (null != result) {
            return (Collection<T>) result.getServiceInstances();
        }
        synchronized (LOAD_LOCKS[serviceInterface.hashCode() % LOAD_LOCKS_COUNT]) {
            result = LOADERS.get(serviceInterface);
            if (null == result) {
                result = new ShardingSphereServiceLoader<>(serviceInterface);
                LOADERS.put(serviceInterface, result);
            }
        }
        return (Collection<T>) result.getServiceInstances();
    }
    
    private Collection<T> getServiceInstances() {
        return null == serviceInterface.getAnnotation(SingletonSPI.class) ? createNewServiceInstances() : getSingletonServiceInstances();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Collection<T> createNewServiceInstances() {
        Collection<T> result = new LinkedList<>();
        for (Object each : services) {
            result.add((T) each.getClass().getDeclaredConstructor().newInstance());
        }
        return result;
    }
    
    private Collection<T> getSingletonServiceInstances() {
        return services;
    }
}
