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

package io.shardingsphere.core.rewrite;

import com.google.common.base.Strings;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.optimizer.condition.ShardingCondition;
import io.shardingsphere.core.optimizer.insert.InsertShardingCondition;
import io.shardingsphere.core.rewrite.placeholder.IndexPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.InsertValuesPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.SchemaPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.ShardingPlaceholder;
import io.shardingsphere.core.rewrite.placeholder.TablePlaceholder;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SQL builder.
 *
 * @author gaohongtao
 * @author zhangliang
 * @author maxiaoguang
 */
public final class SQLBuilder {
    
    private final List<Object> segments;
    
    private final List<Object> parameters;
    
    private StringBuilder currentSegment;
    
    public SQLBuilder() {
        this(Collections.emptyList());
    }
    
    public SQLBuilder(final List<Object> parameters) {
        segments = new LinkedList<>();
        this.parameters = parameters;
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Append literals.
     *
     * @param literals literals for SQL
     */
    public void appendLiterals(final String literals) {
        currentSegment.append(literals);
    }
    
    /**
     * Append sharding placeholder.
     *
     * @param shardingPlaceholder sharding placeholder
     */
    public void appendPlaceholder(final ShardingPlaceholder shardingPlaceholder) {
        segments.add(shardingPlaceholder);
        currentSegment = new StringBuilder();
        segments.add(currentSegment);
    }
    
    /**
     * Convert to SQL unit.
     *
     * @param tableUnit table unit
     * @param logicAndActualTableMap logic and actual map
     * @param shardingRule sharding rule
     * @param shardingDataSourceMetaData sharding data source meta data
     * @return SQL unit
     */
    public SQLUnit toSQL(final TableUnit tableUnit, final Map<String, String> logicAndActualTableMap, final ShardingRule shardingRule, final ShardingDataSourceMetaData shardingDataSourceMetaData) {
        StringBuilder result = new StringBuilder();
        List<Object> insertParameters = new LinkedList<>();
        for (Object each : segments) {
            if (!(each instanceof ShardingPlaceholder)) {
                result.append(each);
                continue;
            }
            String logicTableName = ((ShardingPlaceholder) each).getLogicTableName();
            String actualTableName = logicAndActualTableMap.get(logicTableName);
            if (each instanceof TablePlaceholder) {
                appendTablePlaceholder((TablePlaceholder) each, actualTableName, result);
            } else if (each instanceof SchemaPlaceholder) {
                appendSchemaPlaceholder(shardingRule, shardingDataSourceMetaData, actualTableName, result);
            } else if (each instanceof IndexPlaceholder) {
                appendIndexPlaceholder((IndexPlaceholder) each, actualTableName, result);
            } else if (each instanceof InsertValuesPlaceholder) {
                appendInsertValuesPlaceholder(tableUnit, insertParameters, (InsertValuesPlaceholder) each, result);
            } else {
                result.append(each);
            }
        }
        List<List<Object>> parameterSets = insertParameters.isEmpty() ? new ArrayList<>(Collections.singleton(parameters)) : new ArrayList<>(Collections.singleton(insertParameters));
        return new SQLUnit(result.toString(), parameterSets);
    }
    
    /**
     * Convert to SQL unit.
     * 
     * @param masterSlaveRule master slave rule
     * @param shardingDataSourceMetaData sharding data source meta data
     * @return SQL
     */
    public String toSQL(final MasterSlaveRule masterSlaveRule, final ShardingDataSourceMetaData shardingDataSourceMetaData) {
        StringBuilder result = new StringBuilder();
        for (Object each : segments) {
            if (each instanceof SchemaPlaceholder) {
                result.append(shardingDataSourceMetaData.getActualDataSourceMetaData(masterSlaveRule.getMasterDataSourceName()).getSchemeName());
            } else {
                result.append(each);
            }
        }
        return result.toString();
    }
    
    private void appendTablePlaceholder(final TablePlaceholder tablePlaceholder, final String actualTableName, final StringBuilder stringBuilder) {
        final String logicTableName = tablePlaceholder.getLogicTableName();
        final String originalLiterals = tablePlaceholder.getOriginalLiterals();
        if (logicTableName.length() == originalLiterals.length()) {
            stringBuilder.append(null == actualTableName ? logicTableName : actualTableName);
        } else {
            final char delimiter = originalLiterals.charAt(0);
            stringBuilder.append(null == actualTableName ? originalLiterals : delimiter + actualTableName + delimiter);
        }
    }
    
    private void appendSchemaPlaceholder(final ShardingRule shardingRule, final ShardingDataSourceMetaData shardingDataSourceMetaData,
                                         final String actualTableName, final StringBuilder stringBuilder) {
        stringBuilder.append(shardingDataSourceMetaData.getActualDataSourceMetaData(shardingRule.getActualDataSourceNameByActualTableName(actualTableName)).getSchemeName());
    }
    
    private void appendIndexPlaceholder(final IndexPlaceholder indexPlaceholder, final String actualTableName, final StringBuilder stringBuilder) {
        stringBuilder.append(indexPlaceholder.getLogicIndexName());
        if (!Strings.isNullOrEmpty(actualTableName)) {
            stringBuilder.append("_");
            stringBuilder.append(actualTableName);
        }
    }
    
    private void appendInsertValuesPlaceholder(final TableUnit tableUnit, final List<Object> parameters, final InsertValuesPlaceholder insertValuesPlaceholder, final StringBuilder stringBuilder) {
        List<String> expressions = new LinkedList<>();
        for (ShardingCondition each : insertValuesPlaceholder.getShardingConditions().getShardingConditions()) {
            processInsertShardingCondition(tableUnit, (InsertShardingCondition) each, expressions, parameters);
        }
        int count = 0;
        for (String each : expressions) {
            if (0 != count) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(each);
            count++;
        }
    }
    
    private void processInsertShardingCondition(final TableUnit tableUnit, final InsertShardingCondition shardingCondition, final List<String> expressions, final List<Object> parameters) {
        for (DataNode each : shardingCondition.getDataNodes()) {
            if (each.getDataSourceName().equals(tableUnit.getDataSourceName()) && each.getTableName().equals(tableUnit.getRoutingTables().iterator().next().getActualTableName())) {
                expressions.add(shardingCondition.getInsertValueExpression());
                parameters.addAll(shardingCondition.getParameters());
                return;
            }
        }
        if (shardingCondition.getDataNodes().isEmpty()) {
            expressions.add(shardingCondition.getInsertValueExpression());
            parameters.addAll(shardingCondition.getParameters());
        }
    }
}
