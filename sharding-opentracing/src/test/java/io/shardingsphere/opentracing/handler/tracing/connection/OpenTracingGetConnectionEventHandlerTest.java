/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.opentracing.handler.tracing.connection;

import io.opentracing.ActiveSpan;
import io.opentracing.ActiveSpan.Continuation;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.core.spi.connection.get.GetConnectionHook;
import io.shardingsphere.core.spi.connection.get.SPIGetConnectionHook;
import io.shardingsphere.opentracing.constant.ShardingTags;
import io.shardingsphere.opentracing.handler.BaseOpenTracingHandlerTest;
import io.shardingsphere.opentracing.handler.root.OpenTracingRootInvokeHandler;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class OpenTracingGetConnectionEventHandlerTest extends BaseOpenTracingHandlerTest {
    
    private final GetConnectionHook getConnectionHook = new SPIGetConnectionHook();
    
    @Test
    public void assertExecuteSuccessTrunkThread() {
        new OpenTracingRootInvokeHandler().start();
        getConnectionHook.start("test_ds_name");
        DataSourceMetaData dataSourceMetaData = mock(DataSourceMetaData.class);
        when(dataSourceMetaData.getHostName()).thenReturn("localhost");
        when(dataSourceMetaData.getPort()).thenReturn(8888);
        getConnectionHook.finishSuccess(dataSourceMetaData, 3);
        assertThat(getTracer().finishedSpans().size(), is(1));
        MockSpan actual = getTracer().finishedSpans().get(0);
        assertThat(actual.operationName(), is("/Sharding-Sphere/getConnection/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), CoreMatchers.<Object>is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), CoreMatchers.<Object>is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.DB_INSTANCE.getKey()), CoreMatchers.<Object>is("test_ds_name"));
        assertThat(actualTags.get(Tags.PEER_HOSTNAME.getKey()), CoreMatchers.<Object>is("localhost"));
        assertThat(actualTags.get(Tags.PEER_PORT.getKey()), CoreMatchers.<Object>is(8888));
        assertThat(actualTags.get(ShardingTags.CONNECTION_COUNT.getKey()), CoreMatchers.<Object>is(3));
        new OpenTracingRootInvokeHandler().finishSuccess(2);
    }
    
    @Test
    public void assertExecuteSuccessBranchThread() {
        Continuation activeSpanContinuation = mock(Continuation.class);
        ActiveSpan activeSpan = mock(ActiveSpan.class);
        when(activeSpanContinuation.activate()).thenReturn(activeSpan);
        ExecutorDataMap.getDataMap().put(OpenTracingRootInvokeHandler.ROOT_SPAN_CONTINUATION, activeSpanContinuation);
        getConnectionHook.start("test_ds_name");
        assertNotNull(OpenTracingRootInvokeHandler.getActiveSpan().get());
        DataSourceMetaData dataSourceMetaData = mock(DataSourceMetaData.class);
        when(dataSourceMetaData.getHostName()).thenReturn("localhost");
        when(dataSourceMetaData.getPort()).thenReturn(8888);
        getConnectionHook.finishSuccess(dataSourceMetaData, 3);
        assertThat(getTracer().finishedSpans().size(), is(1));
        MockSpan actual = getTracer().finishedSpans().get(0);
        assertThat(actual.operationName(), is("/Sharding-Sphere/getConnection/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), CoreMatchers.<Object>is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), CoreMatchers.<Object>is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.DB_INSTANCE.getKey()), CoreMatchers.<Object>is("test_ds_name"));
        assertThat(actualTags.get(Tags.PEER_HOSTNAME.getKey()), CoreMatchers.<Object>is("localhost"));
        assertThat(actualTags.get(Tags.PEER_PORT.getKey()), CoreMatchers.<Object>is(8888));
        assertThat(actualTags.get(ShardingTags.CONNECTION_COUNT.getKey()), CoreMatchers.<Object>is(3));
        verify(activeSpan).deactivate();
        assertNull(OpenTracingRootInvokeHandler.getActiveSpan().get());
    }
    
    @Test
    public void assertExecuteFailure() {
        new OpenTracingRootInvokeHandler().start();
        getConnectionHook.start("test_ds_name");
        getConnectionHook.finishFailure(new RuntimeException("get connection error"));
        assertThat(getTracer().finishedSpans().size(), is(1));
        MockSpan actual = getTracer().finishedSpans().get(0);
        assertThat(actual.operationName(), is("/Sharding-Sphere/getConnection/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), CoreMatchers.<Object>is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), CoreMatchers.<Object>is(Tags.SPAN_KIND_CLIENT));
        assertSpanError(actual, RuntimeException.class, "get connection error");
        new OpenTracingRootInvokeHandler().finishSuccess(2);
    }
}
