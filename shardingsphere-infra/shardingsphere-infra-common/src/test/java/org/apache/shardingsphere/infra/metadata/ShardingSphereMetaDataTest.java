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

package org.apache.shardingsphere.infra.metadata;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.schema.fixture.datasource.CloseableDataSource;
import org.apache.shardingsphere.infra.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSphereMetaDataTest {
    
    @Mock
    private CloseableDataSource dataSource0;
    
    @Mock
    private CloseableDataSource dataSource1;
    
    @Mock
    private DataSource dataSource2;
    
    @Test
    public void assertIsComplete() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("name", Collections.singleton(mock(RuleConfiguration.class)),
                Collections.singleton(mock(ShardingSphereRule.class)), Collections.singletonMap("ds", mock(DataSource.class)), mock(ShardingSphereSchema.class));
        assertTrue(metaData.isComplete());
    }
    
    @Test
    public void assertIsNotCompleteWithoutRule() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("name", Collections.emptyList(),
                Collections.emptyList(), Collections.singletonMap("ds", mock(DataSource.class)), mock(ShardingSphereSchema.class));
        assertFalse(metaData.isComplete());
    }
    
    @Test
    public void assertIsNotCompleteWithoutDataSource() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("name", Collections.singleton(mock(RuleConfiguration.class)),
                Collections.singleton(mock(ShardingSphereRule.class)), Collections.emptyMap(), mock(ShardingSphereSchema.class));
        assertFalse(metaData.isComplete());
    }
    
    @Test
    public void assertCloseDataSources() throws SQLException, IOException {
        new ShardingSphereMetaData("name", Collections.emptyList(), Collections.emptyList(), createDataSources(), mock(ShardingSphereSchema.class)).closeDataSources(Arrays.asList("ds_0", "ds_2"));
        verify(dataSource0).close();
        verify(dataSource1, times(0)).close();
    }
    
    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> result = new HashMap<>(3, 1);
        result.put("ds_0", dataSource0);
        result.put("ds_1", dataSource1);
        result.put("ds_2", dataSource2);
        return result;
    }
}
