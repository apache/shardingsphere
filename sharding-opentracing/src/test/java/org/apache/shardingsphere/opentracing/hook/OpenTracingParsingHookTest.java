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

package org.apache.shardingsphere.opentracing.hook;

import io.opentracing.mock.MockSpan;
import io.opentracing.tag.Tags;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.hook.ParsingHook;
import org.apache.shardingsphere.core.parse.hook.SPIParsingHook;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.opentracing.constant.ShardingTags;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class OpenTracingParsingHookTest extends BaseOpenTracingHookTest {
    
    private final ParsingHook parsingHook = new SPIParsingHook();
    
    @BeforeClass
    public static void registerSPI() {
        NewInstanceServiceLoader.register(ParsingHook.class);
    }
    
    @Test
    public void assertExecuteSuccess() {
        parsingHook.start("SELECT * FROM XXX;");
        parsingHook.finishSuccess(mock(SQLStatement.class));
        MockSpan actual = getActualSpan();
        assertThat(actual.operationName(), is("/ShardingSphere/parseSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), CoreMatchers.<Object>is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), CoreMatchers.<Object>is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), CoreMatchers.<Object>is("SELECT * FROM XXX;"));
    }
    
    @Test
    public void assertExecuteFailure() {
        parsingHook.start("SELECT * FROM XXX;");
        parsingHook.finishFailure(new ShardingException("parse SQL error"));
        MockSpan actual = getActualSpan();
        assertThat(actual.operationName(), is("/ShardingSphere/parseSQL/"));
        Map<String, Object> actualTags = actual.tags();
        assertThat(actualTags.get(Tags.COMPONENT.getKey()), CoreMatchers.<Object>is(ShardingTags.COMPONENT_NAME));
        assertThat(actualTags.get(Tags.SPAN_KIND.getKey()), CoreMatchers.<Object>is(Tags.SPAN_KIND_CLIENT));
        assertThat(actualTags.get(Tags.DB_STATEMENT.getKey()), CoreMatchers.<Object>is("SELECT * FROM XXX;"));
        assertSpanError(ShardingException.class, "parse SQL error");
    }
}
