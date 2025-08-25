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

package org.apache.shardingsphere.infra.exception.external.sql.identifier;

import lombok.RequiredArgsConstructor;

/**
 * SQL Exception identifier.
 */
@RequiredArgsConstructor
public final class SQLExceptionIdentifier {
    
    private final String database;
    
    private final String table;
    
    private final String column;
    
    public SQLExceptionIdentifier(final String database) {
        this(database, null, null);
    }
    
    public SQLExceptionIdentifier(final String database, final String table) {
        this(database, table, null);
    }
    
    @Override
    public String toString() {
        if (null != table && null != column) {
            return String.format("database.table.column: '%s'.'%s'.'%s'", database, table, column);
        }
        if (null != table) {
            return String.format("database.table: '%s'.'%s'", database, table);
        }
        return String.format("database: '%s'", database);
    }
}
