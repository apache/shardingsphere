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

package org.apache.shardingsphere.proxy.backend.context;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.transaction.context.impl.StandardTransactionContexts;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class BackendExecutorContextTest {
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = new StandardMetaDataContexts(Collections.emptyMap(), mock(ShardingSphereRuleMetaData.class), 
                mock(ExecutorEngine.class), new ShardingSphereUsers(Collections.singleton(new ShardingSphereUser("root", "root", ""))), new ConfigurationProperties(new Properties()));
        ProxyContext.getInstance().init(metaDataContexts, new StandardTransactionContexts());
    }
    
    @Test
    public void assertGetInstance() {
        assertThat(BackendExecutorContext.getInstance().getExecutorEngine(), is(BackendExecutorContext.getInstance().getExecutorEngine()));
    }
}
