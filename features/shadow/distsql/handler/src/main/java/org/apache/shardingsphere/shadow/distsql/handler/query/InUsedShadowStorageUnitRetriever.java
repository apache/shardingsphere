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

import org.apache.shardingsphere.distsql.handler.executor.rql.resource.InUsedStorageUnitRetriever;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * In used shadow storage unit retriever.
 */
public final class InUsedShadowStorageUnitRetriever implements InUsedStorageUnitRetriever<ShadowRule> {
    
    @Override
    public Collection<String> getInUsedResources(final ShowRulesUsedStorageUnitStatement sqlStatement, final ShadowRule rule) {
        return rule.getConfiguration().getDataSources().stream()
                .filter(each -> each.getShadowDataSourceName().equalsIgnoreCase(sqlStatement.getStorageUnitName())
                        || each.getProductionDataSourceName().equalsIgnoreCase(sqlStatement.getStorageUnitName()))
                .map(ShadowDataSourceConfiguration::getName).collect(Collectors.toList());
    }
    
    @Override
    public Class<ShadowRule> getType() {
        return ShadowRule.class;
    }
}
