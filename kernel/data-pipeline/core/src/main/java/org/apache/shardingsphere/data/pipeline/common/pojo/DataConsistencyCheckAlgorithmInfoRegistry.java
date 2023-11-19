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

package org.apache.shardingsphere.data.pipeline.common.pojo;

import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.table.TableDataConsistencyChecker;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.annotation.SPIDescription;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Data consistency check algorithm info registry.
 */
@NoArgsConstructor
public final class DataConsistencyCheckAlgorithmInfoRegistry {
    
    private static final Collection<DataConsistencyCheckAlgorithmInfo> ALGORITHM_INFOS = loadAllAlgorithms();
    
    private static Collection<DataConsistencyCheckAlgorithmInfo> loadAllAlgorithms() {
        Collection<DataConsistencyCheckAlgorithmInfo> result = new LinkedList<>();
        for (TableDataConsistencyChecker each : ShardingSphereServiceLoader.getServiceInstances(TableDataConsistencyChecker.class)) {
            SPIDescription description = each.getClass().getAnnotation(SPIDescription.class);
            String typeAliases = each.getTypeAliases().stream().map(Object::toString).collect(Collectors.joining(","));
            result.add(
                    new DataConsistencyCheckAlgorithmInfo(each.getType(), typeAliases, getSupportedDatabaseTypes(each.getSupportedDatabaseTypes()), null == description ? "" : description.value()));
        }
        return result;
    }
    
    private static Collection<DatabaseType> getSupportedDatabaseTypes(final Collection<DatabaseType> supportedDatabaseTypes) {
        return supportedDatabaseTypes.isEmpty() ? ShardingSphereServiceLoader.getServiceInstances(DatabaseType.class) : supportedDatabaseTypes;
    }
    
    /**
     * Get all data consistency check algorithm infos.
     * 
     * @return all data consistency check algorithm infos
     */
    public static Collection<DataConsistencyCheckAlgorithmInfo> getAllAlgorithmInfos() {
        return ALGORITHM_INFOS;
    }
}
