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

package org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.sql.parser.statement.core.segment.SQLSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Broker load data desc segment.
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class BrokerLoadDataDescSegment implements SQLSegment {
    
    private final int startIndex;
    
    private final int stopIndex;
    
    private String mergeType;
    
    private final Collection<String> filePaths = new LinkedList<>();
    
    private boolean negative;
    
    private SimpleTableSegment table;
    
    private final Collection<PartitionSegment> partitions = new LinkedList<>();
    
    private String columnSeparator;
    
    private String lineDelimiter;
    
    private String formatType;
    
    private String compressType;
    
    private final Collection<ColumnSegment> columnList = new LinkedList<>();
    
    private final Collection<ColumnSegment> columnsFromPath = new LinkedList<>();
    
    private final Collection<ColumnAssignmentSegment> setAssignments = new LinkedList<>();
    
    private ExpressionSegment precedingFilter;
    
    private ExpressionSegment whereExpr;
    
    private ExpressionSegment deleteOnExpr;
    
    private String orderByColumn;
    
    private PropertiesSegment dataProperties;
    
    /**
     * Get merge type.
     *
     * @return merge type
     */
    public Optional<String> getMergeType() {
        return Optional.ofNullable(mergeType);
    }
    
    /**
     * Get column separator.
     *
     * @return column separator
     */
    public Optional<String> getColumnSeparator() {
        return Optional.ofNullable(columnSeparator);
    }
    
    /**
     * Get line delimiter.
     *
     * @return line delimiter
     */
    public Optional<String> getLineDelimiter() {
        return Optional.ofNullable(lineDelimiter);
    }
    
    /**
     * Get format type.
     *
     * @return format type
     */
    public Optional<String> getFormatType() {
        return Optional.ofNullable(formatType);
    }
    
    /**
     * Get compress type.
     *
     * @return compress type
     */
    public Optional<String> getCompressType() {
        return Optional.ofNullable(compressType);
    }
    
    /**
     * Get preceding filter.
     *
     * @return preceding filter expression
     */
    public Optional<ExpressionSegment> getPrecedingFilter() {
        return Optional.ofNullable(precedingFilter);
    }
    
    /**
     * Get where expression.
     *
     * @return where expression
     */
    public Optional<ExpressionSegment> getWhereExpr() {
        return Optional.ofNullable(whereExpr);
    }
    
    /**
     * Get delete on expression.
     *
     * @return delete on expression
     */
    public Optional<ExpressionSegment> getDeleteOnExpr() {
        return Optional.ofNullable(deleteOnExpr);
    }
    
    /**
     * Get order by column.
     *
     * @return order by column
     */
    public Optional<String> getOrderByColumn() {
        return Optional.ofNullable(orderByColumn);
    }
    
    /**
     * Get data properties.
     *
     * @return data properties segment
     */
    public Optional<PropertiesSegment> getDataProperties() {
        return Optional.ofNullable(dataProperties);
    }
}
