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

package org.apache.shardingsphere.infra.spi.type.ordered;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ordered SPI loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderedSPILoader {
    
    private static final ClassValue<OrderedSPIRegistry<?>> REGISTRY = new ClassValue<OrderedSPIRegistry<?>>() {
        
        @Override
        protected OrderedSPIRegistry<?> computeValue(final Class<?> clazz) {
            return null != clazz.getAnnotation(SingletonSPI.class) ? new CachedSingletonOrderedSPIRegistry<>(clazz) : new NoCachePrototypeOrderedSPIRegistry<>(clazz);
        }
    };
    
    /**
     * Get services by class type.
     *
     * @param serviceInterface ordered SPI service interface
     * @param types types
     * @param <T> type of ordered SPI class
     * @return got services
     */
    @SuppressWarnings("unchecked")
    public static <T extends OrderedSPI<?>> Map<Class<?>, T> getServicesByClass(final Class<T> serviceInterface, final Collection<Class<?>> types) {
        return ((OrderedSPIRegistry<T>) REGISTRY.get(serviceInterface)).getServicesByClass(types);
    }
    
    /**
     * Get services.
     *
     * @param serviceInterface ordered SPI service interface
     * @param types types
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     * @return got services
     */
    @SuppressWarnings("unchecked")
    public static <K, V extends OrderedSPI<?>> Map<K, V> getServices(final Class<V> serviceInterface, final Collection<K> types) {
        return ((OrderedSPIRegistry<V>) REGISTRY.get(serviceInterface)).getServices(types);
    }
    
    /**
     * Get services.
     *
     * @param serviceInterface ordered SPI service interface
     * @param types types
     * @param <K> type of key
     * @param <V> type of ordered SPI class
     * @param orderComparator order comparator
     * @return got services
     */
    @SuppressWarnings("unchecked")
    public static <K, V extends OrderedSPI<?>> Map<K, V> getServices(final Class<V> serviceInterface, final Collection<K> types, final Comparator<Integer> orderComparator) {
        List<Entry<K, V>> orderServices = new ArrayList<>(((OrderedSPIRegistry<V>) REGISTRY.get(serviceInterface)).getServices(types).entrySet());
        orderServices.sort((e1, e2) -> orderComparator.compare(e1.getValue().getOrder(), e2.getValue().getOrder()));
        Map<K, V> result = new LinkedHashMap<>(orderServices.size(), 1F);
        for (Entry<K, V> entry : orderServices) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
    /**
     * Get services.
     *
     * @param serviceInterface ordered SPI service interface
     * @param <T> type of ordered SPI class
     * @return got services
     */
    @SuppressWarnings("unchecked")
    public static <T extends OrderedSPI<?>> Collection<T> getServices(final Class<T> serviceInterface) {
        return ((OrderedSPIRegistry<T>) REGISTRY.get(serviceInterface)).getOrderedServices();
    }
    
    private interface OrderedSPIRegistry<T extends OrderedSPI<?>> {
        
        Collection<T> getOrderedServices();
        
        Map<Class<?>, T> getServicesByClass(Collection<Class<?>> types);
        
        <K> Map<K, T> getServices(Collection<K> types);
    }
    
    private static final class CachedSingletonOrderedSPIRegistry<T extends OrderedSPI<?>> implements OrderedSPIRegistry<T> {
        
        private final Collection<T> orderedServices;
        
        private final Map<Class<?>, T> singleTypeClassToService;
        
        private final Map<Set<Class<?>>, Map<Class<?>, T>> multiTypeClassToServices = new ConcurrentHashMap<>();
        
        private final Map<Collection<?>, Map<?, T>> multiObjectToServices = new ConcurrentHashMap<>();
        
        @SuppressWarnings({"rawtypes", "unchecked"})
        CachedSingletonOrderedSPIRegistry(final Class<?> serviceInterface) {
            Map<Integer, T> orderServices = new TreeMap<>(Comparator.naturalOrder());
            for (Object each : ShardingSphereServiceLoader.getServiceInstances((Class) serviceInterface)) {
                Preconditions.checkArgument(!orderServices.containsKey(((T) each).getOrder()),
                        "Found same order `%s` with `%s` and `%s`", ((T) each).getOrder(), orderServices.get(((T) each).getOrder()), each);
                orderServices.put(((T) each).getOrder(), (T) each);
            }
            orderedServices = orderServices.values();
            singleTypeClassToService = new HashMap<>(orderedServices.size(), 1F);
            for (T each : orderedServices) {
                singleTypeClassToService.put(each.getTypeClass(), each);
            }
        }
        
        @Override
        public Collection<T> getOrderedServices() {
            return orderedServices;
        }
        
        @Override
        public Map<Class<?>, T> getServicesByClass(final Collection<Class<?>> types) {
            if (1 == types.size()) {
                Class<?> type = types.iterator().next();
                T service = singleTypeClassToService.get(type);
                return null == service ? Collections.emptyMap() : Collections.singletonMap(type, service);
            }
            Set<Class<?>> typeClasses = types instanceof Set ? (Set<Class<?>>) types : new HashSet<>(types);
            Map<Class<?>, T> result = multiTypeClassToServices.get(typeClasses);
            if (null == result) {
                result = multiTypeClassToServices.computeIfAbsent(typeClasses, this::computeServicesByClass);
            }
            return result;
        }
        
        private Map<Class<?>, T> computeServicesByClass(final Set<Class<?>> types) {
            Map<Class<?>, T> result = new LinkedHashMap<>(types.size(), 1F);
            for (T each : orderedServices) {
                if (types.contains(each.getTypeClass())) {
                    result.put(each.getTypeClass(), each);
                }
            }
            return result;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public <K> Map<K, T> getServices(final Collection<K> types) {
            if (1 == types.size()) {
                K type = types.iterator().next();
                T service = singleTypeClassToService.get(type.getClass());
                return null == service ? Collections.emptyMap() : Collections.singletonMap(type, service);
            }
            Map<?, T> result = multiObjectToServices.get(types);
            if (null == result) {
                result = multiObjectToServices.computeIfAbsent(types, t -> computeServicesByObject((Collection<Object>) t));
            }
            return (Map<K, T>) result;
        }
        
        private Map<Object, T> computeServicesByObject(final Collection<Object> types) {
            Map<Class<?>, List<Object>> classTypeMap = new HashMap<>(types.size(), 1F);
            Set<Class<?>> typeClasses = new HashSet<>(types.size(), 1F);
            for (Object each : types) {
                classTypeMap.computeIfAbsent(each.getClass(), clazz -> new LinkedList<>()).add(each);
                typeClasses.add(each.getClass());
            }
            Map<Object, T> result = new LinkedHashMap<>(types.size(), 1F);
            for (T each : orderedServices) {
                if (typeClasses.contains(each.getTypeClass())) {
                    for (Object type : classTypeMap.get(each.getTypeClass())) {
                        result.put(type, each);
                    }
                }
            }
            return result;
        }
    }
    
    private static final class NoCachePrototypeOrderedSPIRegistry<T extends OrderedSPI<?>> implements OrderedSPIRegistry<T> {
        
        private final Class<T> serviceInterface;
        
        @SuppressWarnings("unchecked")
        NoCachePrototypeOrderedSPIRegistry(final Class<?> serviceInterface) {
            this.serviceInterface = (Class<T>) serviceInterface;
        }
        
        @Override
        public Collection<T> getOrderedServices() {
            return loadOrderedServices();
        }
        
        @Override
        public Map<Class<?>, T> getServicesByClass(final Collection<Class<?>> types) {
            Set<Class<?>> typeClasses = types instanceof Set ? (Set<Class<?>>) types : new HashSet<>(types);
            Map<Class<?>, T> result = new LinkedHashMap<>(types.size(), 1F);
            for (T each : loadOrderedServices()) {
                if (typeClasses.contains(each.getTypeClass())) {
                    result.put(each.getTypeClass(), each);
                }
            }
            return result;
        }
        
        @Override
        public <K> Map<K, T> getServices(final Collection<K> types) {
            Map<Class<?>, List<K>> classTypeMap = new HashMap<>(types.size(), 1F);
            Set<Class<?>> typeClasses = new HashSet<>(types.size(), 1F);
            for (K each : types) {
                classTypeMap.computeIfAbsent(each.getClass(), clazz -> new LinkedList<>()).add(each);
                typeClasses.add(each.getClass());
            }
            Map<K, T> result = new LinkedHashMap<>(types.size(), 1F);
            for (T each : loadOrderedServices()) {
                if (typeClasses.contains(each.getTypeClass())) {
                    for (K type : classTypeMap.get(each.getTypeClass())) {
                        result.put(type, each);
                    }
                }
            }
            return result;
        }
        
        private Collection<T> loadOrderedServices() {
            Map<Integer, T> result = new TreeMap<>(Comparator.naturalOrder());
            for (T each : ShardingSphereServiceLoader.getServiceInstances(serviceInterface)) {
                Preconditions.checkArgument(!result.containsKey(each.getOrder()),
                        "Found same order `%s` with `%s` and `%s`", each.getOrder(), result.get(each.getOrder()), each);
                result.put(each.getOrder(), each);
            }
            return result.values();
        }
    }
}
