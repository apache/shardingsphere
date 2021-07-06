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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import com.google.common.base.Joiner;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Result set for show readwrite splitting rule.
 */
public final class ReadwriteSplittingRuleQueryResultSet implements DistSQLResultSet {
    
    private Iterator<ReadwriteSplittingDataSourceRuleConfiguration> data;
    
    private Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).map(each -> (ReadwriteSplittingRuleConfiguration) each).findAny();
        data = ruleConfig.map(optional -> optional.getDataSources().iterator()).orElse(Collections.emptyIterator());
        loadBalancers = ruleConfig.map(ReadwriteSplittingRuleConfiguration::getLoadBalancers).orElse(Collections.emptyMap());
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "autoAwareDataSourceName", "writeDataSourceName", "readDataSourceNames", "loadBalancerType", "loadBalancerProps");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        ReadwriteSplittingDataSourceRuleConfiguration ruleConfig = data.next();
        return Arrays.asList(ruleConfig.getName(), ruleConfig.getAutoAwareDataSourceName(), ruleConfig.getWriteDataSourceName(), Joiner.on(",").join(ruleConfig.getReadDataSourceNames()),
                null == loadBalancers.get(ruleConfig.getLoadBalancerName()) ? null : loadBalancers.get(ruleConfig.getLoadBalancerName()).getType(),
                PropertiesConverter.convert(loadBalancers.get(ruleConfig.getLoadBalancerName()).getProps()));
    }
    
    @Override
    public String getType() {
        return ShowReadwriteSplittingRulesStatement.class.getCanonicalName();
    }
}
