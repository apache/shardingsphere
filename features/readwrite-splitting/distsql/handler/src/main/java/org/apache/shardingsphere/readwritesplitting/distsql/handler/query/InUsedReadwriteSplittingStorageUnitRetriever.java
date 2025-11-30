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

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.distsql.handler.executor.rql.resource.InUsedStorageUnitRetriever;
import org.apache.shardingsphere.distsql.statement.type.rql.rule.database.ShowRulesUsedStorageUnitStatement;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Collection;
import java.util.HashSet;

/**
 * In used readwrite-splitting storage unit retriever.
 */
public final class InUsedReadwriteSplittingStorageUnitRetriever implements InUsedStorageUnitRetriever<ReadwriteSplittingRule> {
    
    @Override
    public Collection<String> getInUsedResources(final ShowRulesUsedStorageUnitStatement sqlStatement, final ReadwriteSplittingRule rule) {
        Collection<String> result = new HashSet<>(1, 1F);
        for (ReadwriteSplittingDataSourceGroupRuleConfiguration each : rule.getConfiguration().getDataSourceGroups()) {
            if (each.getWriteDataSourceName().equalsIgnoreCase(sqlStatement.getStorageUnitName())) {
                result.add(each.getName());
            }
            if (new CaseInsensitiveSet<>(each.getReadDataSourceNames()).contains(sqlStatement.getStorageUnitName())) {
                result.add(each.getName());
            }
        }
        return result;
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getType() {
        return ReadwriteSplittingRule.class;
    }
}
