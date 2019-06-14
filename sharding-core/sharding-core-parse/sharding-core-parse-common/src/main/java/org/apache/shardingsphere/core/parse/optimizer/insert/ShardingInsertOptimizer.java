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

package org.apache.shardingsphere.core.parse.optimizer.insert;

import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.optimizer.SQLStatementOptimizer;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Iterator;

/**
 * Insert optimizer for sharding.
 *
 * @author zhangliang
 */
@Setter
public final class ShardingInsertOptimizer implements SQLStatementOptimizer<ShardingRule> {
    
    private ShardingRule rule;
    
    @Override
    public void optimize(final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) {
        InsertStatement insertStatement = (InsertStatement) sqlStatement;
        for (InsertValue each : insertStatement.getValues()) {
            fillCondition(each, insertStatement);
        }
    }
    
    private void fillCondition(final InsertValue insertValue, final InsertStatement insertStatement) {
        AndCondition andCondition = new AndCondition();
        Iterator<String> columnNames = insertStatement.getColumnNames().iterator();
        for (ExpressionSegment each : insertValue.getAssignments()) {
            if (each instanceof SimpleExpressionSegment) {
                fillShardingCondition(andCondition, insertStatement.getTables().getSingleTableName(), columnNames.next(), (SimpleExpressionSegment) each);
            }
        }
        insertStatement.getShardingConditions().getOrConditions().add(andCondition);
    }
    
    private void fillShardingCondition(final AndCondition andCondition, final String tableName, final String columnName, final SimpleExpressionSegment expressionSegment) {
        if (rule.isShardingColumn(columnName, tableName)) {
            andCondition.getConditions().add(new Condition(new Column(columnName, tableName), null, expressionSegment));
        }
    }
}
