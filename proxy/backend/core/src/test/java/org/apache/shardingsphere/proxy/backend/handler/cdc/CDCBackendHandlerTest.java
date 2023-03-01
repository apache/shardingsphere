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

package org.apache.shardingsphere.proxy.backend.handler.cdc;

import io.netty.channel.Channel;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCConnectionContext;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.CDCRequest.Type;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.request.StreamDataRequestBody.SchemaTable;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse;
import org.apache.shardingsphere.data.pipeline.cdc.protocol.response.CDCResponse.Status;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.reflection.ReflectionUtil;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(PipelineContext.class)
public final class CDCBackendHandlerTest {
    
    private final CDCBackendHandler handler = new CDCBackendHandler();
    
    @BeforeEach
    public void setUp() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        ContextManager contextManager = new ContextManager(metaDataContexts, mock(InstanceContext.class));
        when(PipelineContext.getContextManager()).thenReturn(contextManager);
    }
    
    private static Map<String, ShardingSphereDatabase> getDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        Set<ShardingSphereRule> shardingRule = Collections.singleton(mock(ShardingRule.class));
        when(database.getRuleMetaData().getRules()).thenReturn(shardingRule);
        return Collections.singletonMap("foo_db", database);
    }
    
    @Test
    public void assertStreamDataRequestFailed() {
        CDCRequest request = CDCRequest.newBuilder().setRequestId("1").setType(Type.STREAM_DATA).setStreamDataRequestBody(StreamDataRequestBody.newBuilder().setDatabase("none")).build();
        CDCResponse actualResponse = handler.streamData(request.getRequestId(), request.getStreamDataRequestBody(), mock(CDCConnectionContext.class), mock(Channel.class));
        assertThat(actualResponse.getStatus(), is(Status.FAILED));
    }
    
    @Test
    public void assertGetSchemaTableMapWithSchema() throws NoSuchMethodException {
        Map<String, ShardingSphereSchema> schemas = new HashMap<>();
        schemas.put("test", mockSchema());
        schemas.put("public", mockSchema());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", new OpenGaussDatabaseType(), null, null, schemas);
        List<SchemaTable> schemaTables = Arrays.asList(SchemaTable.newBuilder().setSchema("public").setTable("t_order").build(),
                SchemaTable.newBuilder().setSchema("test").setTable("*").build());
        Map<String, Collection<String>> expected = new HashMap<>();
        expected.put("test", new HashSet<>(Arrays.asList("t_order", "t_order_item")));
        expected.put("public", new HashSet<>(Collections.singletonList("t_order")));
        Map<String, String> actual = getSchemaTableMapWithSchemaResult(database, schemaTables);
        assertThat(actual, is(expected));
        schemaTables = Collections.singletonList(SchemaTable.newBuilder().setTable("t_order").build());
        actual = getSchemaTableMapWithSchemaResult(database, schemaTables);
        expected = Collections.singletonMap("", Collections.singleton("t_order"));
        assertThat(actual, is(expected));
        schemaTables = Collections.singletonList(SchemaTable.newBuilder().setSchema("*").setTable("t_order").build());
        actual = getSchemaTableMapWithSchemaResult(database, schemaTables);
        expected = new HashMap<>();
        expected.put("test", new HashSet<>(Collections.singletonList("t_order")));
        expected.put("public", new HashSet<>(Collections.singletonList("t_order")));
        assertThat(actual, is(expected));
    }
    
    private ShardingSphereSchema mockSchema() {
        Map<String, ShardingSphereTable> tables = new HashMap<>();
        tables.put("t_order", mock(ShardingSphereTable.class));
        tables.put("t_order_item", mock(ShardingSphereTable.class));
        return new ShardingSphereSchema(tables, Collections.emptyMap());
    }
    
    private Map<String, String> getSchemaTableMapWithSchemaResult(final ShardingSphereDatabase database, final List<SchemaTable> schemaTables) throws NoSuchMethodException {
        return ReflectionUtil.invokeMethod(handler.getClass().getDeclaredMethod("getSchemaTableMapWithSchema", ShardingSphereDatabase.class, List.class),
                handler, database, schemaTables);
    }
    
    @Test
    public void assertGetSchemaTableMapWithoutSchema() throws NoSuchMethodException {
        Map<String, ShardingSphereSchema> schemas = new HashMap<>();
        schemas.put("foo_db", mockSchema());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", new MySQLDatabaseType(), null, null, schemas);
        List<SchemaTable> schemaTables = Collections.singletonList(SchemaTable.newBuilder().setTable("*").build());
        Collection<String> actualWildcardTable = getSchemaTableMapWithoutSchemaResult(database, schemaTables);
        Set<String> expectedWildcardTable = new HashSet<>(Arrays.asList("t_order", "t_order_item"));
        assertThat(actualWildcardTable, is(expectedWildcardTable));
        schemaTables = Collections.singletonList(SchemaTable.newBuilder().setTable("t_order").build());
        Collection<String> actualSingleTable = getSchemaTableMapWithoutSchemaResult(database, schemaTables);
        Set<String> expectedSingleTable = new HashSet<>(Collections.singletonList("t_order"));
        assertThat(actualSingleTable, is(expectedSingleTable));
    }
    
    private Collection<String> getSchemaTableMapWithoutSchemaResult(final ShardingSphereDatabase database, final List<SchemaTable> schemaTables) throws NoSuchMethodException {
        return ReflectionUtil.invokeMethod(handler.getClass().getDeclaredMethod("getTableNamesWithoutSchema", ShardingSphereDatabase.class, List.class),
                handler, database, schemaTables);
    }
}
