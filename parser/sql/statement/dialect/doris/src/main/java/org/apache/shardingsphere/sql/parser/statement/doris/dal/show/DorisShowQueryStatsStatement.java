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

package org.apache.shardingsphere.sql.parser.statement.doris.dal.show;

import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.FromTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.AllowNotUseDatabaseSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.DatabaseSelectRequiredSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TableSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.TablelessDataSourceBroadcastRouteSQLStatementAttribute;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;

import java.util.Optional;

/**
 * Show query stats statement for Doris.
 */
@Getter
public final class DorisShowQueryStatsStatement extends DALStatement {
    
    private final DatabaseSegment database;
    
    private final FromTableSegment fromTable;
    
    private final boolean all;
    
    private final boolean verbose;
    
    private SQLStatementAttributes attributes;
    
    public DorisShowQueryStatsStatement(final DatabaseType databaseType, final DatabaseSegment database, final FromTableSegment fromTable, final boolean all, final boolean verbose) {
        super(databaseType);
        this.database = database;
        this.fromTable = fromTable;
        this.all = all;
        this.verbose = verbose;
    }
    
    /**
     * Get database.
     *
     * @return database
     */
    public Optional<DatabaseSegment> getDatabase() {
        return Optional.ofNullable(database);
    }
    
    /**
     * Get from table.
     *
     * @return from table
     */
    public Optional<FromTableSegment> getFromTable() {
        return Optional.ofNullable(fromTable);
    }
    
    /**
     * Get table.
     *
     * @return table
     */
    public Optional<SimpleTableSegment> getTable() {
        return null == fromTable ? Optional.empty() : Optional.ofNullable(fromTable.getTable());
    }
    
    @Override
    public void buildAttributes() {
        String databaseName = null == database ? null : database.getIdentifier().getValue();
        SimpleTableSegment table = null == fromTable ? null : fromTable.getTable();
        attributes = new SQLStatementAttributes(new DatabaseSelectRequiredSQLStatementAttribute(), new TableSQLStatementAttribute(table),
                new TablelessDataSourceBroadcastRouteSQLStatementAttribute(), new AllowNotUseDatabaseSQLStatementAttribute(true, databaseName));
    }
}
