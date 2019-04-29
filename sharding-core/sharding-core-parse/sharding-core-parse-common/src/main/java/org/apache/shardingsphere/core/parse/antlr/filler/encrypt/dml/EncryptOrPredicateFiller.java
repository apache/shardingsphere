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

package org.apache.shardingsphere.core.parse.antlr.filler.encrypt.dml;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.antlr.filler.api.EncryptRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.CompareValueExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.InValueExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.AndPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.EncryptColumnToken;
import org.apache.shardingsphere.core.parse.old.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Column;
import org.apache.shardingsphere.core.parse.old.parser.context.condition.Condition;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Table;
import org.apache.shardingsphere.core.parse.old.parser.context.table.Tables;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLNumberExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLPlaceholderExpression;
import org.apache.shardingsphere.core.parse.old.parser.expression.SQLTextExpression;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Or predicate filler for encrypt.
 *
 * @author duhongjun
 */
@Setter
public final class EncryptOrPredicateFiller implements SQLSegmentFiller<OrPredicateSegment>, EncryptRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private EncryptRule encryptRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        Collection<Integer> stopIndexes = new HashSet<>();
        for (AndPredicateSegment each : sqlSegment.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (stopIndexes.add(predicate.getStopIndex())) {
                    Optional<String> tableName = findTableName(predicate, sqlStatement);
                    // TODO panjuan: spilt EncryptRule and EncryptorEngine, cannot pass EncryptorEngine to parse module
                    if (tableName.isPresent() && encryptRule.getEncryptorEngine().getShardingEncryptor(tableName.get(), predicate.getColumn().getName()).isPresent()) {
                        fill(predicate, tableName.get(), sqlStatement);
                    }
                }
            }
        }
    }
    
    private void fill(final PredicateSegment predicateSegment, final String tableName, final SQLStatement sqlStatement) {
        AndCondition andCondition;
        if (sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().isEmpty()) {
            andCondition = new AndCondition();
            sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().add(andCondition);
        } else {
            andCondition = sqlStatement.getEncryptConditions().getOrCondition().getAndConditions().get(0);
        }
        Optional<Condition> condition = createCondition(predicateSegment, sqlStatement);
        if (condition.isPresent()) {
            andCondition.getConditions().add(condition.get());
            sqlStatement.getSQLTokens().add(
                    new EncryptColumnToken(predicateSegment.getColumn().getStartIndex(), predicateSegment.getStopIndex(), new Column(predicateSegment.getColumn().getName(), tableName), true));
        }
    }
    
    private Optional<Condition> createCondition(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        if (!isEncryptCondition(predicateSegment.getOperator())) {
            return Optional.absent();
        }
        Optional<String> tableName = findTableName(predicateSegment, sqlStatement);
        if (!tableName.isPresent() || !encryptRule.getEncryptorEngine().getShardingEncryptor(tableName.get(), predicateSegment.getColumn().getName()).isPresent()) {
            return Optional.absent();
        }
        Column column = new Column(predicateSegment.getColumn().getName(), tableName.get());
        if (predicateSegment.getExpression() instanceof CompareValueExpressionSegment) {
            return createEqualCondition((CompareValueExpressionSegment) predicateSegment.getExpression(), column, sqlStatement.getLogicSQL());
        }
        if (predicateSegment.getExpression() instanceof InValueExpressionSegment) {
            return createInCondition((InValueExpressionSegment) predicateSegment.getExpression(), column, sqlStatement.getLogicSQL());
        }
        return Optional.absent();
    }
    
    private boolean isEncryptCondition(final String operator) {
        return Symbol.EQ.getLiterals().equals(operator) || ShardingOperator.IN.name().equals(operator);
    }
    
    private Optional<Condition> createEqualCondition(final CompareValueExpressionSegment expressionSegment, final Column column, final String sql) {
        SQLExpression sqlExpression = expressionSegment.getExpression().getSQLExpression(sql);
        return isEncryptExpressionType(sqlExpression) ? Optional.of(new Condition(column, sqlExpression)) : Optional.<Condition>absent();
    }
    
    private Optional<Condition> createInCondition(final InValueExpressionSegment expressionSegment, final Column column, final String sql) {
        List<SQLExpression> sqlExpressions = new LinkedList<>();
        for (ExpressionSegment each : expressionSegment.getSqlExpressions()) {
            SQLExpression sqlExpression = each.getSQLExpression(sql);
            if (!isEncryptExpressionType(sqlExpression)) {
                sqlExpressions.clear();
                break;
            } else {
                sqlExpressions.add(sqlExpression);
            }
        }
        return sqlExpressions.isEmpty() ? Optional.<Condition>absent() : Optional.of(new Condition(column, sqlExpressions));
    }
    
    private boolean isEncryptExpressionType(final SQLExpression sqlExpression) {
        return sqlExpression instanceof SQLPlaceholderExpression || sqlExpression instanceof SQLNumberExpression || sqlExpression instanceof SQLTextExpression;
    }
    
    // TODO hongjun: find table from parent select statement, should find table in subquery level only
    private Optional<String> findTableName(final PredicateSegment predicateSegment, final SQLStatement sqlStatement) {
        if (!(sqlStatement instanceof SelectStatement)) {
            return Optional.of(sqlStatement.getTables().getSingleTableName());
        }
        SelectStatement currentSelectStatement = (SelectStatement) sqlStatement;
        while (null != currentSelectStatement.getParentStatement()) {
            currentSelectStatement = currentSelectStatement.getParentStatement();
            Optional<String> tableName = findTableName(predicateSegment, currentSelectStatement.getTables());
            if (tableName.isPresent()) {
                return tableName;
            }
        }
        return findTableName(predicateSegment, currentSelectStatement.getTables());
    }
    
    private Optional<String> findTableName(final PredicateSegment predicateSegment, final Tables tables) {
        if (predicateSegment.getColumn().getOwner().isPresent()) {
            Optional<Table> table = tables.find(predicateSegment.getColumn().getOwner().get());
            return table.isPresent() ? Optional.of(table.get().getName()) : Optional.<String>absent();
        }
        return findTableNameFromMetaData(predicateSegment.getColumn().getName(), tables);
    }
    
    private Optional<String> findTableNameFromMetaData(final String columnName, final Tables tables) {
        for (String each : tables.getTableNames()) {
            if (shardingTableMetaData.containsColumn(each, columnName)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
}
