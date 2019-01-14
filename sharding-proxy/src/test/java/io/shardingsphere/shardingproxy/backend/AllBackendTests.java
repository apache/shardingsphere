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

import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnectionTest;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendTransactionManagerTest;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.ConnectionStateHandlerTest;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCBackendDataSourceTest;
import io.shardingsphere.shardingproxy.backend.jdbc.datasource.JDBCXABackendDataSourceFactoryTest;
import io.shardingsphere.shardingproxy.backend.sctl.ShardingCTLSetBackendHandlerTest;
import io.shardingsphere.shardingproxy.backend.sctl.ShardingCTLShowBackendHandlerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ComQueryBackendHandlerFactoryTest.class, 
        SchemaBroadcastBackendHandlerTest.class, 
        ShowDatabasesBackendHandlerTest.class, 
        SkipBackendHandlerTest.class, 
        TransactionBackendHandlerTest.class, 
        UnicastBackendHandlerTest.class, 
        UseStatementBackendHandlerTest.class, 
        ShardingCTLSetBackendHandlerTest.class,
        ShardingCTLShowBackendHandlerTest.class,
        JDBCXABackendDataSourceFactoryTest.class,
        JDBCBackendDataSourceTest.class,
        BackendConnectionTest.class,
        BackendTransactionManagerTest.class,
        ConnectionStateHandlerTest.class
})
public final class AllBackendTests {
}
