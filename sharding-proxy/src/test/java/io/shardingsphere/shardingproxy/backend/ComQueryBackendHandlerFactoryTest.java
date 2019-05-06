/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.shardingproxy.backend.jdbc.JDBCBackendHandler;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.backend.sctl.ShardingCTLSetBackendHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ComQueryBackendHandlerFactoryTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Test
    public void assertCreateTransactionBackendHandler() {
        String sql = "BEGIN";
        BackendHandler actual = ComQueryBackendHandlerFactory.createBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        assertThat(actual, instanceOf(TransactionBackendHandler.class));
    }
    
    @Test
    public void assertCreateShardingCTLBackendHandler() {
        String sql = "sctl:set transaction_type=XA";
        BackendHandler actual = ComQueryBackendHandlerFactory.createBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        assertThat(actual, instanceOf(ShardingCTLSetBackendHandler.class));
    }
    
    @Test
    public void assertCreateSkipBackendHandler() {
        String sql = "SET AUTOCOMMIT=1";
        BackendHandler actual = ComQueryBackendHandlerFactory.createBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        assertThat(actual, instanceOf(SkipBackendHandler.class));
    }
    
    @Test
    public void assertCreateSchemaBroadcastBackendHandler() {
        String sql = "grant select on testdb.* to root@'%'";
        BackendHandler actual = ComQueryBackendHandlerFactory.createBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        assertThat(actual, instanceOf(SchemaBroadcastBackendHandler.class));
        sql = "set @num=1";
        actual = ComQueryBackendHandlerFactory.createBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        assertThat(actual, instanceOf(SchemaBroadcastBackendHandler.class));
    }
    
    @Test
    public void assertCreateUseSchemaBackendHandler() {
        String sql = "use sharding_db";
        BackendHandler actual = ComQueryBackendHandlerFactory.createBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        assertThat(actual, instanceOf(UseSchemaBackendHandler.class));
    }
    
    @Test
    public void assertCreateShowDatabasesBackendHandler() {
        String sql = "show databases;";
        BackendHandler actual = ComQueryBackendHandlerFactory.createBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        assertThat(actual, instanceOf(ShowDatabasesBackendHandler.class));
    }
    
    @Test
    public void assertCrateDefaultBackendHandler() {
        String sql = "select * from t_order limit 1";
        BackendHandler actual = ComQueryBackendHandlerFactory.createBackendHandler(1, sql, backendConnection, DatabaseType.MySQL);
        assertThat(actual, instanceOf(JDBCBackendHandler.class));
    }
}
