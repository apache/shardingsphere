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

package io.shardingsphere.opentracing.listener;

import com.google.common.eventbus.EventBus;
import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.connection.GetConnectionFinishEvent;
import io.shardingsphere.core.event.connection.GetConnectionStartEvent;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.opentracing.ShardingTags;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GetConnectionEventListenerTest extends BaseEventListenerTest {
    
    private final EventBus shardingEventBus = ShardingEventBusInstance.getInstance();
    
    @Test
    public void assertExecuteSuccess() {
        shardingEventBus.post(new GetConnectionStartEvent("test_ds_name"));
        DataSourceMetaData dataSourceMetaData = mock(DataSourceMetaData.class);
        when(dataSourceMetaData.getHostName()).thenReturn("localhost");
        when(dataSourceMetaData.getPort()).thenReturn(8888);
        GetConnectionFinishEvent finishEvent = new GetConnectionFinishEvent(3, dataSourceMetaData);
        finishEvent.setExecuteSuccess();
        shardingEventBus.post(finishEvent);
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
    }
    
    @Test
    public void assertExecuteFailure() {
        shardingEventBus.post(new GetConnectionStartEvent("test_ds_name"));
        GetConnectionFinishEvent finishEvent = new GetConnectionFinishEvent(3, null);
        finishEvent.setExecuteFailure(new RuntimeException("get connection error"));
        shardingEventBus.post(finishEvent);
        assertThat(getTracer().finishedSpans().size(), is(1));
        MockSpan actual = getTracer().finishedSpans().get(0);
        assertThat(actual.operationName(), is("/Sharding-Sphere/getConnection/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), CoreMatchers.<Object>is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), CoreMatchers.<Object>is(Tags.SPAN_KIND_CLIENT));
        assertSpanError(actual, RuntimeException.class, "get connection error");
    }
}
