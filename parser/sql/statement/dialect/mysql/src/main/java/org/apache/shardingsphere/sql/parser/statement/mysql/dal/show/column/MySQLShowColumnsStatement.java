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

package org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.column;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromDatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.ColumnInResultSetSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.FromDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

import java.util.Optional;

/**
 * Show columns statement.
 */
@Getter
public final class MySQLShowColumnsStatement extends DALStatement {
    
    private final SimpleTableSegment table;
    
    private final FromDatabaseSegment fromDatabase;
    
    private final ShowFilterSegment filter;
    
    private SQLStatementAttributes attributes;
    
    public MySQLShowColumnsStatement(final DatabaseType databaseType, final SimpleTableSegment table, final FromDatabaseSegment fromDatabase, final ShowFilterSegment filter) {
        super(databaseType);
        this.table = table;
        this.fromDatabase = fromDatabase;
        this.filter = filter;
    }
    
    /**
     * Get from database.
     *
     * @return from database
     */
    public Optional<FromDatabaseSegment> getFromDatabase() {
        return Optional.ofNullable(fromDatabase);
    }
    
    /**
     * Get filter segment.
     *
     * @return filter segment
     */
    public Optional<ShowFilterSegment> getFilter() {
        return Optional.ofNullable(filter);
    }
    
    @Override
    public void buildAttributes() {
        attributes = new SQLStatementAttributes(new ColumnInResultSetSQLStatementAttribute(1), new FromDatabaseSQLStatementAttribute(fromDatabase), new TableSQLStatementAttribute(table));
    }
}
