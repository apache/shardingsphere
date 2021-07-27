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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Database discovery rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseDiscoveryRuleStatementConverter {
    
    /**
     * Convert database discovery rule segment to database discovery rule configuration.
     *
     * @param ruleSegments database discovery rule segments
     * @return database discovery rule configuration
     */
    public static DatabaseDiscoveryRuleConfiguration convert(final Collection<DatabaseDiscoveryRuleSegment> ruleSegments) {
        Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypes = new HashMap<>(ruleSegments.size(), 1);
        for (DatabaseDiscoveryRuleSegment each : ruleSegments) {
            String type = getDatabaseDiscoveryType(each.getName(), each.getDiscoveryTypeName());
            dataSources.add(new DatabaseDiscoveryDataSourceRuleConfiguration(each.getName(), new LinkedList<>(each.getDataSources()), type));
            discoveryTypes.put(type, new ShardingSphereAlgorithmConfiguration(each.getDiscoveryTypeName(), each.getProps()));
        }
        return new DatabaseDiscoveryRuleConfiguration(dataSources, discoveryTypes);
    }
    
    private static String getDatabaseDiscoveryType(final String ruleName, final String type) {
        return String.format("%s_%s", ruleName, type);
    }
}
