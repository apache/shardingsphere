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

package org.apache.shardingsphere.sql.parser.statement.postgresql.ddl;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.property.PropertiesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repack statement for PostgreSQL.
 */
@Getter
public final class PostgreSQLRepackStatement extends DDLStatement {
    
    private final SimpleTableSegment table;
    
    private final IndexSegment index;
    
    private final boolean usingIndex;
    
    private final List<ColumnSegment> columns;
    
    private final PropertiesSegment options;
    
    private final SQLStatementAttributes attributes;
    
    /**
     * Null targets and options represent omitted SQL clauses.
     *
     * @param databaseType database type
     * @param table table, or null when no relation is specified
     * @param index index, or null when no index name is specified
     * @param usingIndex whether USING INDEX is present
     * @param columns specified columns
     * @param options options, or null when no options are specified
     */
    public PostgreSQLRepackStatement(final DatabaseType databaseType, final SimpleTableSegment table, final IndexSegment index, final boolean usingIndex,
                                     final List<ColumnSegment> columns, final PropertiesSegment options) {
        super(databaseType);
        this.table = table;
        this.index = index;
        this.usingIndex = usingIndex;
        this.columns = columns;
        this.options = options;
        attributes = new SQLStatementAttributes(new TableSQLStatementAttribute(null == table ? Collections.emptyList() : Collections.singletonList(table)));
    }
    
    /**
     * Get table.
     *
     * @return table
     */
    public Optional<SimpleTableSegment> getTable() {
        return Optional.ofNullable(table);
    }
    
    /**
     * Get index.
     *
     * @return index
     */
    public Optional<IndexSegment> getIndex() {
        return Optional.ofNullable(index);
    }
    
    /**
     * Get options.
     *
     * @return options
     */
    public Optional<PropertiesSegment> getOptions() {
        return Optional.ofNullable(options);
    }
}
