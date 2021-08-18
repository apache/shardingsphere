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

package org.apache.shardingsphere.proxy.initializer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.governance.core.mode.ClusterMode;
import org.apache.shardingsphere.infra.mode.ShardingSphereMode;
import org.apache.shardingsphere.infra.mode.impl.standalone.StandaloneMode;
import org.apache.shardingsphere.proxy.initializer.impl.ClusterBootstrapInitializer;
import org.apache.shardingsphere.proxy.initializer.impl.MemoryBootstrapInitializer;
import org.apache.shardingsphere.proxy.initializer.impl.StandaloneBootstrapInitializer;

/**
 * Bootstrap initializer factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BootstrapInitializerFactory {
    
    /**
     * New instance.
     * 
     * @param mode ShardingSphere mode
     * @param overwrite is overwrite to persist repository
     * @return bootstrap initializer
     */
    public static BootstrapInitializer newInstance(final ShardingSphereMode mode, final boolean overwrite) {
        // TODO split to pluggable SPI
        if (mode instanceof StandaloneMode) {
            return new StandaloneBootstrapInitializer(mode);
        }
        if (mode instanceof ClusterMode) {
            return new ClusterBootstrapInitializer(mode);
        }
        return new MemoryBootstrapInitializer(mode);
    }
}
