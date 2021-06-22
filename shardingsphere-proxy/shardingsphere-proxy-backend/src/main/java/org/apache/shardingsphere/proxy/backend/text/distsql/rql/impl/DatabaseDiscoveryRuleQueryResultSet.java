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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RQLResultSet;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Result set for show database discovery rule.
 */
public final class DatabaseDiscoveryRuleQueryResultSet implements RQLResultSet {
    
    private Iterator<DatabaseDiscoveryDataSourceRuleConfiguration> data;
    
    private Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes;
    
    @Override
    public void init(final String schemaName, final SQLStatement sqlStatement) {
        Optional<DatabaseDiscoveryRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof DatabaseDiscoveryRuleConfiguration).map(each -> (DatabaseDiscoveryRuleConfiguration) each).findAny();
        data = ruleConfig.map(optional -> optional.getDataSources().iterator()).orElse(Collections.emptyIterator());
        discoveryTypes = ruleConfig.map(DatabaseDiscoveryRuleConfiguration::getDiscoveryTypes).orElse(Collections.emptyMap());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "dataSourceNames", "discoverType", "discoverProps");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = data.next();
        return Arrays.asList(dataSourceRuleConfig.getName(), Joiner.on(",").join(dataSourceRuleConfig.getDataSourceNames()), 
                discoveryTypes.get(dataSourceRuleConfig.getDiscoveryTypeName()).getType(), PropertiesConverter.convert(discoveryTypes.get(dataSourceRuleConfig.getDiscoveryTypeName()).getProps()));
    }
}
