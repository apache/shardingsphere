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

package org.apache.shardingsphere.sql.parser.statement.doris.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.job.JobNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnMappingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.LinkedList;
import java.util.Collection;
import java.util.Optional;

/**
 * Create routine load statement for Doris.
 */
@Getter
@Setter
public final class DorisCreateRoutineLoadStatement extends DMLStatement {
    
    private DatabaseSegment database;
    
    private JobNameSegment jobName;
    
    private SimpleTableSegment table;
    
    private String mergeType;
    
    private String columnSeparator;
    
    private final Collection<ColumnMappingSegment> columnMappings = new LinkedList<>();
    
    private final Collection<PartitionSegment> partitions = new LinkedList<>();
    
    private ExpressionSegment precedingFilter;
    
    private WhereSegment where;
    
    private ExpressionSegment deleteOn;
    
    private OrderBySegment orderBy;
    
    private PropertiesSegment jobProperties;
    
    private String dataSource;
    
    private PropertiesSegment dataSourceProperties;
    
    private String comment;
    
    public DorisCreateRoutineLoadStatement(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    /**
     * Get database.
     *
     * @return database segment
     */
    public Optional<DatabaseSegment> getDatabase() {
        return Optional.ofNullable(database);
    }
    
    /**
     * Get job name.
     *
     * @return job name segment
     */
    public Optional<JobNameSegment> getJobName() {
        return Optional.ofNullable(jobName);
    }
    
    /**
     * Get table.
     *
     * @return table segment
     */
    public Optional<SimpleTableSegment> getTable() {
        return Optional.ofNullable(table);
    }
    
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
     * Get where segment.
     *
     * @return where segment
     */
    public Optional<WhereSegment> getWhere() {
        return Optional.ofNullable(where);
    }
    
    /**
     * Get preceding filter expression.
     *
     * @return preceding filter expression
     */
    public Optional<ExpressionSegment> getPrecedingFilter() {
        return Optional.ofNullable(precedingFilter);
    }
    
    /**
     * Get delete on expression.
     *
     * @return delete on expression
     */
    public Optional<ExpressionSegment> getDeleteOn() {
        return Optional.ofNullable(deleteOn);
    }
    
    /**
     * Get order by segment.
     *
     * @return order by segment
     */
    public Optional<OrderBySegment> getOrderBy() {
        return Optional.ofNullable(orderBy);
    }
    
    /**
     * Get job properties.
     *
     * @return job properties segment
     */
    public Optional<PropertiesSegment> getJobProperties() {
        return Optional.ofNullable(jobProperties);
    }
    
    /**
     * Get data source.
     *
     * @return data source
     */
    public Optional<String> getDataSource() {
        return Optional.ofNullable(dataSource);
    }
    
    /**
     * Get data source properties.
     *
     * @return data source properties segment
     */
    public Optional<PropertiesSegment> getDataSourceProperties() {
        return Optional.ofNullable(dataSourceProperties);
    }
    
    /**
     * Get comment.
     *
     * @return comment
     */
    public Optional<String> getComment() {
        return Optional.ofNullable(comment);
    }
}
