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

package org.apache.shardingsphere.transaction.base.seata.at;

import lombok.SneakyThrows;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.core.protocol.RegisterRMRequest;
import org.apache.seata.core.protocol.RegisterRMResponse;
import org.apache.seata.core.protocol.RegisterTMRequest;
import org.apache.seata.core.protocol.RegisterTMResponse;
import org.apache.seata.core.protocol.transaction.GlobalBeginRequest;
import org.apache.seata.core.protocol.transaction.GlobalBeginResponse;
import org.apache.seata.core.protocol.transaction.GlobalCommitRequest;
import org.apache.seata.core.protocol.transaction.GlobalCommitResponse;
import org.apache.seata.core.protocol.transaction.GlobalRollbackRequest;
import org.apache.seata.core.protocol.transaction.GlobalRollbackResponse;
import org.apache.seata.core.rpc.netty.RmNettyRemotingClient;
import org.apache.seata.core.rpc.netty.TmNettyRemotingClient;
import org.apache.seata.rm.datasource.ConnectionProxy;
import org.apache.seata.rm.datasource.DataSourceProxy;
import org.apache.seata.tm.api.GlobalTransactionContext;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.base.seata.at.fixture.MockSeataServer;
import org.apache.shardingsphere.transaction.base.seata.at.fixture.MockedMysqlDataSource;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class SeataATShardingSphereTransactionManagerTest {
    
    private static final MockSeataServer MOCK_SEATA_SERVER = new MockSeataServer();
    
    private static final String DATA_SOURCE_UNIQUE_NAME = "sharding_db.ds_0";
    
    private static ExecutorService executorService;
    
    private final SeataATShardingSphereTransactionManager seataTransactionManager = new SeataATShardingSphereTransactionManager();
    
    private final Queue<Object> requestQueue = MOCK_SEATA_SERVER.getMessageHandler().getRequestQueue();
    
    private final Queue<Object> responseQueue = MOCK_SEATA_SERVER.getMessageHandler().getResponseQueue();
    
    @BeforeAll
    static void before() {
        executorService = Executors.newSingleThreadExecutor();
        executorService.submit(MOCK_SEATA_SERVER::start);
        while (true) {
            if (MOCK_SEATA_SERVER.getInitialized().get()) {
                return;
            }
        }
    }
    
    @AfterAll
    static void after() {
        MOCK_SEATA_SERVER.shutdown();
        executorService.shutdown();
        Awaitility.await().atMost(1L, TimeUnit.MINUTES).until(() -> executorService.isTerminated());
    }
    
    @BeforeEach
    void setUp() {
        seataTransactionManager.init(Collections.singletonMap("sharding_db.ds_0", TypedSPILoader.getService(DatabaseType.class, "MySQL")),
                Collections.singletonMap(DATA_SOURCE_UNIQUE_NAME, new MockedMysqlDataSource()), "Seata");
    }
    
    @AfterEach
    void tearDown() {
        SeataXIDContext.remove();
        RootContext.unbind();
        SeataTransactionHolder.clear();
        seataTransactionManager.close();
        releaseRpcClient();
        requestQueue.clear();
        responseQueue.clear();
    }
    
    @Test
    void assertInit() {
        Map<String, DataSource> actual = getDataSourceMap();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(DATA_SOURCE_UNIQUE_NAME), isA(DataSourceProxy.class));
        assertThat(seataTransactionManager.getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    void assertGetConnection() throws SQLException {
        Connection actual = seataTransactionManager.getConnection("sharding_db", "ds_0");
        assertThat(actual, isA(ConnectionProxy.class));
    }
    
    @Test
    void assertBegin() {
        seataTransactionManager.begin();
        assertTrue(seataTransactionManager.isInTransaction());
        assertResult(GlobalBeginRequest.class, GlobalBeginResponse.class);
    }
    
    @Test
    void assertBeginTimeout() {
        seataTransactionManager.begin(30);
        assertTrue(seataTransactionManager.isInTransaction());
        assertResult(GlobalBeginRequest.class, GlobalBeginResponse.class);
    }
    
    @Test
    void assertCommit() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        setXID("testXID");
        seataTransactionManager.commit(false);
        assertResult(GlobalCommitRequest.class, GlobalCommitResponse.class);
    }
    
    @Test
    void assertCommitWithoutBegin() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        assertThrows(IllegalStateException.class, () -> seataTransactionManager.commit(false));
    }
    
    @Test
    void assertRollback() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        setXID("testXID");
        seataTransactionManager.rollback();
        assertResult(GlobalRollbackRequest.class, GlobalRollbackResponse.class);
    }
    
    @Test
    void assertRollbackWithoutBegin() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        assertThrows(IllegalStateException.class, seataTransactionManager::rollback);
    }
    
    private void assertResult(final Class<?> requestClass, final Class<?> responseClass) {
        assertTrue(requestQueue.stream().anyMatch(RegisterTMRequest.class::isInstance));
        assertTrue(requestQueue.stream().anyMatch(RegisterRMRequest.class::isInstance));
        assertTrue(requestQueue.stream().anyMatch(each -> requestClass.equals(each.getClass())));
        assertTrue(responseQueue.stream().anyMatch(RegisterTMResponse.class::isInstance));
        assertTrue(responseQueue.stream().anyMatch(RegisterRMResponse.class::isInstance));
        assertTrue(responseQueue.stream().anyMatch(each -> responseClass.equals(each.getClass())));
        while (!requestQueue.isEmpty()) {
            Object requestPackage = requestQueue.poll();
            Object responsePackage = responseQueue.poll();
            if (requestPackage instanceof RegisterTMRequest) {
                assertThat(responsePackage, isA(RegisterTMResponse.class));
            } else if (requestPackage instanceof RegisterRMRequest) {
                assertThat(responsePackage, isA(RegisterRMResponse.class));
            } else if (requestPackage instanceof GlobalBeginRequest) {
                assertThat(responsePackage, isA(GlobalBeginResponse.class));
            } else if (requestPackage instanceof GlobalCommitRequest) {
                assertThat(responsePackage, isA(GlobalCommitResponse.class));
            } else if (requestPackage instanceof GlobalRollbackRequest) {
                assertThat(responsePackage, isA(GlobalRollbackResponse.class));
            } else {
                fail("Request package type error");
            }
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Map<String, DataSource> getDataSourceMap() {
        return (Map<String, DataSource>) Plugins.getMemberAccessor().get(seataTransactionManager.getClass().getDeclaredField("dataSourceMap"), seataTransactionManager);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setXID(final String xid) {
        Plugins.getMemberAccessor().set(SeataTransactionHolder.get().getClass().getDeclaredField("xid"), SeataTransactionHolder.get(), xid);
        RootContext.bind(xid);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void releaseRpcClient() {
        Plugins.getMemberAccessor().set(TmNettyRemotingClient.getInstance().getClass().getDeclaredField("initialized"), TmNettyRemotingClient.getInstance(), new AtomicBoolean(false));
        Plugins.getMemberAccessor().set(TmNettyRemotingClient.getInstance().getClass().getDeclaredField("instance"), TmNettyRemotingClient.getInstance(), null);
        Plugins.getMemberAccessor().set(RmNettyRemotingClient.getInstance().getClass().getDeclaredField("initialized"), RmNettyRemotingClient.getInstance(), new AtomicBoolean(false));
        Plugins.getMemberAccessor().set(RmNettyRemotingClient.getInstance().getClass().getDeclaredField("instance"), RmNettyRemotingClient.getInstance(), null);
    }
}
