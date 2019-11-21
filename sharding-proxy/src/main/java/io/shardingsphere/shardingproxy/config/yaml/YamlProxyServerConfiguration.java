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

package io.shardingsphere.shardingproxy.config.yaml;

import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.orchestration.yaml.YamlOrchestrationConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Server configuration for yaml.
 * 
 * @author chenqingyang
 * @author panjuan
 */
@Getter
@Setter
public final class YamlProxyServerConfiguration {
    
    private Authentication authentication = new Authentication();
    
    private YamlOrchestrationConfiguration orchestration;
    
    private Properties props = new Properties();
    
    private Map<String, Object> configMap = new LinkedHashMap<>();
}
