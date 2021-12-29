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

package org.apache.shardingsphere.data.pipeline.core.check.consistency;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.SingleTableDataCalculator;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.exception.ServiceLoaderInstantiationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Single table data calculator registry.
 */
@Slf4j
public final class SingleTableDataCalculatorRegistry {
    
    private static final Map<String, Map<String, SingleTableDataCalculator>> ALGORITHM_DATABASE_CALCULATOR_MAP = new HashMap<>();
    
    static {
        ShardingSphereServiceLoader.register(SingleTableDataCalculator.class);
        for (SingleTableDataCalculator each : ShardingSphereServiceLoader.getSingletonServiceInstances(SingleTableDataCalculator.class)) {
            Map<String, SingleTableDataCalculator> dataCalculatorMap = ALGORITHM_DATABASE_CALCULATOR_MAP.computeIfAbsent(each.getAlgorithmType(), algorithmType -> new HashMap<>());
            for (String databaseType : each.getDatabaseTypes()) {
                SingleTableDataCalculator replaced = dataCalculatorMap.put(databaseType, each);
                if (null != replaced) {
                    log.warn("element replaced, algorithmType={}, databaseTypes={}, current={}, replaced={}",
                            each.getAlgorithmType(), each.getDatabaseTypes(), each.getClass().getName(), replaced.getClass().getName());
                }
            }
        }
    }
    
    /**
     * New service instance.
     *
     * @param algorithmType algorithm type
     * @param databaseType database type
     * @return single table data calculator
     * @throws NullPointerException if calculator not found
     * @throws ServiceLoaderInstantiationException if new instance by reflection failed
     */
    public static SingleTableDataCalculator newServiceInstance(final String algorithmType, final String databaseType) {
        Map<String, SingleTableDataCalculator> calculatorMap = ALGORITHM_DATABASE_CALCULATOR_MAP.get(algorithmType);
        Preconditions.checkNotNull(calculatorMap, String.format("calculator not found for algorithmType '%s'", algorithmType));
        SingleTableDataCalculator calculator = calculatorMap.get(databaseType);
        Preconditions.checkNotNull(calculator, String.format("calculator not found for algorithmType '%s' databaseType '%s'", algorithmType, databaseType));
        try {
            return calculator.getClass().newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new ServiceLoaderInstantiationException(calculator.getClass(), ex);
        }
    }
}
