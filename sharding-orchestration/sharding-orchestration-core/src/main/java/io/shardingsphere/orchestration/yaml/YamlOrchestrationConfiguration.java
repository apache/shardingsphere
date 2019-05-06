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

package io.shardingsphere.orchestration.yaml;

import com.google.common.base.Preconditions;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * Orchestration configuration for yaml.
 *
 * @author caohao
 * @author panjuan
 */
@Getter
@Setter
public class YamlOrchestrationConfiguration {
    
    private String name;
    
    private RegistryCenterConfiguration registry;
    
    private boolean overwrite;
    
    /**
     * Get orchestration configuration from yaml.
     *
     * @return orchestration configuration from yaml
     */
    public OrchestrationConfiguration getOrchestrationConfiguration() {
        Preconditions.checkNotNull(registry, "Registry center must be required.");
        return new OrchestrationConfiguration(name, registry, overwrite);
    }
}
