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

package org.apache.shardingsphere.sql.parser.sql.common.statement.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Insert statement.
 */
@Getter
@Setter
public abstract class InsertStatement extends AbstractSQLStatement implements DMLStatement {
    
    private SimpleTableSegment table;
    
    private InsertColumnsSegment insertColumns;
    
    private SubquerySegment insertSelect;
    
    private final Collection<InsertValuesSegment> values = new LinkedList<>();
    
    /**
     * Get insert columns segment.
     * 
     * @return insert columns segment
     */
    public Optional<InsertColumnsSegment> getInsertColumns() {
        return Optional.ofNullable(insertColumns);
    }
    
    /**
     * Get columns.
     * 
     * @return columns
     */
    public Collection<ColumnSegment> getColumns() {
        return null == insertColumns ? Collections.emptyList() : insertColumns.getColumns();
    }
    
    /**
     * Get insert select segment.
     *
     * @return insert select segment
     */
    public Optional<SubquerySegment> getInsertSelect() {
        return Optional.ofNullable(insertSelect);
    }
    
    /**
     * Judge is use default columns or not.
     * 
     * @return is use default columns or not
     */
    public abstract boolean useDefaultColumns();
    
    /**
     * Get column names.
     *
     * @return column names
     */
    public abstract List<String> getColumnNames();
    
    /**
     * Get value list count.
     *
     * @return value list count
     */
    public abstract int getValueListCount();
    
    /**
     * Get value count for per value list.
     * 
     * @return value count
     */
    public abstract int getValueCountForPerGroup();
    
    /**
     * Get all value expressions.
     * 
     * @return all value expressions
     */
    public abstract List<List<ExpressionSegment>> getAllValueExpressions();

    /**
     * Get column names for insert columns.
     * 
     * @return column names
     */
    protected List<String> getColumnNamesForInsertColumns() {
        List<String> result = new LinkedList<>();
        for (ColumnSegment each : getColumns()) {
            result.add(each.getIdentifier().getValue().toLowerCase());
        }
        return result;
    }

    /**
     * Get all value expressions from values.
     * @return all value expressions
     */
    protected List<List<ExpressionSegment>> getAllValueExpressionsFromValues() {
        List<List<ExpressionSegment>> result = new ArrayList<>(values.size());
        for (InsertValuesSegment each : values) {
            result.add(each.getValues());
        }
        return result;
    }
}
