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
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.AbstractDatabaseDiscoverySegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryHeartbeatSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryProviderAlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public static DatabaseDiscoveryRuleConfiguration convert(final Collection<AbstractDatabaseDiscoverySegment> ruleSegments) {
        Map<String, List<AbstractDatabaseDiscoverySegment>> segmentMap = ruleSegments.stream().collect(Collectors.groupingBy(each -> each.getClass().getSimpleName()));
        DatabaseDiscoveryRuleConfiguration result = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        segmentMap.getOrDefault(DatabaseDiscoveryDefinitionSegment.class.getSimpleName(), Collections.emptyList())
                .forEach(each -> addRuleConfiguration(result, (DatabaseDiscoveryDefinitionSegment) each));
        return result;
    }
    
    private static void addRuleConfiguration(final DatabaseDiscoveryRuleConfiguration ruleConfig, final DatabaseDiscoveryDefinitionSegment segment) {
        String discoveryTypeName = getName(segment.getName(), segment.getDiscoveryType().getName());
        AlgorithmConfiguration discoveryType = new AlgorithmConfiguration(segment.getDiscoveryType().getName(), segment.getDiscoveryType().getProps());
        String heartbeatName = getName(segment.getName(), "heartbeat");
        DatabaseDiscoveryHeartBeatConfiguration heartbeatConfig = new DatabaseDiscoveryHeartBeatConfiguration(segment.getDiscoveryHeartbeat());
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig =
                new DatabaseDiscoveryDataSourceRuleConfiguration(segment.getName(), new LinkedList<>(segment.getDataSources()), heartbeatName, discoveryTypeName);
        ruleConfig.getDataSources().add(dataSourceRuleConfig);
        ruleConfig.getDiscoveryTypes().put(discoveryTypeName, discoveryType);
        ruleConfig.getDiscoveryHeartbeats().put(heartbeatName, heartbeatConfig);
    }
    
    private static String getName(final String ruleName, final String type) {
        return String.format("%s_%s", ruleName, type.replace(".", "_"));
    }
    
    /**
     * Convert database discovery heartbeat segment to database discovery heartbeat configuration.
     *
     * @param heartbeatSegments database discovery heartbeat segments
     * @return database discovery heartbeat configuration
     */
    public static DatabaseDiscoveryRuleConfiguration convertDiscoveryHeartbeat(final Collection<DatabaseDiscoveryHeartbeatSegment> heartbeatSegments) {
        DatabaseDiscoveryRuleConfiguration result = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        heartbeatSegments.forEach(each -> result.getDiscoveryHeartbeats().put(each.getHeartbeatName(), new DatabaseDiscoveryHeartBeatConfiguration(each.getProperties())));
        return result;
    }
    
    /**
     * Convert database discovery provider algorithm segment to database discovery heartbeat configuration.
     *
     * @param algorithmSegments database discovery provider algorithm segments
     * @return database discovery type configuration
     */
    public static DatabaseDiscoveryRuleConfiguration convertDiscoveryProviderAlgorithm(final Collection<DatabaseDiscoveryProviderAlgorithmSegment> algorithmSegments) {
        final DatabaseDiscoveryRuleConfiguration result = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        algorithmSegments.forEach(each -> result.getDiscoveryTypes().put(each.getDiscoveryProviderName(),
                new AlgorithmConfiguration(each.getAlgorithm().getName(), each.getAlgorithm().getProps())));
        return result;
    }
}
