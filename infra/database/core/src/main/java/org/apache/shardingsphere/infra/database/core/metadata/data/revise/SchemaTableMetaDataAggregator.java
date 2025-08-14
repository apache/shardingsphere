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

package org.apache.shardingsphere.infra.database.core.metadata.data.revise;

import com.sphereex.dbplusengine.SphereEx;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ConstraintMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Schema table meta data aggregator.
 */
@Slf4j
public final class SchemaTableMetaDataAggregator {
    
    /**
     * Aggregate table metadata.
     *
     * @param checkTableMetaDataEnabled check table metadata enabled
     * @param tableMetaDataMap table metadata map
     * @return table metadata
     */
    public Collection<TableMetaData> aggregate(final boolean checkTableMetaDataEnabled, final Map<String, Collection<TableMetaData>> tableMetaDataMap) {
        Collection<TableMetaData> result = new LinkedList<>();
        for (Map.Entry<String, Collection<TableMetaData>> entry : tableMetaDataMap.entrySet()) {
            if (checkTableMetaDataEnabled) {
                checkUniformed(entry.getKey(), entry.getValue());
            }
            result.add(entry.getValue().iterator().next());
        }
        return result;
    }
    
    @SphereEx(SphereEx.Type.MODIFY)
    private void checkUniformed(final String logicTableName, final Collection<TableMetaData> tableMetaDataList) {
        TableMetaData sample = tableMetaDataList.iterator().next();
        Collection<TableMetaDataViolation> violations = tableMetaDataList.stream()
                .filter(each -> !isEqualsTableMetadata(each, sample)).map(each -> new TableMetaDataViolation(each.getName(), each)).collect(Collectors.toList());
        Collection<String> violationNames = violations.isEmpty() ? Collections.emptyList() : getViolationNames(violations);
        ShardingSpherePreconditions.checkMustEmpty(violations,
                () -> new RuleAndStorageMetaDataMismatchedException(createErrorReason(logicTableName, violations, sample), getActualTableName(sample), violationNames));
    }
    
    @SphereEx
    private Collection<String> getViolationNames(final Collection<TableMetaDataViolation> violations) {
        return violations.stream().map(each -> getActualTableName(each.getTableMetaData())).collect(Collectors.toList());
    }
    
    @SphereEx
    private static boolean isEqualsTableMetadata(final TableMetaData current, final TableMetaData sample) {
        if (!Objects.equals(current.getName(), sample.getName())) {
            return false;
        }
        if (!Objects.equals(current.getType(), sample.getType())) {
            return false;
        }
        if (current.getColumns().size() != sample.getColumns().size()) {
            return false;
        }
        List<ColumnMetaData> sortedColumns1 = current.getColumns().stream().sorted(Comparator.comparing(ColumnMetaData::getName)).collect(Collectors.toList());
        List<ColumnMetaData> sortedColumns2 = sample.getColumns().stream().sorted(Comparator.comparing(ColumnMetaData::getName)).collect(Collectors.toList());
        if (!sortedColumns1.toString().equals(sortedColumns2.toString())) {
            return false;
        }
        if (current.getIndexes().size() != sample.getIndexes().size()) {
            return false;
        }
        List<IndexMetaData> sortedIndexes1 = current.getIndexes().stream().sorted(Comparator.comparing(IndexMetaData::getName)).collect(Collectors.toList());
        List<IndexMetaData> sortedIndexes2 = sample.getIndexes().stream().sorted(Comparator.comparing(IndexMetaData::getName)).collect(Collectors.toList());
        if (!sortedIndexes1.toString().equals(sortedIndexes2.toString())) {
            return false;
        }
        if (current.getConstraints().size() != sample.getConstraints().size()) {
            return false;
        }
        List<ConstraintMetaData> sortedConstraint1 = current.getConstraints().stream().sorted(Comparator.comparing(ConstraintMetaData::getName)).collect(Collectors.toList());
        List<ConstraintMetaData> sortedConstraint2 = sample.getConstraints().stream().sorted(Comparator.comparing(ConstraintMetaData::getName)).collect(Collectors.toList());
        return sortedConstraint1.toString().equals(sortedConstraint2.toString());
    }
    
    @SphereEx(SphereEx.Type.MODIFY)
    private String createErrorReason(final String logicTableName, final Collection<TableMetaDataViolation> violations, final TableMetaData sample) {
        // SPEX CHANGED: BEGIN
        StringBuilder fullLogBuilder = new StringBuilder(
                "Can not get uniformed table structure for logic table '%s',\n sample: %s\n different meta data of actual tables are as follows: ").append(System.lineSeparator());
        // SPEX CHANGED: END
        for (TableMetaDataViolation each : violations) {
            fullLogBuilder.append("actual table: ").append(getActualTableName(each.getTableMetaData())).append(", meta data: ").append(each.getTableMetaData()).append(System.lineSeparator());
        }
        log.info(String.format(fullLogBuilder.toString(), logicTableName, getActualTableName(sample)));
        return String.format("Table structure conflict: logical table: '%s' (sample: '%s', diff: '%s')", logicTableName, getActualTableName(sample),
                violations.stream().map(each -> getActualTableName(each.getTableMetaData())).collect(Collectors.toList()));
    }
    
    @SphereEx
    private static String getActualTableName(final TableMetaData table) {
        return null == table.getActualTableName() || null == table.getStorageUnitName() ? table.getName() : table.getStorageUnitName() + "." + table.getActualTableName();
    }
}
