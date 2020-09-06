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

package org.apache.shardingsphere.driver.governance.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.swapper.GovernanceCenterConfigurationYamlSwapper;

/**
 * YAML governance configuration swapper utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlGovernanceRepositoryConfigurationSwapperUtil {
    
    private static final GovernanceCenterConfigurationYamlSwapper SWAPPER = new GovernanceCenterConfigurationYamlSwapper();
    
    /**
     * Marshal YAML governance repository configuration map to instance configuration map.
     *
     * @param governance YAML governance configuration
     * @return governance repository configuration map
     */
    public static GovernanceConfiguration marshal(final YamlGovernanceConfiguration governance) {
        if (null == governance.getAdditionalConfigCenter()) {
            return new GovernanceConfiguration(governance.getName(), SWAPPER.swapToObject(governance.getRegistryCenter()), governance.isOverwrite());
        }
        return new GovernanceConfiguration(governance.getName(),
                SWAPPER.swapToObject(governance.getRegistryCenter()), SWAPPER.swapToObject(governance.getAdditionalConfigCenter()), governance.isOverwrite());
    }
}
