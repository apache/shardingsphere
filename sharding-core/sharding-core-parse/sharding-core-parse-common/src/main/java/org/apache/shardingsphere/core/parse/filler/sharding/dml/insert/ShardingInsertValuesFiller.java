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

package org.apache.shardingsphere.core.parse.filler.sharding.dml.insert;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Insert values filler for sharding.
 *
 * @author zhangliang
 * @author panjuan
 */
@Setter
public final class ShardingInsertValuesFiller implements SQLSegmentFiller<InsertValuesSegment>, ShardingRuleAwareFiller {
    
    private ShardingRule shardingRule;
    
    @Override
    public void fill(final InsertValuesSegment sqlSegment, final SQLStatement sqlStatement) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        AndCondition andCondition = new AndCondition();
        Iterator<String> columnNames = getColumnNames(sqlSegment, insertStatement);
        for (ExpressionSegment each : sqlSegment.getValues()) {
            if (each instanceof SimpleExpressionSegment) {
                fillShardingCondition(andCondition, insertStatement.getTables().getSingleTableName(), columnNames.next(), null, (SimpleExpressionSegment) each);
            }
        }
        insertStatement.getRouteCondition().getOrConditions().add(andCondition);
        InsertValue insertValue = new InsertValue(sqlSegment.getValues());
        insertStatement.getValues().add(insertValue);
        insertStatement.setParametersIndex(insertStatement.getParametersIndex() + insertValue.getParametersCount());
        reviseInsertStatement(insertStatement, sqlSegment);
    }
    
    private Iterator<String> getColumnNames(final InsertValuesSegment sqlSegment, final InsertStatement insertStatement) {
        Collection<String> result = new ArrayList<>(insertStatement.getColumnNames());
        result.removeAll(shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumns(insertStatement.getTables().getSingleTableName()));
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (insertStatement.getColumnNames().size() != sqlSegment.getValues().size() && generateKeyColumnName.isPresent()) {
            result.remove(generateKeyColumnName.get());
        }
        return result.iterator();
    }
    
    private void fillShardingCondition(final AndCondition andCondition, final String tableName,
                                       final String columnName, final PredicateSegment predicateSegment, final SimpleExpressionSegment expressionSegment) {
        if (shardingRule.isShardingColumn(columnName, tableName)) {
            andCondition.getConditions().add(new Condition(new Column(columnName, tableName), predicateSegment, expressionSegment));
        }
    }
    
    private void reviseInsertStatement(final InsertStatement insertStatement, final InsertValuesSegment sqlSegment) {
        reviseInsertColumnNames(insertStatement, sqlSegment);
        setNeededToAppendGeneratedKey(insertStatement);
        setNeededToAppendAssistedColumns(insertStatement);
    }
    
    private void reviseInsertColumnNames(final InsertStatement insertStatement, final InsertValuesSegment sqlSegment) {
        Collection<String> insertColumns = new ArrayList<>(insertStatement.getColumnNames());
        insertColumns.removeAll(getAssistedQueryColumns(insertStatement));
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (insertStatement.getColumnNames().size() != sqlSegment.getValues().size() && generateKeyColumnName.isPresent()) {
            insertColumns.remove(generateKeyColumnName.get());
        }
        insertStatement.getColumnNames().clear();
        insertStatement.getColumnNames().addAll(insertColumns);
    }
    
    private void setNeededToAppendGeneratedKey(final InsertStatement insertStatement) {
        Optional<String> generateKeyColumnName = shardingRule.findGenerateKeyColumnName(insertStatement.getTables().getSingleTableName());
        if (generateKeyColumnName.isPresent() && !insertStatement.getColumnNames().contains(generateKeyColumnName.get())) {
            insertStatement.setNeededToAppendGeneratedKey(true);
        }
    }
    
    private void setNeededToAppendAssistedColumns(final InsertStatement insertStatement) {
        Collection<String> assistedQueryColumns = getAssistedQueryColumns(insertStatement);
        if (!assistedQueryColumns.isEmpty()) {
            insertStatement.setNeededToAppendAssistedColumns(true);
        }
    }
    
    private Collection<String> getAssistedQueryColumns(final InsertStatement insertStatement) {
        Collection<String> result = new ArrayList<>();
        Collection<String> assistedQueryColumns = shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumns(insertStatement.getTables().getSingleTableName());
        if (!assistedQueryColumns.isEmpty()) {
            result.addAll(assistedQueryColumns);
        }
        return result;
    }
}
