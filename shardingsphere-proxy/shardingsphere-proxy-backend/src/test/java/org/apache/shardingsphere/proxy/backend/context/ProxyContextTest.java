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

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.schema.SchemaContext;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.context.schema.runtime.RuntimeContext;
import org.apache.shardingsphere.infra.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.jdbc.test.MockedDataSource;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProxyContextTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    @Test
    public void assertGetDataSourceSample() throws NoSuchFieldException, IllegalAccessException {
        Map<String, DataSource> mockDataSourceMap = new HashMap<>(2, 1);
        mockDataSourceMap.put("ds_1", new MockedDataSource());
        mockDataSourceMap.put("ds_2", new MockedDataSource());
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(),
                new StandardSchemaContexts(getSchemaContextMap(mockDataSourceMap), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        Optional<DataSource> actual = ProxyContext.getInstance().getDataSourceSample();
        assertThat(actual, is(Optional.of(mockDataSourceMap.get("ds_1"))));
    }
    
    @Test
    public void assertInit() {
        SchemaContexts schemaContexts = mock(SchemaContexts.class);
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        ProxyContext proxyContext = ProxyContext.getInstance();
        proxyContext.init(schemaContexts, transactionContexts);
        assertEquals(schemaContexts, proxyContext.getSchemaContexts());
        assertEquals(transactionContexts, proxyContext.getTransactionContexts());
    }
    
    @Test
    public void assertSchemaExists() throws NoSuchFieldException, IllegalAccessException {
        SchemaContext schemaContext = mock(SchemaContext.class);
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(shardingSphereSchema.getDataSources()).thenReturn(Collections.emptyMap());
        when(schemaContext.getName()).thenReturn("schema");
        when(schemaContext.getSchema()).thenReturn(shardingSphereSchema);
        when(schemaContext.getRuntimeContext()).thenReturn(runtimeContext);
        Map<String, SchemaContext> schemaContextsMap = Collections.singletonMap("schema", schemaContext);
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(), new StandardSchemaContexts(schemaContextsMap, new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        boolean exists1 = ProxyContext.getInstance().schemaExists("schema");
        assertThat(true, is(exists1));
        boolean exists2 = ProxyContext.getInstance().schemaExists("schema_2");
        assertThat(false, is(exists2));
    }
    
    @Test
    public void assertGetSchema() throws NoSuchFieldException, IllegalAccessException {
        SchemaContext schemaContext = mock(SchemaContext.class);
        ShardingSphereSchema shardingSphereSchema = mock(ShardingSphereSchema.class);
        RuntimeContext runtimeContext = mock(RuntimeContext.class);
        when(shardingSphereSchema.getDataSources()).thenReturn(Collections.emptyMap());
        when(schemaContext.getName()).thenReturn("schema");
        when(schemaContext.getSchema()).thenReturn(shardingSphereSchema);
        when(schemaContext.getRuntimeContext()).thenReturn(runtimeContext);
        Map<String, SchemaContext> schemaContextsMap = Collections.singletonMap("schema", schemaContext);
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(), new StandardSchemaContexts(schemaContextsMap, new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        assertThat(null, is(ProxyContext.getInstance().getSchema(null)));
        assertThat(null, is(ProxyContext.getInstance().getSchema("")));
        assertThat(null, is(ProxyContext.getInstance().getSchema("schema1")));
        assertThat(schemaContext, is(ProxyContext.getInstance().getSchema("schema")));
    }
    
    @Test
    public void assertGetAllSchemaNames() throws NoSuchFieldException, IllegalAccessException {
        Map<String, SchemaContext> schemaContextsMap = createSchemaContextMap();
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(), new StandardSchemaContexts(schemaContextsMap, new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        assertThat(new LinkedHashSet<>(ProxyContext.getInstance().getAllSchemaNames()), is(schemaContextsMap.keySet()));
    }
    
    private Map<String, SchemaContext> createSchemaContextMap() {
        Map<String, SchemaContext> result = new LinkedHashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            result.put(String.format(SCHEMA_PATTERN, i), mock(SchemaContext.class));
        }
        return result;
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
