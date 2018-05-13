/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.yaml.masterslave;

import io.shardingjdbc.core.rule.MasterSlaveRule;
import lombok.Getter;
import lombok.Setter;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Master-slave configuration for yaml.
 *
 * @author caohao
 */
@Getter
@Setter
public class YamlMasterSlaveConfiguration {
    
    private Map<String, DataSource> dataSources = new HashMap<>();
    
    private YamlMasterSlaveRuleConfiguration masterSlaveRule;
    
    /**
     * Get master-slave rule from yaml.
     *
     * @param dataSourceMap data source map
     * @return master-slave rule from yaml
     * @throws SQLException SQL exception
     */
    public MasterSlaveRule getMasterSlaveRule(final Map<String, DataSource> dataSourceMap) throws SQLException {
        return masterSlaveRule.getMasterSlaveRuleConfiguration().build(dataSourceMap.isEmpty() ? dataSources : dataSourceMap);
    }
}
