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

package org.apache.shardingsphere.single.config;

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.rule.function.EnhancedRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Single rule configuration.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
public final class SingleRuleConfiguration implements DatabaseRuleConfiguration, EnhancedRuleConfiguration {
    
    @Getter
    private Collection<String> tables = new LinkedList<>();
    
    private String defaultDataSource;
    
    /**
     * Get default data source.
     *
     * @return default data source
     */
    public Optional<String> getDefaultDataSource() {
        return Optional.ofNullable(defaultDataSource);
    }
    
    @Override
    public Collection<String> getLogicTableNames() {
        Collection<DataNode> dataNodes = tables.stream().map(DataNode::new).collect(Collectors.toList());
        return new CaseInsensitiveSet<>(dataNodes.stream().map(DataNode::getTableName).collect(Collectors.toList()));
    }
}
