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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
}
