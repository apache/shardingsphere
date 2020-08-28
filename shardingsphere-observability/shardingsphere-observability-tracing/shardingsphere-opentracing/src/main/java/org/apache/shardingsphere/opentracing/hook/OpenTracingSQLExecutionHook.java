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

import com.google.common.base.Joiner;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import org.apache.shardingsphere.infra.executor.sql.hook.SQLExecutionHook;
import org.apache.shardingsphere.opentracing.OpenTracingTracer;
import org.apache.shardingsphere.opentracing.constant.ShardingTags;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;

import java.util.List;
import java.util.Map;

/**
 * Open tracing SQL execution hook.
 */
public final class OpenTracingSQLExecutionHook implements SQLExecutionHook {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/executeSQL/";
    
    private ActiveSpan activeSpan;
    
    private Span span;
    
    @Override
    public void start(final String dataSourceName, final String sql, final List<Object> parameters, 
                      final DataSourceMetaData dataSourceMetaData, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) {
        if (!isTrunkThread) {
            activeSpan = ((ActiveSpan.Continuation) shardingExecuteDataMap.get(OpenTracingRootInvokeHook.ACTIVE_SPAN_CONTINUATION)).activate();
        }
        span = OpenTracingTracer.get().buildSpan(OPERATION_NAME)
                .withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.PEER_HOSTNAME.getKey(), dataSourceMetaData.getHostName())
                .withTag(Tags.PEER_PORT.getKey(), dataSourceMetaData.getPort())
                .withTag(Tags.DB_TYPE.getKey(), "sql")
                .withTag(Tags.DB_INSTANCE.getKey(), dataSourceName)
                .withTag(Tags.DB_STATEMENT.getKey(), sql)
                .withTag(ShardingTags.DB_BIND_VARIABLES.getKey(), toString(parameters)).startManual();
        
    }
    
    private String toString(final List<Object> parameterSets) {
        if (null == parameterSets || parameterSets.isEmpty()) {
            return "";
        }
        return String.format("[%s]", Joiner.on(", ").useForNull("Null").join(parameterSets));
    }
    
    @Override
    public void finishSuccess() {
        span.finish();
        if (null != activeSpan) {
            activeSpan.deactivate();
        }
    }
    
    @Override
    public void finishFailure(final Exception cause) {
        ShardingErrorSpan.setError(span, cause);
        span.finish();
        if (null != activeSpan) {
            activeSpan.deactivate();
        }
    }
}
