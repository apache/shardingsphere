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

package io.shardingsphere.opentracing.hook;

import com.google.common.base.Joiner;
import io.opentracing.ActiveSpan;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.shardingsphere.core.metadata.datasource.DataSourceMetaData;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.opentracing.ShardingTracer;
import io.shardingsphere.opentracing.constant.ShardingTags;
import io.shardingsphere.spi.executor.SQLExecutionHook;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Open tracing SQL execution hook.
 *
 * @author zhangliang
 */
public final class OpenTracingSQLExecutionHook implements SQLExecutionHook {
    
    private static final String OPERATION_NAME = "/" + ShardingTags.COMPONENT_NAME + "/executeSQL/";
    
    private ActiveSpan activeSpan;
    
    private Span span;
    
    @Override
    public void start(final RouteUnit routeUnit, final DataSourceMetaData dataSourceMetaData, final boolean isTrunkThread, final Map<String, Object> shardingExecuteDataMap) {
        if (!isTrunkThread) {
            activeSpan = ((ActiveSpan.Continuation) shardingExecuteDataMap.get(OpenTracingRootInvokeHook.ACTIVE_SPAN_CONTINUATION)).activate();
        }
        span = ShardingTracer.get().buildSpan(OPERATION_NAME)
                .withTag(Tags.COMPONENT.getKey(), ShardingTags.COMPONENT_NAME)
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
                .withTag(Tags.PEER_HOSTNAME.getKey(), dataSourceMetaData.getHostName())
                .withTag(Tags.PEER_PORT.getKey(), dataSourceMetaData.getPort())
                .withTag(Tags.DB_TYPE.getKey(), "sql")
                .withTag(Tags.DB_INSTANCE.getKey(), routeUnit.getDataSourceName())
                .withTag(Tags.DB_STATEMENT.getKey(), routeUnit.getSqlUnit().getSql())
                .withTag(ShardingTags.DB_BIND_VARIABLES.getKey(), toString(routeUnit.getSqlUnit().getParameterSets())).startManual();
        
    }
    
    private String toString(final List<List<Object>> parameterSets) {
        return parameterSets.isEmpty() ? "" : Joiner.on(", ").join(toStringList(parameterSets));
    }
    
    private List<String> toStringList(final List<List<Object>> parameterSets) {
        List<String> result = new LinkedList<>();
        for (List<Object> each : parameterSets) {
            result.add(String.format("[%s]", Joiner.on(", ").join(each)));
        }
        return result;
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
