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
import org.apache.shardingsphere.shadow.distsql.statement.ShowDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Show default shadow algorithm executor.
 */
public final class ShowDefaultShadowAlgorithmExecutor extends RuleAwareRQLExecutor<ShowDefaultShadowAlgorithmStatement, ShadowRule> {
    
    public ShowDefaultShadowAlgorithmExecutor() {
        super(ShadowRule.class);
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("shadow_algorithm_name", "type", "props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowDefaultShadowAlgorithmStatement sqlStatement, final ShadowRule rule) {
        String defaultAlgorithm = rule.getConfiguration().getDefaultShadowAlgorithmName();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (Entry<String, AlgorithmConfiguration> entry : rule.getConfiguration().getShadowAlgorithms().entrySet().stream()
                .filter(each -> each.getKey().equals(defaultAlgorithm)).collect(Collectors.toMap(Entry::getKey, Entry::getValue)).entrySet()) {
            result.add(new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(), convertToString(entry.getValue().getProps())));
        }
        return result;
    }
    
    private String convertToString(final Properties props) {
        return null == props ? "" : PropertiesConverter.convert(props);
    }
    
    @Override
    public Class<ShowDefaultShadowAlgorithmStatement> getType() {
        return ShowDefaultShadowAlgorithmStatement.class;
    }
}
