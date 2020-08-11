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

package org.apache.shardingsphere.proxy.backend.schema;

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.StandardSchemaContexts;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.MockDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProxySchemaContextsTest {
    
    @Test
    public void assertGetDataSourceSample() throws NoSuchFieldException, IllegalAccessException {
        Map<String, DataSource> mockDataSourceMap = new HashMap<>(2, 1);
        mockDataSourceMap.put("ds_1", new MockDataSource());
        mockDataSourceMap.put("ds_2", new MockDataSource());
        Field schemaContexts = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxySchemaContexts.getInstance(),
                new StandardSchemaContexts(getSchemaContextMap(mockDataSourceMap), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        Optional<DataSource> actual = ProxySchemaContexts.getInstance().getDataSourceSample();
        assertThat(actual, is(Optional.of(mockDataSourceMap.get("ds_1"))));
    }
    
    private Map<String, SchemaContext> getSchemaContextMap(final Map<String, DataSource> mockDataSourceMap) {
        SchemaContext schemaContext = mock(SchemaContext.class);
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(shardingSphereSchema.getDataSources()).thenReturn(mockDataSourceMap);
        when(schemaContext.getName()).thenReturn("schema");
        when(schemaContext.getSchema()).thenReturn(shardingSphereSchema);
        when(schemaContext.getRuntimeContext()).thenReturn(runtimeContext);
        return Collections.singletonMap("schema", schemaContext);
    }
}
