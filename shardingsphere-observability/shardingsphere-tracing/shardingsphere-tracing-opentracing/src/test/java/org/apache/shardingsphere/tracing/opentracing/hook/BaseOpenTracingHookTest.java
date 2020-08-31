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

import io.opentracing.mock.MockSpan;
import io.opentracing.mock.MockTracer;
import io.opentracing.noop.NoopTracerFactory;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import io.opentracing.util.ThreadLocalScopeManager;
import lombok.SneakyThrows;
import org.apache.shardingsphere.tracing.opentracing.OpenTracingTracer;
import org.apache.shardingsphere.tracing.opentracing.constant.ShardingErrorLogTags;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class BaseOpenTracingHookTest {
    
    private static final MockTracer TRACER = new MockTracer(new ThreadLocalScopeManager(), MockTracer.Propagator.TEXT_MAP);
    
    @BeforeClass
    public static void initTracer() {
        OpenTracingTracer.init(TRACER);
    }
    
    @AfterClass
    @SneakyThrows(ReflectiveOperationException.class)
    public static void releaseTracer() {
        Field field = GlobalTracer.class.getDeclaredField("tracer");
        field.setAccessible(true);
        field.set(GlobalTracer.class, TRACER);
    }
    
    @Before
    public final void resetTracer() {
        releaseTracer();
        TRACER.reset();
    }
    
    protected final MockSpan getActualSpan() {
        List<MockSpan> finishedSpans = TRACER.finishedSpans();
        assertThat(finishedSpans.size(), is(1));
        return finishedSpans.get(0);
    }
    
    protected final void assertSpanError(final Class<? extends Throwable> expectedException, final String expectedErrorMessage) {
        MockSpan actual = getActualSpan();
        assertTrue((Boolean) actual.tags().get(Tags.ERROR.getKey()));
        List<MockSpan.LogEntry> actualLogEntries = actual.logEntries();
        assertThat(actualLogEntries.size(), is(1));
        assertThat(actualLogEntries.get(0).fields().size(), is(3));
        assertThat(actualLogEntries.get(0).fields().get(ShardingErrorLogTags.EVENT), is(ShardingErrorLogTags.EVENT_ERROR_TYPE));
        assertThat(actualLogEntries.get(0).fields().get(ShardingErrorLogTags.ERROR_KIND), is(expectedException.getName()));
        assertThat(actualLogEntries.get(0).fields().get(ShardingErrorLogTags.MESSAGE), is(expectedErrorMessage));
    }
}
