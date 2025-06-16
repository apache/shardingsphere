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

package org.apache.shardingsphere.infra.binder.postgresql;

import org.apache.shardingsphere.infra.binder.context.provider.DialectCommonSQLStatementContextWarpProvider;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.CopyStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Common SQL Statement context warp provider for PostgreSQL.
 */
public final class PostgreSQLSQLStatementContextWarpProvider implements DialectCommonSQLStatementContextWarpProvider {
    
    private static final Collection<Class<? extends SQLStatement>> NEED_TO_WARP_SQL_STATEMENT_TYPES = Collections.singleton(CopyStatement.class);
    
    @Override
    public Collection<Class<? extends SQLStatement>> getNeedToWarpSQLStatementTypes() {
        return NEED_TO_WARP_SQL_STATEMENT_TYPES;
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
}
