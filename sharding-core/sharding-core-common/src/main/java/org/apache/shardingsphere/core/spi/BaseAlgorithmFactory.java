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

package org.apache.shardingsphere.core.spi;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingConfigurationException;
import org.apache.shardingsphere.spi.BaseAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * Base algorithm factory.
 * 
 * @author panjuan
 * @author zhangliang
 * 
 * @param <T> type of algorithm class
 */
@RequiredArgsConstructor
public abstract class BaseAlgorithmFactory<T extends BaseAlgorithm> {
    
    private final Class<T> classType;
    
    /**
     * Create algorithm instance.
     * 
     * @param type algorithm type
     * @param props algorithm properties
     * @return algorithm instance
     */
    public final T newAlgorithm(final String type, final Properties props) {
        Collection<T> algorithms = loadAlgorithms(type);
        if (algorithms.isEmpty()) {
            throw new ShardingConfigurationException("Invalid `%s` algorithm type `%s`.", classType.getName(), type);
        }
        T result = algorithms.iterator().next();
        result.setProperties(props);
        return result;
    }
    
    /**
     * Create algorithm instance by default algorithm type.
     *
     * @return algorithm instance
     */
    public final T newAlgorithm() {
        T result = loadFirstAlgorithm();
        result.setProperties(new Properties());
        return result;
    }
    
    private Collection<T> loadAlgorithms(final String type) {
        return Collections2.filter(NewInstanceServiceLoader.newServiceInstances(classType), new Predicate<T>() {
            
            @Override
            public boolean apply(final T input) {
                return type.equalsIgnoreCase(input.getType());
            }
        });
    }
    
    private T loadFirstAlgorithm() {
        Collection<T> algorithms = NewInstanceServiceLoader.newServiceInstances(classType);
        if (algorithms.isEmpty()) {
            throw new ShardingConfigurationException("Invalid `%s` algorithm, no implementation class load from SPI.", classType.getName());
        }
        return algorithms.iterator().next();
    }
}
