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

package org.apache.shardingsphere.core.parse.filler.sharding.dml.update;

import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.exception.SQLParsingException;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.sql.context.condition.AndCondition;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.condition.Condition;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Set assignments filler.
 *
 * @author zhangliang
 * @author panjuan
 */
@Setter
public final class ShardingSetAssignmentsFiller implements SQLSegmentFiller<SetAssignmentsSegment>, ShardingRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final SetAssignmentsSegment sqlSegment, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof InsertStatement) {
            fillInsert(sqlSegment, (InsertStatement) sqlStatement);
        } else if (sqlStatement instanceof UpdateStatement) {
            fillUpdate(sqlSegment, (UpdateStatement) sqlStatement);
        }
    }
    
    private void fillInsert(final SetAssignmentsSegment sqlSegment, final InsertStatement insertStatement) {
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            insertStatement.getColumnNames().add(each.getColumn().getName());
        }
        int columnCount = getColumnCountExcludeAssistedQueryColumns(insertStatement);
        if (sqlSegment.getAssignments().size() != columnCount) {
            throw new SQLParsingException("INSERT INTO column size mismatch value size.");
        }
        AndCondition andCondition = new AndCondition();
        Iterator<String> columnNames = insertStatement.getColumnNames().iterator();
        List<ExpressionSegment> columnValues = new LinkedList<>();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            if (each.getValue() instanceof SimpleExpressionSegment) {
                fillShardingCondition(andCondition, columnNames.next(), insertStatement.getTables().getSingleTableName(), null, (SimpleExpressionSegment) each.getValue());
            }
            columnValues.add(each.getValue());
        }
        InsertValue insertValue = new InsertValue(columnValues);
        insertStatement.getValues().add(insertValue);
        insertStatement.getRouteCondition().getOrConditions().add(andCondition);
        insertStatement.setParametersIndex(insertValue.getParametersCount());
    }
    
    private int getColumnCountExcludeAssistedQueryColumns(final InsertStatement insertStatement) {
        String tableName = insertStatement.getTables().getSingleTableName();
        if (shardingTableMetaData.containsTable(tableName) && shardingTableMetaData.get(tableName).getColumns().size() == insertStatement.getColumnNames().size()) {
            return insertStatement.getColumnNames().size();
        }
        Integer assistedQueryColumnCount = shardingRule.getEncryptRule().getEncryptorEngine().getAssistedQueryColumnCount(insertStatement.getTables().getSingleTableName());
        return insertStatement.getColumnNames().size() - assistedQueryColumnCount;
    }
    
    private void fillShardingCondition(final AndCondition andCondition,
                                       final String columnName, final String tableName, final PredicateSegment predicateSegment, final SimpleExpressionSegment simpleExpressionSegment) {
        if (shardingRule.isShardingColumn(columnName, tableName)) {
            andCondition.getConditions().add(new Condition(new Column(columnName, tableName), predicateSegment, simpleExpressionSegment));
        }
    }
    
    private void fillUpdate(final SetAssignmentsSegment sqlSegment, final UpdateStatement updateStatement) {
        String tableName = updateStatement.getTables().getSingleTableName();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            fillEncryptCondition(each, tableName, updateStatement);
        }
    }
    
    private void fillEncryptCondition(final AssignmentSegment assignment, final String tableName, final UpdateStatement updateStatement) {
        Column column = new Column(assignment.getColumn().getName(), tableName);
        updateStatement.getAssignments().put(column, assignment.getValue());
    }
}
