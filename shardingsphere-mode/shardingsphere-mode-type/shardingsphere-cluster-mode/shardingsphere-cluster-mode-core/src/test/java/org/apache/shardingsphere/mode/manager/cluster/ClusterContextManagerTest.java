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

package org.apache.shardingsphere.mode.manager.cluster;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.persist.PersistService;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ClusterContextManagerTest {
    
    private final ConfigurationProperties props = new ConfigurationProperties(new Properties());
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PersistService persistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RegistryCenter registryCenter;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    private ClusterContextManager clusterContextManager;
    
    @Mock
    private ShardingSphereRuleMetaData globalRuleMetaData;
    
    @Before
    public void setUp() {
        clusterContextManager = new ClusterContextManager(persistService, registryCenter);
        clusterContextManager.init(
                new MetaDataContexts(mock(PersistService.class), createMetaDataMap(), globalRuleMetaData, mock(ExecutorEngine.class), props, mockOptimizeContextFactory()), 
                mock(TransactionContexts.class, RETURNS_DEEP_STUBS));
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        when(metaData.getSchema()).thenReturn(mock(ShardingSphereSchema.class));
        when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("schema", metaData);
    }
    
    private OptimizeContextFactory mockOptimizeContextFactory() {
        OptimizeContextFactory result = mock(OptimizeContextFactory.class);
        return result;
    }
    
    @Test
    public void assertGetMetaData() {
        assertThat(clusterContextManager.getMetaDataContexts().getMetaData("schema"), is(metaData));
    }
    
    @Test
    public void assertGetDefaultMetaData() {
        assertNull(clusterContextManager.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME));
    }
    
    @Test
    public void assertGetProps() {
        assertThat(clusterContextManager.getMetaDataContexts().getProps(), is(props));
    }
}
