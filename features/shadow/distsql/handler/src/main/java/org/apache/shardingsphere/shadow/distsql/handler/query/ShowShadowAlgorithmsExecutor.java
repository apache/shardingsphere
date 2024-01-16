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
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.shadow.distsql.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

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
        String defaultAlgorithm = rule.getConfiguration().getDefaultShadowAlgorithmName();
        return rule.getConfiguration().getShadowAlgorithms().entrySet().stream()
                .map(entry -> new LocalDataQueryResultRow(entry.getKey(), entry.getValue().getType(),
                        convertToString(entry.getValue().getProps()), Boolean.toString(entry.getKey().equals(defaultAlgorithm)))).collect(Collectors.toList());
    }
    
    private String convertToString(final Properties props) {
        return null == props ? "" : PropertiesConverter.convert(props);
    }
    
    @Override
    public Class<ShowShadowAlgorithmsStatement> getType() {
        return ShowShadowAlgorithmsStatement.class;
    }
}
