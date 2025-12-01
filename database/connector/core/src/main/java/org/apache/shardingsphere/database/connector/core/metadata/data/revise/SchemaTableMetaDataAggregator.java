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

package org.apache.shardingsphere.database.connector.core.metadata.data.revise;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Schema table meta data aggregator.
 */
@RequiredArgsConstructor
public final class SchemaTableMetaDataAggregator {
    
    private final boolean checkTableMetaDataEnabled;
    
    /**
     * Aggregate table meta data.
     *
     * @param tableMetaDataMap table meta data map
     * @return table meta data
     */
    public Collection<TableMetaData> aggregate(final Map<String, Collection<TableMetaData>> tableMetaDataMap) {
        Collection<TableMetaData> result = new LinkedList<>();
        for (Entry<String, Collection<TableMetaData>> entry : tableMetaDataMap.entrySet()) {
            if (checkTableMetaDataEnabled) {
                checkUniformed(entry.getKey(), entry.getValue());
            }
            result.add(entry.getValue().iterator().next());
        }
        return result;
    }
    
    private void checkUniformed(final String logicTableName, final Collection<TableMetaData> tableMetaDataList) {
        TableMetaData sample = tableMetaDataList.iterator().next();
        Collection<TableMetaDataViolation> violations = tableMetaDataList.stream()
                .filter(each -> !sample.toString().equals(each.toString())).map(each -> new TableMetaDataViolation(each.getName(), each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(violations, () -> new RuleAndStorageMetaDataMismatchedException(createErrorReason(logicTableName, violations)));
    }
    
    private String createErrorReason(final String logicTableName, final Collection<TableMetaDataViolation> violations) {
        StringBuilder result = new StringBuilder(
                "Can not get uniformed table structure for logic table '%s', it has different meta data of actual tables are as follows: ").append(System.lineSeparator());
        for (TableMetaDataViolation each : violations) {
            result.append("actual table: ").append(each.getActualTableName()).append(", meta data: ").append(each.getTableMetaData()).append(System.lineSeparator());
        }
        return String.format(result.toString(), logicTableName);
    }
}
