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

import io.seata.core.context.RootContext;
import io.seata.core.protocol.MergeResultMessage;
import io.seata.core.protocol.MergedWarpMessage;
import io.seata.core.protocol.RegisterRMRequest;
import io.seata.core.protocol.RegisterRMResponse;
import io.seata.core.protocol.RegisterTMRequest;
import io.seata.core.protocol.RegisterTMResponse;
import io.seata.core.rpc.netty.RmNettyRemotingClient;
import io.seata.core.rpc.netty.TmNettyRemotingClient;
import io.seata.rm.datasource.ConnectionProxy;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.tm.api.GlobalTransactionContext;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.base.seata.at.fixture.MockSeataServer;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SeataATShardingSphereTransactionManagerTest {
    
    private static final MockSeataServer MOCK_SEATA_SERVER = new MockSeataServer();
    
    private static final String DATA_SOURCE_UNIQUE_NAME = "sharding_db.foo_ds";
    
    private final SeataATShardingSphereTransactionManager seataTransactionManager = new SeataATShardingSphereTransactionManager();
    
    private final Queue<Object> requestQueue = MOCK_SEATA_SERVER.getMessageHandler().getRequestQueue();
    
    private final Queue<Object> responseQueue = MOCK_SEATA_SERVER.getMessageHandler().getResponseQueue();
    
    @BeforeClass
    public static void before() {
        Executors.newSingleThreadExecutor().submit(MOCK_SEATA_SERVER::start);
        while (true) {
            if (MOCK_SEATA_SERVER.getInitialized().get()) {
                return;
            }
        }
    }
    
    @AfterClass
    public static void after() {
        MOCK_SEATA_SERVER.shutdown();
    }
    
    @Before
    public void setUp() {
        seataTransactionManager.init(DatabaseTypeFactory.getInstance("MySQL"), Collections.singletonList(new ResourceDataSource(DATA_SOURCE_UNIQUE_NAME, new MockedDataSource())), "Seata");
    }
    
    @After
    public void tearDown() {
        ExecutorDataMap.getValue().clear();
        RootContext.unbind();
        SeataTransactionHolder.clear();
        seataTransactionManager.close();
        releaseRpcClient();
        requestQueue.clear();
        responseQueue.clear();
    }
    
    @Test
    public void assertInit() {
        Map<String, DataSource> actual = getDataSourceMap();
        assertThat(actual.size(), is(1));
        assertThat(actual.get(DATA_SOURCE_UNIQUE_NAME), instanceOf(DataSourceProxy.class));
        assertThat(seataTransactionManager.getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        Connection actual = seataTransactionManager.getConnection("sharding_db", "foo_ds");
        assertThat(actual, instanceOf(ConnectionProxy.class));
    }
    
    @Test
    public void assertBegin() {
        seataTransactionManager.begin();
        assertTrue(seataTransactionManager.isInTransaction());
        assertResult();
    }
    
    @Test
    public void assertBeginTimeout() {
        seataTransactionManager.begin(30);
        assertTrue(seataTransactionManager.isInTransaction());
        assertResult();
    }
    
    @Test
    public void assertCommit() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        setXID("testXID");
        seataTransactionManager.commit(false);
        assertResult();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertCommitWithoutBegin() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        seataTransactionManager.commit(false);
    }
    
    @Test
    public void assertRollback() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        setXID("testXID");
        seataTransactionManager.rollback();
        assertResult();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertRollbackWithoutBegin() {
        SeataTransactionHolder.set(GlobalTransactionContext.getCurrentOrCreate());
        seataTransactionManager.rollback();
    }
    
    private void assertResult() {
        int requestQueueSize = requestQueue.size();
        if (3 == requestQueueSize) {
            assertThat(requestQueue.poll(), instanceOf(RegisterRMRequest.class));
            assertThat(requestQueue.poll(), instanceOf(RegisterTMRequest.class));
            assertThat(requestQueue.poll(), instanceOf(MergedWarpMessage.class));
        }
        int responseQueueSize = responseQueue.size();
        if (3 == responseQueueSize) {
            assertThat(responseQueue.poll(), instanceOf(RegisterRMResponse.class));
            assertThat(responseQueue.poll(), instanceOf(RegisterTMResponse.class));
            assertThat(responseQueue.poll(), instanceOf(MergeResultMessage.class));
        }
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Map<String, DataSource> getDataSourceMap() {
        Field field = seataTransactionManager.getClass().getDeclaredField("dataSourceMap");
        field.setAccessible(true);
        return (Map<String, DataSource>) field.get(seataTransactionManager);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setXID(final String xid) {
        Field field = SeataTransactionHolder.get().getClass().getDeclaredField("xid");
        field.setAccessible(true);
        field.set(SeataTransactionHolder.get(), xid);
        RootContext.bind(xid);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void releaseRpcClient() {
        Field field = TmNettyRemotingClient.getInstance().getClass().getDeclaredField("initialized");
        field.setAccessible(true);
        field.set(TmNettyRemotingClient.getInstance(), new AtomicBoolean(false));
        field = TmNettyRemotingClient.getInstance().getClass().getDeclaredField("instance");
        field.setAccessible(true);
        field.set(TmNettyRemotingClient.getInstance(), null);
        field = RmNettyRemotingClient.getInstance().getClass().getDeclaredField("initialized");
        field.setAccessible(true);
        field.set(RmNettyRemotingClient.getInstance(), new AtomicBoolean(false));
        field = RmNettyRemotingClient.getInstance().getClass().getDeclaredField("instance");
        field.setAccessible(true);
        field.set(RmNettyRemotingClient.getInstance(), null);
    }
}
