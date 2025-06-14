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

package org.apache.shardingsphere.infra.binder.mysql;

import org.apache.shardingsphere.infra.binder.context.provider.DialectTableAvailableSQLStatementContextWarpProvider;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.OptimizeTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.LoadDataStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.LoadXMLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLDescribeStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLFlushStatement;

import java.util.Arrays;
import java.util.Collection;

/**
 * Table available SQL statement context warp provider for MySQL.
 */
public final class MySQLSQLStatementContextWarpProvider implements DialectTableAvailableSQLStatementContextWarpProvider {
    
    private static final Collection<Class<? extends SQLStatement>> NEED_TO_WARP_TABLE_AVAILABLE_SQL_STATEMENT_CONTEXT_TYPES = Arrays.asList(
            ShowCreateTableStatement.class, MySQLFlushStatement.class, OptimizeTableStatement.class, MySQLDescribeStatement.class, LoadDataStatement.class, LoadXMLStatement.class);
    
    @Override
    public Collection<Class<? extends SQLStatement>> getNeedToWarpTableAvailableSQLStatementContextTypes() {
        return NEED_TO_WARP_TABLE_AVAILABLE_SQL_STATEMENT_CONTEXT_TYPES;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
