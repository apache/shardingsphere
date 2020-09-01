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

import io.opentracing.Span;
import io.opentracing.tag.Tags;
import org.apache.shardingsphere.sql.parser.hook.ParsingHook;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.tracing.opentracing.OpenTracingTracer;
import org.apache.shardingsphere.tracing.opentracing.constant.ShardingTags;

/**
 * Open tracing parsing hook.
 */
public final class OpenTracingParsingHook implements ParsingHook {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/parseSQL/";
    
    private Span span;
    
    @Override
    public void start(final String sql) {
        span = OpenTracingTracer.get().buildSpan(OPERATION_NAME)
                .withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.DB_STATEMENT.getKey(), sql).startManual();
    }
    
    @Override
    public void finishSuccess(final SQLStatement sqlStatement) {
        span.finish();
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        ShardingErrorSpan.setError(span, cause);
        span.finish();
    }
}
