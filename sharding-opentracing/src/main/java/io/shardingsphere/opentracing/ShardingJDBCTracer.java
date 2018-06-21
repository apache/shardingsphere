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

package io.shardingsphere.opentracing;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.util.EventBusInstance;
import io.shardingsphere.opentracing.config.ConfigurationLoader;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * A container of tracer object.
 * 
 * @author gaohongtao
 * @author wangkai
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingJDBCTracer {
    
    /**
     * Initialize tracer.
     */
    public static void init() {
        if (GlobalTracer.isRegistered()) {
            return;
        }
        String tracerClassName = new ConfigurationLoader().getTracerClassName();
        try {
            init((Tracer) Class.forName(tracerClassName).newInstance());
        } catch (final InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            throw new ShardingException("Parse tracer class name", ex);
        }
    }
    
    /**
     * Initialize tracer from another one.
     * 
     * @param tracer that is delegated
     */
    public static void init(final Tracer tracer) {
        if (GlobalTracer.isRegistered()) {
            return;
        }
        GlobalTracer.register(tracer);
        EventBusInstance.getInstance().register(new ExecuteEventListener());
        EventBusInstance.getInstance().register(new SqlRoutingEventListener());
        EventBusInstance.getInstance().register(new MergeEventListener());
    }
    
    /**
     * Get the tracer from container.
     * 
     * @return tracer
     */
    public static Tracer get() {
        if (GlobalTracer.isRegistered()) {
            return GlobalTracer.get();
        }
        init();
        return GlobalTracer.get();
    }
}
