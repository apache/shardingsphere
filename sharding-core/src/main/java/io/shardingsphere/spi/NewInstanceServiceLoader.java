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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.LinkedList;
import java.util.ServiceLoader;

/**
 * SPI service loader for new instance for every call.
 *
 * @author zhangliang
 * @param <T> type of class
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NewInstanceServiceLoader<T> {
    
    private final Collection<Class<T>> serviceClasses = new LinkedList<>();
    
    /**
     * Creates a new service class loader for the given service type.
     * 
     * @param service service type
     * @param <T> type of service
     * @return new service class loader
     */
    @SuppressWarnings("unchecked")
    public static <T> NewInstanceServiceLoader<T> load(final Class<T> service) {
        NewInstanceServiceLoader result = new NewInstanceServiceLoader();
        for (T each : ServiceLoader.load(service)) {
            result.serviceClasses.add(each.getClass());
        }
        return result;
    }
    
    /**
     * New service instances.
     * 
     * @return service instances
     */
    @SneakyThrows
    public Collection<T> newServiceInstances() {
        Collection<T> result = new LinkedList<>();
        for (Class<T> each : serviceClasses) {
            result.add(each.newInstance());
        }
        return result;
    }
}
