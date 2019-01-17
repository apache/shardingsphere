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

package org.apache.shardingsphere.shardingproxy.backend;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.JDBCBackendHandler;
import org.apache.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.sctl.ShardingCTLSetBackendHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class ComQueryBackendHandlerFactoryTest {
    
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
        String sql = "grant select on test_db.* to root@'%'";
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
