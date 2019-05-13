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

package org.apache.shardingsphere.core.parse.antlr.filler.common.dml;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.antlr.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.antlr.sql.context.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.antlr.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.antlr.sql.context.table.Tables;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;

import java.util.LinkedList;
import java.util.List;

/**
 * Predicate utils.
 *
 * @author zhangliang
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
    // TODO hongjun: find table from parent select statement, should find table in subquery level only
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
            Optional<Table> table = tables.find(predicateSegment.getColumn().getOwner().get());
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
     * @param column column
     * @return condition
     */
    public static Optional<Condition> createCompareCondition(final PredicateCompareRightValue compareRightValue, final Column column) {
        return compareRightValue.getExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new Condition(column, ((SimpleExpressionSegment) compareRightValue.getExpression()).getSQLExpression())) : Optional.<Condition>absent();
    }
    
    /**
     * Create condition of IN operator.
     *
     * @param inRightValue right value of IN operator
     * @param column column
     * @return condition
     */
    public static Optional<Condition> createInCondition(final PredicateInRightValue inRightValue, final Column column) {
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        for (ExpressionSegment each : inRightValue.getSqlExpressions()) {
            if (!(each instanceof SimpleExpressionSegment)) {
                sqlExpressions.clear();
                break;
            } else {
                sqlExpressions.add(((SimpleExpressionSegment) each).getSQLExpression());
            }
        }
        return sqlExpressions.isEmpty() ? Optional.<Condition>absent() : Optional.of(new Condition(column, sqlExpressions));
    }
    
    /**
     * Create condition of BETWEEN ... AND ... operator.
     * 
     * @param betweenRightValue right value of BETWEEN operator
     * @param column column
     * @return condition
     */
    public static Optional<Condition> createBetweenCondition(final PredicateBetweenRightValue betweenRightValue, final Column column) {
        return betweenRightValue.getBetweenExpression() instanceof SimpleExpressionSegment && betweenRightValue.getAndExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new Condition(column,
                ((SimpleExpressionSegment) betweenRightValue.getBetweenExpression()).getSQLExpression(), ((SimpleExpressionSegment) betweenRightValue.getAndExpression()).getSQLExpression()))
                : Optional.<Condition>absent();
    }
}
