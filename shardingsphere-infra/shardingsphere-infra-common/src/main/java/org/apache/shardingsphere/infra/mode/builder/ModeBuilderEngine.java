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

package org.apache.shardingsphere.infra.mode.builder;

import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.mode.impl.memory.MemoryMode;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

import java.util.Properties;

/**
 * Mode builder engine.
 */
public final class ModeBuilderEngine {
    
    static {
        ShardingSphereServiceLoader.register(ModeBuilder.class);
    }
    
    /**
     * Build mode.
     * 
     * @param config mode configuration
     * @return built mode
     */
    public static ShardingSphereMode build(final ModeConfiguration config) {
        return null == config ? new MemoryMode() : TypedSPIRegistry.getRegisteredService(ModeBuilder.class, config.getType(), new Properties()).build(config);
    }
}
