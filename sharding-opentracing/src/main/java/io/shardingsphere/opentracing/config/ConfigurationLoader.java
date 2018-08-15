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

package io.shardingsphere.opentracing.config;

import com.google.common.base.Preconditions;
import lombok.Getter;

/**
 * Config loader.
 *
 * @author gaohongtao
 * @author wangkai
 */
@Getter
public final class ConfigurationLoader {
    
    private static final String OPENTRACING_TRACER_CLASS_NAME = "shardingsphere.opentracing.tracer.class";
    
    private final String tracerClassName;
    
    public ConfigurationLoader() {
        tracerClassName = System.getProperty(OPENTRACING_TRACER_CLASS_NAME);
        Preconditions.checkNotNull(tracerClassName, "Can not find opentracing tracer implementation class.");
    }
}
