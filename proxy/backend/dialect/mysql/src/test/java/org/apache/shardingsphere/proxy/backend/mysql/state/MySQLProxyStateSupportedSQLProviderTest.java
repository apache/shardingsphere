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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.state.DialectProxyStateSupportedSQLProvider;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLFlushStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.privilege.MySQLShowCreateUserStatement;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLProxyStateSupportedSQLProviderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final DialectProxyStateSupportedSQLProvider provider = TypedSPILoader.getService(DialectProxyStateSupportedSQLProvider.class, databaseType);
    
    @Test
    void assertGetUnsupportedSQLStatementTypesOnReadyState() {
        assertThat(provider.getUnsupportedSQLStatementTypesOnReadyState(), hasItems(MySQLFlushStatement.class, MySQLShowCreateUserStatement.class));
    }
    
    @Test
    void assertGetSupportedSQLStatementTypesOnUnavailableState() {
        assertThat(provider.getSupportedSQLStatementTypesOnUnavailableState(), hasItems(MySQLShowDatabasesStatement.class, MySQLUseStatement.class));
    }
}
