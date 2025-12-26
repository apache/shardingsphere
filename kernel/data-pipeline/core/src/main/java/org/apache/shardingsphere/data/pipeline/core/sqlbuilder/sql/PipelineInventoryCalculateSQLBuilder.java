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

package org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.segment.PipelineSQLSegmentBuilder;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pipeline inventory calculate SQL builder.
 */
public final class PipelineInventoryCalculateSQLBuilder {
    
    private final DialectPipelineSQLBuilder dialectSQLBuilder;
    
    private final PipelineSQLSegmentBuilder sqlSegmentBuilder;
    
    public PipelineInventoryCalculateSQLBuilder(final DatabaseType databaseType) {
        dialectSQLBuilder = DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, databaseType);
        sqlSegmentBuilder = new PipelineSQLSegmentBuilder(databaseType);
    }
    
    /**
     * Build range ordering SQL.
     *
     * @param qualifiedTable qualified table
     * @param columnNames column names
     * @param uniqueKeys unique keys, it may be primary key, not null
     * @param range range
     * @param pageQuery whether it is page query
     * @param shardingColumnsNames sharding columns names
     * @return built SQL
     */
    public String buildQueryRangeOrderingSQL(final QualifiedTable qualifiedTable, final Collection<String> columnNames, final List<String> uniqueKeys, final Range range,
                                             final boolean pageQuery, final List<String> shardingColumnsNames) {
        String result = buildQueryRangeOrderingSQL0(qualifiedTable, columnNames, uniqueKeys, range, shardingColumnsNames);
        return pageQuery ? dialectSQLBuilder.wrapWithPageQuery(result) : result;
    }
    
    private String buildQueryRangeOrderingSQL0(final QualifiedTable qualifiedTable, final Collection<String> columnNames, final List<String> uniqueKeys, final Range range,
                                               final List<String> shardingColumnsNames) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(qualifiedTable);
        String queryColumns = columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
        String firstUniqueKey = uniqueKeys.get(0);
        String orderByColumns = joinColumns(uniqueKeys, shardingColumnsNames).stream().map(each -> sqlSegmentBuilder.getEscapedIdentifier(each) + " ASC").collect(Collectors.joining(", "));
        if (null != range.getLower() && null != range.getUpper()) {
            return String.format("SELECT %s FROM %s WHERE %s AND %s ORDER BY %s", queryColumns, qualifiedTableName,
                    buildLowerQueryRangeCondition(range.isLowerInclusive(), firstUniqueKey),
                    buildUpperQueryRangeCondition(firstUniqueKey), orderByColumns);
        } else if (null != range.getLower()) {
            return String.format("SELECT %s FROM %s WHERE %s ORDER BY %s", queryColumns, qualifiedTableName,
                    buildLowerQueryRangeCondition(range.isLowerInclusive(), firstUniqueKey), orderByColumns);
        } else if (null != range.getUpper()) {
            return String.format("SELECT %s FROM %s WHERE %s ORDER BY %s", queryColumns, qualifiedTableName,
                    buildUpperQueryRangeCondition(firstUniqueKey), orderByColumns);
        } else {
            return String.format("SELECT %s FROM %s ORDER BY %s", queryColumns, qualifiedTableName, orderByColumns);
        }
    }
    
    private String buildLowerQueryRangeCondition(final boolean inclusive, final String firstUniqueKey) {
        String delimiter = inclusive ? ">=?" : ">?";
        return sqlSegmentBuilder.getEscapedIdentifier(firstUniqueKey) + delimiter;
    }
    
    private String buildUpperQueryRangeCondition(final String firstUniqueKey) {
        return sqlSegmentBuilder.getEscapedIdentifier(firstUniqueKey) + "<=?";
    }
    
    /**
     * Build point query SQL.
     *
     * @param qualifiedTable qualified table
     * @param columnNames column names
     * @param uniqueKeys unique keys, it may be primary key, not null
     * @param shardingColumnsNames sharding columns names, nullable
     * @return built SQL
     */
    public String buildPointQuerySQL(final QualifiedTable qualifiedTable, final Collection<String> columnNames, final List<String> uniqueKeys, final List<String> shardingColumnsNames) {
        String qualifiedTableName = sqlSegmentBuilder.getQualifiedTableName(qualifiedTable);
        String queryColumns = columnNames.stream().map(sqlSegmentBuilder::getEscapedIdentifier).collect(Collectors.joining(","));
        String equalsConditions = joinColumns(uniqueKeys, shardingColumnsNames).stream().map(each -> sqlSegmentBuilder.getEscapedIdentifier(each) + "=?").collect(Collectors.joining(" AND "));
        return String.format("SELECT %s FROM %s WHERE %s", queryColumns, qualifiedTableName, equalsConditions);
    }
    
    private List<String> joinColumns(final List<String> uniqueKeys, final List<String> shardingColumnsNames) {
        if (shardingColumnsNames.isEmpty()) {
            return uniqueKeys;
        }
        List<String> result = new ArrayList<>(uniqueKeys.size() + shardingColumnsNames.size());
        result.addAll(uniqueKeys);
        result.addAll(shardingColumnsNames);
        return result;
    }
}
