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

package org.apache.shardingsphere.orchestration.internal.configcenter;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.spi.NewInstanceServiceLoader;
import org.apache.shardingsphere.spi.TypeBasedSPIServiceLoader;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.config.api.ConfigCenterConfiguration;


/**
 * Config center loader from SPI.
 *
 * @author wangguangyuan
 */
@Slf4j
public final class ConfigCenterServiceLoader extends TypeBasedSPIServiceLoader<ConfigCenter> {
    
    static {
        NewInstanceServiceLoader.register(ConfigCenter.class);
    }
    
    public ConfigCenterServiceLoader() {
        super(ConfigCenter.class);
    }
    
    /**
     * Load config center from SPI.
     * 
     * @param configCenterConfig config center configuration
     * @return config center
     */
    public ConfigCenter load(final ConfigCenterConfiguration configCenterConfig) {
        Preconditions.checkNotNull(configCenterConfig, "Config center configuration cannot be null.");
        ConfigCenter result = newService(configCenterConfig.getType(), configCenterConfig.getProperties());
        result.init(configCenterConfig);
        return result;
    }
}
