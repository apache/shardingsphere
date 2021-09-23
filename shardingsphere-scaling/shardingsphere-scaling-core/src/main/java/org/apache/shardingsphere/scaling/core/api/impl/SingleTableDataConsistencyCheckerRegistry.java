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

package org.apache.shardingsphere.scaling.core.api.impl;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.api.SingleTableDataConsistencyChecker;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.exception.ServiceLoaderInstantiationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Single table data consistency checker registry.
 */
@Slf4j
public final class SingleTableDataConsistencyCheckerRegistry {
    
    private static final Map<String, Map<String, SingleTableDataConsistencyChecker>> ALGORITHM_DATABASE_CHECKER_MAP = new HashMap<>();
    
    static {
        ShardingSphereServiceLoader.register(SingleTableDataConsistencyChecker.class);
        for (SingleTableDataConsistencyChecker each : ShardingSphereServiceLoader.getSingletonServiceInstances(SingleTableDataConsistencyChecker.class)) {
            SingleTableDataConsistencyChecker replaced = ALGORITHM_DATABASE_CHECKER_MAP.computeIfAbsent(each.getAlgorithmType(), algorithmType -> new HashMap<>())
                    .put(each.getDatabaseType(), each);
            if (null != replaced) {
                log.info("checker replaced, algorithmType={}, databaseType={}, current={}, replaced={}",
                        each.getAlgorithmType(), each.getDatabaseType(), each.getClass().getName(), replaced.getClass().getName());
            }
        }
    }
    
    /**
     * New service instance.
     *
     * @param algorithmType algorithm type
     * @param databaseType database type
     * @return single table data consistency checker
     * @throws NullPointerException if checker not found
     * @throws ServiceLoaderInstantiationException if new instance by reflection failed
     */
    public static SingleTableDataConsistencyChecker newServiceInstance(final String algorithmType, final String databaseType) {
        Map<String, SingleTableDataConsistencyChecker> checkerMap = ALGORITHM_DATABASE_CHECKER_MAP.get(algorithmType);
        Preconditions.checkNotNull(checkerMap, String.format("checker not found for algorithmType '%s'", algorithmType));
        SingleTableDataConsistencyChecker checker = checkerMap.get(databaseType);
        Preconditions.checkNotNull(checker, String.format("checker not found for algorithmType '%s' databaseType '%s'", algorithmType, databaseType));
        try {
            return checker.getClass().newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new ServiceLoaderInstantiationException(checker.getClass(), ex);
        }
    }
}
