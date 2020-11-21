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

package org.apache.shardingsphere.tracing.opentracing.hook;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpan.Continuation;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import org.apache.shardingsphere.tracing.opentracing.constant.ShardingTags;
import org.apache.shardingsphere.infra.executor.sql.hook.SPISQLExecutionHook;
import org.apache.shardingsphere.infra.executor.sql.hook.SQLExecutionHook;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutorDataMap;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class OpenTracingSQLExecutionHookTest extends BaseOpenTracingHookTest {
    
    private final SQLExecutionHook sqlExecutionHook = new SPISQLExecutionHook();
    
    private ActiveSpan activeSpan;
    
    @BeforeClass
    public static void registerSPI() {
        ShardingSphereServiceLoader.register(SQLExecutionHook.class);
    }
    
    @Before
    public void setUp() {
        activeSpan = mockActiveSpan();
    }
    
    private ActiveSpan mockActiveSpan() {
        Continuation continuation = mock(Continuation.class);
        ActiveSpan result = mock(ActiveSpan.class);
        when(continuation.activate()).thenReturn(result);
        ExecutorDataMap.getValue().put(OpenTracingRootInvokeHook.ACTIVE_SPAN_CONTINUATION, continuation);
        return result;
    }
    
    @After
    public void tearDown() {
        ExecutorDataMap.getValue().remove(OpenTracingRootInvokeHook.ACTIVE_SPAN_CONTINUATION);
    }
    
    @Test
    public void assertExecuteSuccessForTrunkThread() {
        DataSourceMetaData dataSourceMetaData = mock(DataSourceMetaData.class);
        when(dataSourceMetaData.getHostName()).thenReturn("localhost");
        when(dataSourceMetaData.getPort()).thenReturn(8888);
        sqlExecutionHook.start("success_ds", "SELECT * FROM success_tbl;", Arrays.asList("1", 2), dataSourceMetaData, true, null);
        sqlExecutionHook.finishSuccess();
        MockSpan actual = getActualSpan();
        assertThat(actual.operationName(), is("/ShardingSphere/executeSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.PEER_HOSTNAME.getKey()), is("localhost"));
        assertThat(actualTags.get(Tags.PEER_PORT.getKey()), is(8888));
        assertThat(actualTags.get(Tags.DB_TYPE.getKey()), is("sql"));
        assertThat(actualTags.get(Tags.DB_INSTANCE.getKey()), is("success_ds"));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), is("SELECT * FROM success_tbl;"));
        assertThat(actualTags.get(ShardingTags.DB_BIND_VARIABLES.getKey()), is("[1, 2]"));
        verify(activeSpan, times(0)).deactivate();
        sqlExecutionHook.start("success_ds", "SELECT * FROM success_tbl;", null, dataSourceMetaData, true, null);
        sqlExecutionHook.finishSuccess();
    }

    @Test
    public void assertExecuteSuccessForTrunkThreadWhenParamsIsNull() {
        DataSourceMetaData dataSourceMetaData = mock(DataSourceMetaData.class);
        when(dataSourceMetaData.getHostName()).thenReturn("localhost");
        when(dataSourceMetaData.getPort()).thenReturn(8888);
        sqlExecutionHook.start("success_ds", "SELECT * FROM success_tbl;", null, dataSourceMetaData, true, null);
        sqlExecutionHook.finishSuccess();
        MockSpan actual = getActualSpan();
        assertThat(actual.operationName(), is("/ShardingSphere/executeSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.PEER_HOSTNAME.getKey()), is("localhost"));
        assertThat(actualTags.get(Tags.PEER_PORT.getKey()), is(8888));
        assertThat(actualTags.get(Tags.DB_TYPE.getKey()), is("sql"));
        assertThat(actualTags.get(Tags.DB_INSTANCE.getKey()), is("success_ds"));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), is("SELECT * FROM success_tbl;"));
        assertThat(actualTags.get(ShardingTags.DB_BIND_VARIABLES.getKey()), is(""));
        verify(activeSpan, times(0)).deactivate();
    }
    
    @Test
    public void assertExecuteSuccessForBranchThread() {
        DataSourceMetaData dataSourceMetaData = mock(DataSourceMetaData.class);
        when(dataSourceMetaData.getHostName()).thenReturn("localhost");
        when(dataSourceMetaData.getPort()).thenReturn(8888);
        sqlExecutionHook.start("success_ds", "SELECT * FROM success_tbl;", Arrays.asList("1", 2), dataSourceMetaData, false, ExecutorDataMap.getValue());
        sqlExecutionHook.finishSuccess();
        MockSpan actual = getActualSpan();
        assertThat(actual.operationName(), is("/ShardingSphere/executeSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.PEER_HOSTNAME.getKey()), is("localhost"));
        assertThat(actualTags.get(Tags.PEER_PORT.getKey()), is(8888));
        assertThat(actualTags.get(Tags.DB_TYPE.getKey()), is("sql"));
        assertThat(actualTags.get(Tags.DB_INSTANCE.getKey()), is("success_ds"));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), is("SELECT * FROM success_tbl;"));
        assertThat(actualTags.get(ShardingTags.DB_BIND_VARIABLES.getKey()), is("[1, 2]"));
        verify(activeSpan).deactivate();
    }
    
    @Test
    public void assertExecuteFailure() {
        DataSourceMetaData dataSourceMetaData = mock(DataSourceMetaData.class);
        when(dataSourceMetaData.getHostName()).thenReturn("localhost");
        when(dataSourceMetaData.getPort()).thenReturn(8888);
        sqlExecutionHook.start("failure_ds", "SELECT * FROM failure_tbl;", Collections.emptyList(), dataSourceMetaData, true, null);
        sqlExecutionHook.finishFailure(new RuntimeException("SQL execution error"));
        MockSpan actual = getActualSpan();
        assertThat(actual.operationName(), is("/ShardingSphere/executeSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.PEER_HOSTNAME.getKey()), is("localhost"));
        assertThat(actualTags.get(Tags.PEER_PORT.getKey()), is(8888));
        assertThat(actualTags.get(Tags.DB_TYPE.getKey()), is("sql"));
        assertThat(actualTags.get(Tags.DB_INSTANCE.getKey()), is("failure_ds"));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), is("SELECT * FROM failure_tbl;"));
        assertThat(actualTags.get(ShardingTags.DB_BIND_VARIABLES.getKey()), is(""));
        assertSpanError(RuntimeException.class, "SQL execution error");
        verify(activeSpan, times(0)).deactivate();
    }
}
