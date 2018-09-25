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

package io.shardingsphere.opentracing.handler.parsing;

import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.spi.event.parsing.ParsingEventHandlerLoader;
import io.shardingsphere.core.spi.event.parsing.ParsingFinishEvent;
import io.shardingsphere.core.spi.event.parsing.ParsingStartEvent;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.opentracing.constant.ShardingTags;
import io.shardingsphere.opentracing.handler.BaseOpenTracingHandlerTest;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class OpenTracingParsingEventHandlerTest extends BaseOpenTracingHandlerTest {
    
    private final ParsingEventHandlerLoader loader = ParsingEventHandlerLoader.getInstance();
    
    @Test
    public void assertExecuteSuccess() {
        loader.start(new ParsingStartEvent("SELECT * FROM XXX;"));
        loader.finish(new ParsingFinishEvent());
        assertThat(getTracer().finishedSpans().size(), is(1));
        MockSpan actual = getTracer().finishedSpans().get(0);
        assertThat(actual.operationName(), is("/Sharding-Sphere/parseSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), CoreMatchers.<Object>is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), CoreMatchers.<Object>is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), CoreMatchers.<Object>is("SELECT * FROM XXX;"));
    }
    
    @Test
    public void assertExecuteFailure() {
        loader.start(new ParsingStartEvent("SELECT * FROM XXX;"));
        ParsingFinishEvent finishEvent = new ParsingFinishEvent();
        finishEvent.setException(new ShardingException("parse SQL error"));
        loader.finish(finishEvent);
        assertThat(getTracer().finishedSpans().size(), is(1));
        MockSpan actual = getTracer().finishedSpans().get(0);
        assertThat(actual.operationName(), is("/Sharding-Sphere/parseSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), CoreMatchers.<Object>is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), CoreMatchers.<Object>is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), CoreMatchers.<Object>is("SELECT * FROM XXX;"));
        assertSpanError(actual, ShardingException.class, "parse SQL error");
    }
}
