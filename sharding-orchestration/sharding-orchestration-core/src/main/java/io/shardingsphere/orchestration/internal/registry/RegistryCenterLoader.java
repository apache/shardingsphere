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

package io.shardingsphere.orchestration.internal.registry;

import com.google.common.base.Preconditions;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.ServiceLoader;

/**
 * Registry center loader from SPI.
 *
 * @author zhangliang
 */
@Slf4j
public final class RegistryCenterLoader {
    
    /**
     * Load registry center from SPI.
     * 
     * @param regCenterConfig registry center configuration
     * @return registry center
     */
    public static RegistryCenter load(final RegistryCenterConfiguration regCenterConfig) {
        Preconditions.checkNotNull(regCenterConfig, "Registry center configuration cannot be null.");
        RegistryCenter result = null;
        int count = 0;
        for (RegistryCenter each : ServiceLoader.load(RegistryCenter.class)) {
            result = each;
            count++;
        }
        Preconditions.checkNotNull(result, "Cannot load implementation class for registry center.");
        if (1 != count) {
            log.warn("Find more than one registry center implementation class, use `{}` now.", result.getClass().getName());
        }
        result.init(regCenterConfig);
        return result;
    }
}
