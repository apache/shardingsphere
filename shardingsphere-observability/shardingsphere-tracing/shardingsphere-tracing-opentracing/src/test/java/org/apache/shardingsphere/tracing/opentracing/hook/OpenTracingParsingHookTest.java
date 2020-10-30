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
import io.opentracing.tag.Tags;
import org.apache.shardingsphere.tracing.opentracing.constant.ShardingTags;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.parser.hook.ParsingHook;
import org.apache.shardingsphere.infra.parser.hook.ParsingHookRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class OpenTracingParsingHookTest extends BaseOpenTracingHookTest {
    
    private final ParsingHookRegistry registry = ParsingHookRegistry.getInstance();
    
    @BeforeClass
    public static void registerSPI() {
        ShardingSphereServiceLoader.register(ParsingHook.class);
    }
    
    @Test
    public void assertExecuteSuccess() {
        registry.start("SELECT * FROM XXX;");
        registry.finishSuccess(mock(SQLStatement.class));
        MockSpan actual = getActualSpan();
        assertThat(actual.operationName(), is("/ShardingSphere/parseSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), is("SELECT * FROM XXX;"));
    }
    
    @Test
    public void assertExecuteFailure() {
        registry.start("SELECT * FROM XXX;");
        registry.finishFailure(new ShardingSphereException("parse SQL error"));
        MockSpan actual = getActualSpan();
        assertThat(actual.operationName(), is("/ShardingSphere/parseSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), is("SELECT * FROM XXX;"));
        assertSpanError(ShardingSphereException.class, "parse SQL error");
    }
}
