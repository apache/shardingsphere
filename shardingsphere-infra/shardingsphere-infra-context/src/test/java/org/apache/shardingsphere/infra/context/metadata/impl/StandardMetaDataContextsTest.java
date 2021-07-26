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

package org.apache.shardingsphere.infra.context.metadata.impl;

import org.apache.shardingsphere.infra.config.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class StandardMetaDataContextsTest {
    
    @Mock
    private OptimizeContextFactory optimizeContextFactory;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereMetaData metaData;
    
    @Test
    public void assertGetDefaultMetaData() {
        StandardMetaDataContexts standardMetaDataContexts = new StandardMetaDataContexts(mock(DistMetaDataPersistService.class), Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData), 
                mock(ShardingSphereRuleMetaData.class), null, new ConfigurationProperties(new Properties()), optimizeContextFactory);
        assertThat(standardMetaDataContexts.getDefaultMetaData(), is(metaData));
    }
    
    @Test
    public void assertClose() {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        StandardMetaDataContexts standardMetaDataContexts = new StandardMetaDataContexts(mock(DistMetaDataPersistService.class), Collections.singletonMap("logic_db", metaData), 
                mock(ShardingSphereRuleMetaData.class), executorEngine, new ConfigurationProperties(new Properties()), optimizeContextFactory);
        standardMetaDataContexts.close();
        verify(executorEngine).close();
    }
}
