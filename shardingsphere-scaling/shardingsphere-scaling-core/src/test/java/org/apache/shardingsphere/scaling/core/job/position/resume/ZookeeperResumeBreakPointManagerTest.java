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

package org.apache.shardingsphere.scaling.core.job.position.resume;

import lombok.SneakyThrows;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.api.transaction.CuratorMultiTransaction;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.api.transaction.TransactionCheckBuilder;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.api.transaction.TransactionSetDataBuilder;
import org.apache.curator.framework.imps.ExtractingCuratorOp;
import org.apache.shardingsphere.orchestration.repository.zookeeper.CuratorZookeeperRepository;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ZookeeperResumeBreakPointManagerTest {
    
    @Mock
    private CuratorFramework client;
    
    @Mock
    private GetDataBuilder getDataBuilder;
    
    @Mock
    private CuratorMultiTransaction curatorMultiTransaction;
    
    @Mock
    private ExistsBuilder existsBuilder;
    
    @Mock
    private TransactionOp transactionOp;
    
    @Mock
    private TransactionCheckBuilder<CuratorOp> transactionCheckBuilder;
    
    @Mock
    private TransactionSetDataBuilder<CuratorOp> transactionSetDataBuilder;
    
    private ZookeeperResumeBreakPointManager zookeeperResumeBreakPointManager;
    
    @Before
    @SneakyThrows
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
        CuratorZookeeperRepository curatorZookeeperRepository = ReflectionUtil.getStaticFieldValueFromClass(ZookeeperResumeBreakPointManager.class, "CURATOR_ZOOKEEPER_REPOSITORY");
        ReflectionUtil.setFieldValue(curatorZookeeperRepository, "client", client);
        when(client.getData()).thenReturn(getDataBuilder);
        when(getDataBuilder.forPath("/base/inventory")).thenReturn("{\"unfinished\":{},\"finished\":[]}".getBytes());
        when(getDataBuilder.forPath("/base/incremental")).thenReturn("{\"ds0\":{},\"ds1\":{}}".getBytes());
        zookeeperResumeBreakPointManager = new ZookeeperResumeBreakPointManager("H2", "/base");
    }
    
    @Test
    @SneakyThrows
    public void assertPersistPosition() {
        CuratorOp curatorOp = new ExtractingCuratorOp();
        when(client.checkExists()).thenReturn(existsBuilder);
        when(existsBuilder.forPath("/base/inventory")).thenReturn(new Stat());
        when(existsBuilder.forPath("/base/incremental")).thenReturn(new Stat());
        when(client.transactionOp()).thenReturn(transactionOp);
        when(transactionOp.check()).thenReturn(transactionCheckBuilder);
        when(transactionCheckBuilder.forPath(anyString())).thenReturn(curatorOp);
        when(transactionOp.setData()).thenReturn(transactionSetDataBuilder);
        when(transactionSetDataBuilder.forPath(anyString(), any(byte[].class))).thenReturn(curatorOp);
        when(client.transaction()).thenReturn(curatorMultiTransaction);
        ReflectionUtil.invokeMethod(zookeeperResumeBreakPointManager, "persistPosition");
        verify(curatorMultiTransaction, times(2)).forOperations(curatorOp, curatorOp);
    }
    
    @After
    public void tearDown() {
        zookeeperResumeBreakPointManager.close();
    }
}
