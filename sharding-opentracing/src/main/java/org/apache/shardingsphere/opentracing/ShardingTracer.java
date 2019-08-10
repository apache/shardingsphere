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

package org.apache.shardingsphere.opentracing;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.opentracing.spi.ShardingTracerGenerator;
import org.apache.shardingsphere.opentracing.spi.ShardingTracerGeneratorServiceLoader;

import java.util.Properties;

/**
 * Sharding tracer object container.
 *
 * @author gaohongtao
 * @author wangkai
 * @author liya
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingTracer {
    
    private static final String OPENTRACING_TRACER_CLASS_NAME = "org.apache.shardingsphere.opentracing.tracer.class";
    
    /**
     * Initialize sharding tracer.
     */
    public static void init() {
        String tracerClassName = System.getProperty(OPENTRACING_TRACER_CLASS_NAME);
        Preconditions.checkNotNull(tracerClassName, "Can not find opentracing tracer implementation class via system property `%s`", OPENTRACING_TRACER_CLASS_NAME);
        try {
            init((Tracer) Class.forName(tracerClassName).newInstance());
        } catch (final ReflectiveOperationException ex) {
            throw new ShardingException("Initialize opentracing tracer class failure.", ex);
        }
    }
    
    /**
     * Initialize sharding tracer.
     * 
     * @param tracer opentracing tracer
     */
    public static void init(final Tracer tracer) {
        if (!GlobalTracer.isRegistered()) {
            GlobalTracer.register(tracer);
        }
    }

    /**
     * Initialize sharding tracer by SPI with properties.
     *
     * @param tracerGeneratorType type of tracerGenerator
     * @param properties properties of tracerGenerator
     */
    public static void init(final String tracerGeneratorType, final Properties properties) {
        if (Strings.isNullOrEmpty(tracerGeneratorType)) {
            return;
        }
        ShardingTracerGeneratorServiceLoader serviceLoader = new ShardingTracerGeneratorServiceLoader();
        ShardingTracerGenerator shardingTracerGenerator = serviceLoader.newService(tracerGeneratorType, properties);
        ShardingTracer.init(shardingTracerGenerator.generate());
    }

    /**
     * Initialize sharding tracer by SPI.
     *
     * @param tracerGeneratorType type of tracerGenerator
     */
    public static void init(final String tracerGeneratorType) {
        init(tracerGeneratorType, new Properties());
    }
    
    /**
     * Get tracer.
     *
     * @return tracer
     */
    public static Tracer get() {
        return GlobalTracer.get();
    }
}
