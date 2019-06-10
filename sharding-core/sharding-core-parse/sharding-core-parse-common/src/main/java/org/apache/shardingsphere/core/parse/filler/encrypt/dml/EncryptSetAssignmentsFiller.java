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

package org.apache.shardingsphere.core.parse.filler.encrypt.dml;

import lombok.Setter;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.context.condition.Column;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.UpdateStatement;

import java.util.LinkedList;
import java.util.List;

/**
 * Set assignments filler for encrypt.
 *
 * @author zhangliang
 */
@Setter
public final class EncryptSetAssignmentsFiller implements SQLSegmentFiller<SetAssignmentsSegment> {
    
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
        InsertValue insertValue = getInsertValue(sqlSegment, insertStatement);
        insertStatement.getValues().add(insertValue);
        insertStatement.setParametersIndex(insertValue.getParametersCount());
    }
    
    private InsertValue getInsertValue(final SetAssignmentsSegment sqlSegment, final InsertStatement insertStatement) {
        List<ExpressionSegment> columnValues = new LinkedList<>();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            columnValues.add(each.getValue());
        }
        return new InsertValue(columnValues);
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
