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

package org.apache.shardingsphere.single.api.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.config.rule.function.EnhancedRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.function.MutableRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.single.api.config.rule.SingleTableRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

/**
 * Single rule configuration.
 */
@Getter
@Setter
public final class SingleRuleConfiguration implements DatabaseRuleConfiguration, EnhancedRuleConfiguration, MutableRuleConfiguration {
    
    private final Collection<SingleTableRuleConfiguration> tables = new LinkedHashSet<>();
    
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
    public void put(final String dataSourceName, final String schemaName, final String tableName) {
        SingleTableRuleConfiguration singleTableRuleConfig =
                tables.stream().filter(each -> each.getDataSourceName().equals(dataSourceName)).findFirst().orElse(new SingleTableRuleConfiguration(dataSourceName));
        singleTableRuleConfig.getTables().add(new QualifiedTable(schemaName, tableName));
        tables.add(singleTableRuleConfig);
    }
    
    @Override
    public void remove(final String dataSourceName, final String schemaName, final String tableName) {
        Optional<SingleTableRuleConfiguration> singleTableRuleConfig = tables.stream().filter(each -> each.getDataSourceName().equals(dataSourceName)).findFirst();
        singleTableRuleConfig.ifPresent(optional -> optional.getTables().remove(new QualifiedTable(schemaName, tableName)));
    }
}
