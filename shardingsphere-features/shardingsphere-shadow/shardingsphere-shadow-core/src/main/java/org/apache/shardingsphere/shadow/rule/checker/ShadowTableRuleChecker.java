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

package org.apache.shardingsphere.shadow.rule.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.column.ShadowOperationType;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shadow table rule checker.
 */
public final class ShadowTableRuleChecker {
    
    /**
     * Check data sources mappings size.
     *
     * @param dataSources data sources mappings
     */
    public static void checkDataSources(final Map<String, ShadowDataSourceConfiguration> dataSources) {
        Preconditions.checkState(!dataSources.isEmpty(), "No available shadow data sources mappings in shadow configuration.");
    }
    
    /**
     * Check shadow tables size.
     *
     * @param shadowTables shadow tables
     */
    public static void checkShadowTables(final Map<String, ShadowTableConfiguration> shadowTables) {
        Preconditions.checkState(!shadowTables.isEmpty(), "No available shadow tables in shadow configuration.");
    }
    
    /**
     * Check shadow algorithms size.
     *
     * @param shadowAlgorithms shadow algorithms
     */
    public static void checkShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        Preconditions.checkState(!shadowAlgorithms.isEmpty(), "No available shadow data algorithms in shadow configuration.");
    }
    
    /**
     * Check table shadow algorithms.
     *
     * @param tableName table name
     * @param tableShadowAlgorithmNames table shadow algorithm names
     * @param shadowAlgorithms shadow algorithms
     */
    public static void checkTableShadowAlgorithms(final String tableName, final Collection<String> tableShadowAlgorithmNames, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        Preconditions.checkState(!tableShadowAlgorithmNames.isEmpty(), "No available shadow Algorithm configuration in shadow table `%s`.", tableName);
        checkTableColumnShadowAlgorithms(tableName, createTableShadowAlgorithms(tableShadowAlgorithmNames, shadowAlgorithms));
    }
    
    private static void checkTableColumnShadowAlgorithms(final String tableName, final Collection<ShadowAlgorithm> tableShadowAlgorithms) {
        int insertOperationCount = 0;
        int updateOperationCount = 0;
        int deleteOperationCount = 0;
        int selectOperationCount = 0;
        for (ShadowAlgorithm each : tableShadowAlgorithms) {
            if (each instanceof ColumnShadowAlgorithm) {
                Optional<ShadowOperationType> shadowOperationTypeOptional = ShadowOperationType.contains(each.getProps().get("operation").toString());
                if (shadowOperationTypeOptional.isPresent()) {
                    ShadowOperationType shadowOperationType = shadowOperationTypeOptional.get();
                    switch (shadowOperationType) {
                        case INSERT:
                            insertOperationCount++;
                            checkOperationCount(tableName, shadowOperationType, insertOperationCount);
                            break;
                        case UPDATE:
                            updateOperationCount++;
                            checkOperationCount(tableName, shadowOperationType, updateOperationCount);
                            break;
                        case DELETE:
                            deleteOperationCount++;
                            checkOperationCount(tableName, shadowOperationType, deleteOperationCount);
                            break;
                        case SELECT:
                            selectOperationCount++;
                            checkOperationCount(tableName, shadowOperationType, selectOperationCount);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }
    
    private static void checkOperationCount(final String tableName, final ShadowOperationType shadowOperationType, final int operationCount) {
        Preconditions.checkState(operationCount <= 1, "Column shadow algorithm `%s` operation only supports one column mapping in shadow table `%s`.",
                shadowOperationType.name(), tableName);
    }
    
    private static Collection<ShadowAlgorithm> createTableShadowAlgorithms(final Collection<String> tableShadowAlgorithmNames, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        return tableShadowAlgorithmNames.stream().map(shadowAlgorithms::get).filter(shadowAlgorithm -> !Objects.isNull(shadowAlgorithm)).collect(Collectors.toCollection(LinkedList::new));
    }
}
