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

package org.apache.shardingsphere.core.parse.filler.impl.dml;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.context.table.Tables;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import java.util.LinkedList;
import java.util.List;

/**
 * Predicate utils.
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PredicateUtils {
    
    /**
     * Find table name.
     * 
     * @param predicateSegment predicate segment
     * @param sqlStatement sql statement
     * @param shardingTableMetaData sharding table meta data
     * @return table name
     */
    // TODO find table from parent select statement, should find table in subquery level only
    public static Optional<String> findTableName(final PredicateSegment predicateSegment, final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return Optional.of(sqlStatement.getTables().getSingleTableName());
        }
        SelectStatement currentSelectStatement = (SelectStatement) sqlStatement;
        while (null != currentSelectStatement.getParentStatement()) {
            currentSelectStatement = currentSelectStatement.getParentStatement();
            Optional<String> tableName = findTableName(predicateSegment, currentSelectStatement.getTables(), shardingTableMetaData);
            if (tableName.isPresent()) {
                return tableName;
            }
        }
        return findTableName(predicateSegment, currentSelectStatement.getTables(), shardingTableMetaData);
    }
    
    private static Optional<String> findTableName(final PredicateSegment predicateSegment, final Tables tables, final ShardingTableMetaData shardingTableMetaData) {
        if (tables.isSingleTable()) {
            return Optional.of(tables.getSingleTableName());
        }
        if (predicateSegment.getColumn().getOwner().isPresent()) {
            Optional<Table> table = tables.find(predicateSegment.getColumn().getOwner().get().getName());
            return table.isPresent() ? Optional.of(table.get().getName()) : Optional.<String>absent();
        }
        return findTableNameFromMetaData(predicateSegment.getColumn().getName(), tables, shardingTableMetaData);
    }
    
    private static Optional<String> findTableNameFromMetaData(final String columnName, final Tables tables, final ShardingTableMetaData shardingTableMetaData) {
        for (String each : tables.getTableNames()) {
            if (shardingTableMetaData.containsColumn(each, columnName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    /**
     * Create condition of compare operator.
     * 
     * @param compareRightValue right value of compare operator
     * @param columnName column name
     * @param tableName table name
     * @param predicateSegment predicate segment
     * @return condition
     */
    public static Optional<Condition> createCompareCondition(final PredicateCompareRightValue compareRightValue, 
                                                             final String columnName, final String tableName, final PredicateSegment predicateSegment) {
        return compareRightValue.getExpression() instanceof SimpleExpressionSegment 
                ? Optional.of(new Condition(columnName, tableName, predicateSegment, compareRightValue.getExpression())) : Optional.<Condition>absent();
    }
    
    /**
     * Create condition of IN operator.
     *
     * @param inRightValue right value of IN operator
     * @param columnName column name
     * @param tableName table name
     * @param predicateSegment predicate segment
     * @return condition
     */
    public static Optional<Condition> createInCondition(final PredicateInRightValue inRightValue, final String columnName, final String tableName, final PredicateSegment predicateSegment) {
        List<ExpressionSegment> expressionSegments = new LinkedList<>();
        for (ExpressionSegment each : inRightValue.getSqlExpressions()) {
            if (each instanceof SimpleExpressionSegment) {
                expressionSegments.add(each);
            } else {
                return Optional.absent();
            }
        }
        return expressionSegments.isEmpty() ? Optional.<Condition>absent() : Optional.of(new Condition(columnName, tableName, predicateSegment, expressionSegments));
    }
}
