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

package org.apache.shardingsphere.parser.yaml.swapper;

import org.apache.shardingsphere.infra.util.yaml.swapper.YamlConfigurationSwapper;
import org.apache.shardingsphere.parser.yaml.config.YamlSQLParserCacheOptionRuleConfiguration;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;

/**
 * YAML SQL parser cache option configuration swapper.
 */
public final class YamlSQLParserCacheOptionConfigurationSwapper implements YamlConfigurationSwapper<YamlSQLParserCacheOptionRuleConfiguration, CacheOption> {
    
    @Override
    public YamlSQLParserCacheOptionRuleConfiguration swapToYamlConfiguration(final CacheOption data) {
        YamlSQLParserCacheOptionRuleConfiguration result = new YamlSQLParserCacheOptionRuleConfiguration();
        result.setInitialCapacity(data.getInitialCapacity());
        result.setMaximumSize(data.getMaximumSize());
        return result;
    }
    
    @Override
    public CacheOption swapToObject(final YamlSQLParserCacheOptionRuleConfiguration yamlConfig) {
        return new CacheOption(yamlConfig.getInitialCapacity(), yamlConfig.getMaximumSize());
    }
}
