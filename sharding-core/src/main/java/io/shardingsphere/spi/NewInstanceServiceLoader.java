/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.spi;

import io.shardingsphere.core.exception.ShardingException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * SPI service loader for new instance for every call.
 *
 * @author zhangliang
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewInstanceServiceLoader {
    
    private static final Map<Class, Collection<Class<?>>> SERVICE_MAP = new HashMap<>();
    
    /**
     * Create service using service loader.
     * 
     * @param service service type
     * @param <T> type of service
     * @return SPI service collection
     */
    public static <T> Collection<T> load(final Class<T> service) {
        Collection<T> result = new LinkedList<>();
        for (T each : ServiceLoader.load(service)) {
            result.add(each);
        }
        return result;
    }
    
    /**
     * Register SPI service into map for new instance.
     *
     * @param service service type
     * @param <T> type of service
     */
    public static <T> void register(final Class<T> service) {
        for (T each : ServiceLoader.load(service)) {
            registerServiceClass(service, each);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> void registerServiceClass(final Class<T> service, final T instance) {
        Collection<Class<?>> serviceClasses = SERVICE_MAP.get(service);
        if (null == serviceClasses) {
            serviceClasses = new LinkedList<>();
        }
        serviceClasses.add(instance.getClass());
        SERVICE_MAP.put(service, serviceClasses);
    }
    
    /**
     * New service instances.
     *
     * @param service service class
     * @param <T> type of service
     * @return service instances
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> newServiceInstances(final Class<T> service) {
        Collection<T> result = new LinkedList<>();
        if (null == SERVICE_MAP.get(service)) {
            return result;
        }
        for (Class<?> each : SERVICE_MAP.get(service)) {
            try {
                result.add((T) each.newInstance());
            } catch (final ReflectiveOperationException ex) {
                throw new ShardingException(ex);
            }
        }
        return result;
    }
}
