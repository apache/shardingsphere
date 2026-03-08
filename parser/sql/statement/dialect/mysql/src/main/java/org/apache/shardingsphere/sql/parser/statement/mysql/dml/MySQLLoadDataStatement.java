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

package org.apache.shardingsphere.sql.parser.statement.mysql.dml;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.PartitionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.ColNameOrUserVarSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.IgnoreLinesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.load.LiteralValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.UnsupportedDistributeSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Load data statement for MySQL.
 */
@Getter
@Setter
public final class MySQLLoadDataStatement extends DMLStatement {
    
    private final SimpleTableSegment table;
    
    private final SQLStatementAttributes attributes;
    
    private boolean local;
    
    private LiteralValueSegment fileName;
    
    private final Collection<PartitionSegment> partitions = new LinkedList<>();
    
    private LiteralValueSegment columnSeparator;
    
    private LiteralValueSegment lineDelimiter;
    
    private IgnoreLinesSegment ignoreLines;
    
    private final List<ColNameOrUserVarSegment> columnList = new LinkedList<>();
    
    private SetAssignmentSegment setAssignments;
    
    private PropertiesSegment properties;
    
    public MySQLLoadDataStatement(final DatabaseType databaseType, final SimpleTableSegment table) {
        super(databaseType);
        this.table = table;
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(table), new UnsupportedDistributeSQLStatementAttribute());
    }
    
    /**
     * Get column separator.
     *
     * @return column separator segment
     */
    public Optional<LiteralValueSegment> getColumnSeparator() {
        return Optional.ofNullable(columnSeparator);
    }
    
    /**
     * Get line delimiter.
     *
     * @return line delimiter segment
     */
    public Optional<LiteralValueSegment> getLineDelimiter() {
        return Optional.ofNullable(lineDelimiter);
    }
    
    /**
     * Get ignore lines.
     *
     * @return ignore lines segment
     */
    public Optional<IgnoreLinesSegment> getIgnoreLines() {
        return Optional.ofNullable(ignoreLines);
    }
    
    /**
     * Get set assignments.
     *
     * @return set assignment segment
     */
    public Optional<SetAssignmentSegment> getSetAssignments() {
        return Optional.ofNullable(setAssignments);
    }
    
    /**
     * Get properties.
     *
     * @return properties segment
     */
    public Optional<PropertiesSegment> getProperties() {
        return Optional.ofNullable(properties);
    }
}
