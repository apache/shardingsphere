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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.util.Optional;

/**
 * Cluster statement for PostgreSQL.
 */
public final class PostgreSQLClusterStatement extends DDLStatement {
    
    private final SimpleTableSegment simpleTable;
    
    private final IndexSegment index;
    
    public PostgreSQLClusterStatement(final DatabaseType databaseType, final SimpleTableSegment simpleTable, final IndexSegment index) {
        super(databaseType);
        this.simpleTable = simpleTable;
        this.index = index;
    }
    
    /**
     * Get simple table.
     *
     * @return simple table
     */
    public Optional<SimpleTableSegment> getSimpleTable() {
        return Optional.ofNullable(simpleTable);
    }
    
    /**
     * Get index.
     *
     * @return index segment
     */
    public Optional<IndexSegment> getIndex() {
        return Optional.ofNullable(index);
    }
}
