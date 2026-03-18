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

package org.apache.shardingsphere.proxy.backend.mysql.state;

import org.apache.shardingsphere.proxy.backend.state.DialectProxyStateSupportedSQLProvider;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.privilege.MySQLShowCreateUserStatement;

import java.util.Arrays;
import java.util.Collection;

/**
 * Proxy state supported SQL provider for MySQL.
 */
public final class MySQLProxyStateSupportedSQLProvider implements DialectProxyStateSupportedSQLProvider {
    
    private static final Collection<Class<? extends SQLStatement>> UNSUPPORTED_SQL_STATEMENT_TYPES_ON_READY_STATE = Arrays.asList(MySQLFlushStatement.class, MySQLShowCreateUserStatement.class);
    
    private static final Collection<Class<? extends SQLStatement>> SUPPORTED_SQL_STATEMENT_TYPES_ON_UNAVAILABLE_STATE = Arrays.asList(MySQLShowDatabasesStatement.class, MySQLUseStatement.class);
    
    @Override
    public Collection<Class<? extends SQLStatement>> getUnsupportedSQLStatementTypesOnReadyState() {
        return UNSUPPORTED_SQL_STATEMENT_TYPES_ON_READY_STATE;
    }
    
    @Override
    public Collection<Class<? extends SQLStatement>> getSupportedSQLStatementTypesOnUnavailableState() {
        return SUPPORTED_SQL_STATEMENT_TYPES_ON_UNAVAILABLE_STATE;
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
