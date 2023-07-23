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

package org.apache.shardingsphere.proxy.backend.distsql;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;

/**
 * SQL statement context for DistSQL.
 */
@RequiredArgsConstructor
@Getter
public final class DistSQLStatementContext implements SQLStatementContext {
    
    private final DistSQLStatement sqlStatement;
    
    @Override
    public TablesContext getTablesContext() {
        throw new UnsupportedOperationException("Cannot get tables context of DistSQLStatementContext");
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        throw new UnsupportedOperationException("Cannot get database type of DistSQLStatementContext");
    }
}
