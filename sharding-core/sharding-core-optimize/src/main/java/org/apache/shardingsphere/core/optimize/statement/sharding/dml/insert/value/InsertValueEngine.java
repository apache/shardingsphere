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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.insert.value;

import org.apache.shardingsphere.core.parse.sql.context.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert value engine.
 *
 * @author zhangliang
 */
public final class InsertValueEngine {
    
    /**
     * Create insert values.
     *
     * @param insertStatement insert statement
     * @return insert values
     */
    public Collection<InsertValue> createInsertValues(final InsertStatement insertStatement) {
        return (insertStatement.getSetAssignment().isPresent()) ? getInsertValues(insertStatement.getSetAssignment().get()) : insertStatement.getValues();
    }
    
    private Collection<InsertValue> getInsertValues(final SetAssignmentsSegment sqlSegment) {
        Collection<InsertValue> result = new LinkedList<>();
        result.add(getInsertValue(sqlSegment));
        return result;
    }
    
    private InsertValue getInsertValue(final SetAssignmentsSegment sqlSegment) {
        List<ExpressionSegment> columnValues = new LinkedList<>();
        for (AssignmentSegment each : sqlSegment.getAssignments()) {
            columnValues.add(each.getValue());
        }
        return new InsertValue(columnValues);
    }
}
