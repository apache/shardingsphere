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

package org.apache.shardingsphere.kernel.context.schema;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.kernel.context.schema.fixture.ClosableDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSphereSchemaTest {
    
    @Mock
    private ClosableDataSource dataSource0;
    
    @Mock
    private ClosableDataSource dataSource1;
    
    @Test
    public void assertCloseDataSources() throws Exception {
        new ShardingSphereSchema(Collections.emptyList(), Collections.emptyList(), createDataSources(), mock(ShardingSphereMetaData.class)).closeDataSources(Collections.singleton("ds_0"));
        verify(dataSource0).close();
        verify(dataSource1, times(0)).close();
    }
    
    private Map<String, DataSource> createDataSources() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", dataSource0);
        result.put("ds_1", dataSource1);
        return result;
    }
}
