package org.apache.shardingsphere.proxy.backend.text.sctl.explain;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.kernel.context.impl.StandardSchemaContexts;
import org.apache.shardingsphere.kernel.context.runtime.RuntimeContext;
import org.apache.shardingsphere.kernel.context.schema.ShardingSphereSchema;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.rdl.parser.engine.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.StandardSQLParserEngine;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

public final class ShardingCTLExplainBackendHandlerTest {
    
    private ShardingCTLExplainBackendHandler handler;
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchema()).thenReturn("schema");
        handler = new ShardingCTLExplainBackendHandler("sctl:explain select 1", connection);
        Field schemaContexts = ProxySchemaContexts.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxySchemaContexts.getInstance(),
                new StandardSchemaContexts(getSchemaContextMap(), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    private Map<String, SchemaContext> getSchemaContextMap() {
        RuntimeContext runtimeContext = new RuntimeContext(null, null, new ShardingSphereSQLParserEngine(new StandardSQLParserEngine("MySQL")));
        ShardingSphereSchema schema = new ShardingSphereSchema(Collections.emptyList(),
                Collections.emptyList(), Collections.singletonMap("ds0", mock(DataSource.class)), null);
        return Collections.singletonMap("schema", new SchemaContext("schema", schema, runtimeContext));
    }
    
    @Test
    public void assertQueryData() {
        handler.execute();
        assertTrue(handler.next());
        assertThat(handler.getQueryData().getData().get(1), is("select 1"));
    }
}
