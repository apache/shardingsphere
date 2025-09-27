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

package org.apache.shardingsphere.test.e2e.operation.pipeline.env;

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;

import java.util.Properties;

@Getter
public final class PipelineE2EEnvironment {
    
    private static final PipelineE2EEnvironment INSTANCE = new PipelineE2EEnvironment();
    
    private final PipelineProxyType proxyType;
    
    private PipelineE2EEnvironment() {
        Properties props = EnvironmentPropertiesLoader.loadProperties();
        proxyType = PipelineProxyType.valueOf(props.getProperty("e2e.pipeline.proxy.type", PipelineProxyType.NONE.name()).toUpperCase());
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static PipelineE2EEnvironment getInstance() {
        return INSTANCE;
    }
}
