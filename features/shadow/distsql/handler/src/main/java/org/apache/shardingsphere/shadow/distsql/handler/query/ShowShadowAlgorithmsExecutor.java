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

package org.apache.shardingsphere.shadow.distsql.handler.query;

import org.apache.shardingsphere.distsql.handler.type.rql.rule.RuleAwareRQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Show shadow algorithms executor.
 */
public final class ShowShadowAlgorithmsExecutor extends RuleAwareRQLExecutor<ShowShadowAlgorithmsStatement, ShadowRule> {
    
    public ShowShadowAlgorithmsExecutor() {
        super(ShadowRule.class);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("shadow_algorithm_name", "type", "props", "is_default");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowShadowAlgorithmsStatement sqlStatement, final ShadowRule rule) {
        Iterator<Entry<String, AlgorithmConfiguration>> data = rule.getConfiguration().getShadowAlgorithms().entrySet().iterator();
        String defaultAlgorithm = rule.getConfiguration().getDefaultShadowAlgorithmName();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        while (data.hasNext()) {
            Entry<String, AlgorithmConfiguration> row = data.next();
            result.add(new LocalDataQueryResultRow(row.getKey(), row.getValue().getType(), convertToString(row.getValue().getProps()), Boolean.toString(row.getKey().equals(defaultAlgorithm))));
        }
        return result;
    }
    
    private String convertToString(final Properties props) {
        return null == props ? "" : PropertiesConverter.convert(props);
    }
    
    @Override
    public Class<ShowShadowAlgorithmsStatement> getType() {
        return ShowShadowAlgorithmsStatement.class;
    }
}
