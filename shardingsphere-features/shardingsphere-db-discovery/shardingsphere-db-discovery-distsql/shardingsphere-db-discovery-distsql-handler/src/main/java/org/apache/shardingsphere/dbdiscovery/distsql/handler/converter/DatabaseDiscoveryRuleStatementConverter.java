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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryConstructionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryDefinitionSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryHeartbeatSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryTypeSegment;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;

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
        final DatabaseDiscoveryRuleConfiguration configuration = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        segmentMap.getOrDefault(DatabaseDiscoveryDefinitionSegment.class.getSimpleName(), Collections.emptyList())
                .forEach(each -> addConfiguration(configuration, (DatabaseDiscoveryDefinitionSegment) each));
        segmentMap.getOrDefault(DatabaseDiscoveryConstructionSegment.class.getSimpleName(), Collections.emptyList())
                .forEach(each -> addConfiguration(configuration, (DatabaseDiscoveryConstructionSegment) each));
        return configuration;
    }
    
    private static void addConfiguration(final DatabaseDiscoveryRuleConfiguration configuration, final DatabaseDiscoveryDefinitionSegment segment) {
        String discoveryTypeName = getName(segment.getName(), segment.getDiscoveryType().getName());
        ShardingSphereAlgorithmConfiguration discoveryType = new ShardingSphereAlgorithmConfiguration(segment.getDiscoveryType().getName(), segment.getDiscoveryType().getProps());
        String heartbeatName = getName(segment.getName(), "heartbeat");
        DatabaseDiscoveryHeartBeatConfiguration heartbeatConfiguration = new DatabaseDiscoveryHeartBeatConfiguration(segment.getDiscoveryHeartbeat());
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfiguration
                = new DatabaseDiscoveryDataSourceRuleConfiguration(segment.getName(), new LinkedList<>(segment.getDataSources()), heartbeatName, discoveryTypeName);
        configuration.getDataSources().add(dataSourceRuleConfiguration);
        configuration.getDiscoveryTypes().put(discoveryTypeName, discoveryType);
        configuration.getDiscoveryHeartbeats().put(heartbeatName, heartbeatConfiguration);
    }
    
    private static void addConfiguration(final DatabaseDiscoveryRuleConfiguration configuration, final DatabaseDiscoveryConstructionSegment segment) {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfiguration
                = new DatabaseDiscoveryDataSourceRuleConfiguration(segment.getName(), new LinkedList<>(segment.getDataSources()), segment.getDiscoveryHeartbeatName(), segment.getDiscoveryTypeName());
        configuration.getDataSources().add(dataSourceRuleConfiguration);
    }
    
    private static String getName(final String ruleName, final String type) {
        return String.format("%s_%s", ruleName, type);
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
     * Convert database discovery type segment to database discovery heartbeat configuration.
     *
     * @param typeSegment database discovery type segments
     * @return database discovery type configuration
     */
    public static DatabaseDiscoveryRuleConfiguration convertDiscoveryType(final Collection<DatabaseDiscoveryTypeSegment> typeSegment) {
        final DatabaseDiscoveryRuleConfiguration result = new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>(), new LinkedHashMap<>());
        typeSegment.forEach(each -> result.getDiscoveryTypes().put(each.getDiscoveryTypeName(), 
                new ShardingSphereAlgorithmConfiguration(each.getAlgorithmSegment().getName(), each.getAlgorithmSegment().getProps())));
        return result;
    }
}
