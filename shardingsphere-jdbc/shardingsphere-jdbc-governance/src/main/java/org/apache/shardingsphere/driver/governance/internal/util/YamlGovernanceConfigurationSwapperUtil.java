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
import org.apache.shardingsphere.governance.core.yaml.swapper.RegistryCenterConfigurationYamlSwapper;

/**
 * YAML governance configuration swapper utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlGovernanceConfigurationSwapperUtil {
    
    private static final RegistryCenterConfigurationYamlSwapper SWAPPER = new RegistryCenterConfigurationYamlSwapper();
    
    /**
     * Marshal YAML governance configuration to governance configuration.
     *
     * @param governance YAML governance configuration
     * @return governance configuration
     */
    public static GovernanceConfiguration marshal(final YamlGovernanceConfiguration governance) {
        return new GovernanceConfiguration(governance.getName(), SWAPPER.swapToObject(governance.getRegistryCenter()), governance.isOverwrite());
    }
}
