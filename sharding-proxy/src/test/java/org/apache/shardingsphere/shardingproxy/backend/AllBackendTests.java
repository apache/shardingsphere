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

import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnectionTest;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendTransactionManagerTest;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.ConnectionStateHandlerTest;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCBackendDataSourceTest;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.datasource.JDBCXABackendDataSourceFactoryTest;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.recognizer.AllRecognizerTests;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.BroadcastBackendHandlerTest;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.ShowDatabasesBackendHandlerTest;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UnicastBackendHandlerTest;
import org.apache.shardingsphere.shardingproxy.backend.text.admin.UseDatabaseBackendHandlerTest;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.set.ShardingCTLSetBackendHandlerTest;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.show.ShardingCTLShowBackendHandlerTest;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.SkipBackendHandlerTest;
import org.apache.shardingsphere.shardingproxy.backend.text.transaction.TransactionBackendHandlerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        TextProtocolBackendHandlerFactoryTest.class, 
        BroadcastBackendHandlerTest.class, 
        ShowDatabasesBackendHandlerTest.class, 
        SkipBackendHandlerTest.class, 
        TransactionBackendHandlerTest.class, 
        UnicastBackendHandlerTest.class, 
        UseDatabaseBackendHandlerTest.class, 
        ShardingCTLSetBackendHandlerTest.class,
        ShardingCTLShowBackendHandlerTest.class,
        JDBCXABackendDataSourceFactoryTest.class,
        JDBCBackendDataSourceTest.class,
        BackendConnectionTest.class,
        BackendTransactionManagerTest.class,
        ConnectionStateHandlerTest.class,
        AllRecognizerTests.class
})
public final class AllBackendTests {
}
