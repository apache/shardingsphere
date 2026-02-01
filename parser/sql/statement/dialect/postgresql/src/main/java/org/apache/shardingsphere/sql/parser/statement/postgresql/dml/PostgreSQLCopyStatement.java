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

package org.apache.shardingsphere.sql.parser.statement.postgresql.dml;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.UnsupportedDistributeSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Copy statement for PostgreSQL.
 */
@Getter
public final class PostgreSQLCopyStatement extends DMLStatement {
    
    private final SimpleTableSegment table;
    
    private final Collection<ColumnSegment> columns;
    
    private final PrepareStatementQuerySegment prepareStatementQuery;
    
    private SQLStatementAttributes attributes;
    
    public PostgreSQLCopyStatement(final DatabaseType databaseType,
                                   final SimpleTableSegment table, final Collection<ColumnSegment> columns, final PrepareStatementQuerySegment prepareStatementQuery) {
        super(databaseType);
        this.table = table;
        this.columns = columns;
        this.prepareStatementQuery = prepareStatementQuery;
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
     * Get prepare statement query segment.
     *
     * @return prepare statement query segment
     */
    public Optional<PrepareStatementQuerySegment> getPrepareStatementQuery() {
        return Optional.ofNullable(prepareStatementQuery);
    }
    
    @Override
    public void buildAttributes() {
        attributes = new SQLStatementAttributes(
                new TableSQLStatementAttribute(null == table ? Collections.emptyList() : Collections.singletonList(table)), new UnsupportedDistributeSQLStatementAttribute());
    }
}
