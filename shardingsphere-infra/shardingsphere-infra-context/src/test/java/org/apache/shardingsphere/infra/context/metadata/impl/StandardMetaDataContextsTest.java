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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StandardMetaDataContextsTest {

    @Mock
    private OptimizeContextFactory optimizeContextFactory;
    
    @Test
    public void assertGetDefaultMetaData() {
        ShardingSphereMetaData metaData = getShardingSphereMetaData();
        StandardMetaDataContexts standardMetaDataContexts = new StandardMetaDataContexts(Collections.singletonMap(DefaultSchema.LOGIC_NAME, metaData), 
                mock(ShardingSphereRuleMetaData.class), null, new ConfigurationProperties(new Properties()), optimizeContextFactory);
        assertThat(standardMetaDataContexts.getDefaultMetaData(), is(metaData));
    }
    
    @Test
    public void assertClose() {
        ExecutorEngine executorEngine = mock(ExecutorEngine.class);
        ShardingSphereMetaData metaData = getShardingSphereMetaData();
        StandardMetaDataContexts standardMetaDataContexts = new StandardMetaDataContexts(Collections.singletonMap("logic_db", metaData), 
                mock(ShardingSphereRuleMetaData.class), executorEngine, new ConfigurationProperties(new Properties()), optimizeContextFactory);
        standardMetaDataContexts.close();
        verify(executorEngine).close();
    }
    
    private ShardingSphereMetaData getShardingSphereMetaData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList());
        ShardingSphereResource resource = new ShardingSphereResource(Collections.emptyMap(), null, null, new MySQLDatabaseType());
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        Map<String, TableMetaData> tables = new HashMap<>();
        when(metaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(metaData.getResource()).thenReturn(resource);
        when(metaData.getSchema()).thenReturn(schema);
        when(schema.getTables()).thenReturn(tables);
        return metaData;
    }
}
