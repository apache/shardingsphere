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
 */
public final class ShardingSphereServiceLoader<T> {
    
    private static final Map<Class<?>, ShardingSphereServiceLoader<?>> LOADERS = new ConcurrentHashMap<>();
    
    private final Class<T> serviceInterface;
    
    @Getter
    private final Collection<T> services;
    
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
        return (Collection<T>) (null != result ? result.getServiceInstances() : LOADERS.computeIfAbsent(serviceInterface, ShardingSphereServiceLoader::new).getServiceInstances());
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
