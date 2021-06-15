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

package org.apache.shardingsphere.dbdiscovery.yaml.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.yaml.config.rule.YamlDatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;

import java.util.Collection;

/**
 * Database discovery rule statement converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseDiscoveryRuleStatementConverter {
    
    /**
     * Convert database discovery rule segment to YAML database discovery rule configuration.
     *
     * @param databaseDiscoveryRuleSegments collection of database discovery rule segments
     * @return YAML database discovery rule configuration
     */
    public static YamlDatabaseDiscoveryRuleConfiguration convert(final Collection<DatabaseDiscoveryRuleSegment> databaseDiscoveryRuleSegments) {
        YamlDatabaseDiscoveryRuleConfiguration result = new YamlDatabaseDiscoveryRuleConfiguration();
        for (DatabaseDiscoveryRuleSegment each : databaseDiscoveryRuleSegments) {
            String databaseDiscoveryType = getDatabaseDiscoveryType(each.getName(), each.getDiscoveryTypeName());
            result.getDataSources().put(each.getName(), buildDataSourceRuleConfiguration(databaseDiscoveryType, each));
            result.getDiscoveryTypes().put(databaseDiscoveryType, buildDiscoveryType(each));
        }
        return result;
    }
    
    private static YamlDatabaseDiscoveryDataSourceRuleConfiguration buildDataSourceRuleConfiguration(final String databaseDiscoveryType,
                                                                                                     final DatabaseDiscoveryRuleSegment databaseDiscoveryRuleSegment) {
        YamlDatabaseDiscoveryDataSourceRuleConfiguration result = new YamlDatabaseDiscoveryDataSourceRuleConfiguration();
        result.getDataSourceNames().addAll(databaseDiscoveryRuleSegment.getDataSources());
        result.setDiscoveryTypeName(databaseDiscoveryType);
        result.setProps(databaseDiscoveryRuleSegment.getProps());
        return result;
    }
    
    private static YamlShardingSphereAlgorithmConfiguration buildDiscoveryType(final DatabaseDiscoveryRuleSegment databaseDiscoveryRuleSegment) {
        YamlShardingSphereAlgorithmConfiguration result = new YamlShardingSphereAlgorithmConfiguration();
        result.setType(databaseDiscoveryRuleSegment.getDiscoveryTypeName());
        result.setProps(databaseDiscoveryRuleSegment.getProps());
        return result;
    }
    
    private static String getDatabaseDiscoveryType(final String ruleName, final String databaseDiscoveryType) {
        return String.format("%s_%s", ruleName, databaseDiscoveryType);
    }
}
