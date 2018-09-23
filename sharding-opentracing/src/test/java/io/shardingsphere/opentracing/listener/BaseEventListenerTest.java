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

import com.google.common.collect.HashMultimap;
import com.google.common.eventbus.EventBus;
import io.opentracing.NoopTracerFactory;
import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.opentracing.constant.ShardingErrorLogTags;
import io.shardingsphere.opentracing.ShardingTracer;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class BaseEventListenerTest {
    
    private static final MockTracer TRACER = new MockTracer(new ThreadLocalActiveSpanSource(), MockTracer.Propagator.TEXT_MAP);
    
    static MockTracer getTracer() {
        return TRACER;
    }
    
    @BeforeClass
    public static void initTracer() {
        ShardingTracer.init(TRACER);
        ExecutorDataMap.getDataMap().remove(RootInvokeEventListener.OVERALL_SPAN_CONTINUATION);
    }
    
    @AfterClass
    public static void releaseTracer() throws NoSuchFieldException, IllegalAccessException {
        Field field = GlobalTracer.class.getDeclaredField("tracer");
        field.setAccessible(true);
        field.set(GlobalTracer.class, NoopTracerFactory.create());
    }
    
    @AfterClass
    public static void resetShardingEventBus() throws NoSuchFieldException, IllegalAccessException {
        Field field = EventBus.class.getDeclaredField("subscribersByType");
        field.setAccessible(true);
        field.set(ShardingEventBusInstance.getInstance(), HashMultimap.create());
    }
    
    @Before
    public void resetTracer() {
        TRACER.reset();
    }
    
    protected final void assertSpanError(final MockSpan actualSpan, final Class<? extends Throwable> expectedException, final String expectedErrorMessage) {
        assertTrue((Boolean) actualSpan.tags().get(Tags.ERROR.getKey()));
        List<MockSpan.LogEntry> actualLogEntries = actualSpan.logEntries();
        assertThat(actualLogEntries.size(), is(1));
        assertThat(actualLogEntries.get(0).fields().size(), is(3));
        assertThat(actualLogEntries.get(0).fields().get(ShardingErrorLogTags.EVENT), CoreMatchers.<Object>is(ShardingErrorLogTags.EVENT_ERROR_TYPE));
        assertThat(actualLogEntries.get(0).fields().get(ShardingErrorLogTags.ERROR_KIND), CoreMatchers.<Object>is(expectedException.getName()));
        assertThat(actualLogEntries.get(0).fields().get(ShardingErrorLogTags.MESSAGE), CoreMatchers.<Object>is(expectedErrorMessage));
    }
}
