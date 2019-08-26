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

package org.apache.shardingsphere.core.parse.sql.statement.dml;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.assignment.SetAssignmentsSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.generic.TableSegmentAvailable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert statement.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@Getter
@Setter
public final class InsertStatement extends DMLStatement implements TableSegmentAvailable {
    
    private TableSegment table;
    
    private SetAssignmentsSegment setAssignment;
    
    private final Collection<ColumnSegment> columns = new LinkedList<>();
    
    private final Collection<InsertValuesSegment> values = new LinkedList<>();
    
    /**
     * Get set assignment segment.
     * 
     * @return set assignment segment
     */
    public Optional<SetAssignmentsSegment> getSetAssignment() {
        return Optional.fromNullable(setAssignment);
    }
    
    /**
     * Judge is use default columns or not.
     * 
     * @return is use default columns or not
     */
    public boolean useDefaultColumns() {
        return columns.isEmpty() && null == setAssignment;
    }
    
    /**
     * Get column names.
     *
     * @return column names
     */
    public Collection<String> getColumnNames() {
        return null == setAssignment ? getColumnNamesForInsertColumns() : getColumnNamesForSetAssignment();
    }
    
    private Collection<String> getColumnNamesForInsertColumns() {
        Collection<String> result = new LinkedList<>();
        for (ColumnSegment each : columns) {
            result.add(each.getName());
        }
        return result;
    }
    
    private Collection<String> getColumnNamesForSetAssignment() {
        Collection<String> result = new LinkedList<>();
        for (AssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getColumn().getName());
        }
        return result;
    }
    
    /**
     * Get value list count.
     *
     * @return value list count
     */
    public int getValueListCount() {
        return null == setAssignment ? values.size() : 1;
    }
    
    /**
     * Get value count for per value list.
     * 
     * @return value count
     */
    public int getValueCountForPerGroup() {
        if (!values.isEmpty()) {
            return values.iterator().next().getValues().size();
        }
        if (null != setAssignment) {
            return setAssignment.getAssignments().size();
        }
        return 0;
    }
    
    /**
     * Get all value expressions.
     * 
     * @return all value expressions
     */
    public Collection<Collection<ExpressionSegment>> getAllValueExpressions() {
        return null == setAssignment ? getAllValueExpressionsFromValues() : Collections.singletonList(getAllValueExpressionsFromSetAssignment());
    }
    
    private Collection<Collection<ExpressionSegment>> getAllValueExpressionsFromValues() {
        Collection<Collection<ExpressionSegment>> result = new LinkedList<>();
        for (InsertValuesSegment each : values) {
            result.add(each.getValues());
        }
        return result;
    }
    
    private Collection<ExpressionSegment> getAllValueExpressionsFromSetAssignment() {
        List<ExpressionSegment> result = new LinkedList<>();
        for (AssignmentSegment each : setAssignment.getAssignments()) {
            result.add(each.getValue());
        }
        return result;
    }
}
