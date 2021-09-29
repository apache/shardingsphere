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

package org.apache.shardingsphere.shadow.distsql.handler.supporter;

import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Shadow rule statement supporter.
 */
public final class ShadowRuleStatementSupporter {
    
    /**
     * Get rule names from the configuration.
     *
     * @param configuration configuration
     * @return rule names
     */
    public static List<String> getRuleNames(final ShadowRuleConfiguration configuration) {
        if (null == configuration) {
            return Collections.emptyList();
        }
        return new ArrayList<>(configuration.getDataSources().keySet());
    }
    
    /**
     * Get rule names from the rules.
     *
     * @param rules rules
     * @return rule names
     */
    public static List<String> getRuleNames(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().map(ShadowRuleSegment::getRuleName).collect(Collectors.toList());
    }
    
    /**
     * Get table names from the configuration.
     *
     * @param configuration configuration
     * @return table names
     */
    public static List<String> getTableNames(final ShadowRuleConfiguration configuration) {
        if (null == configuration) {
            return Collections.emptyList();
        }
        return new ArrayList<>(configuration.getTables().keySet());
    }
    
    /**
     * Get the table names from the rules.
     *
     * @param rules rules
     * @return table names
     */
    public static List<String> getTableNames(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().flatMap(each -> each.getShadowTableRules().keySet().stream()).collect(Collectors.toList());
    }
    
    /**
     * Get the resource names from the rules.
     *
     * @param rules rules
     * @return resource names
     */
    public static List<String> getResourceNames(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().map(each -> Arrays.asList(each.getSource(), each.getShadow())).flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    /**
     * Get the algorithm names from the configuration.
     *
     * @param configuration configuration
     * @return algorithm names
     */
    public static List<String> getAlgorithmNames(final ShadowRuleConfiguration configuration) {
        if (null == configuration) {
            return Collections.emptyList();
        }
        return new ArrayList<>(configuration.getShadowAlgorithms().keySet());
    }
    
    /**
     * Get the algorithm names from the rules.
     *
     * @param rules configuration
     * @return algorithm names
     */
    public static List<String> getAlgorithmNames(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream).map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList());
    }
    
    /**
     * Get the algorithm segments from the rules.
     *
     * @param rules configuration
     * @return algorithm segments
     */
    public static List<ShadowAlgorithmSegment> getShadowAlgorithmSegment(final Collection<ShadowRuleSegment> rules) {
        return rules.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    /**
     * Merge configuration.
     *
     * @param existingConfiguration already existing configuration
     * @param newConfiguration new shadow table configuration
     * @return shadow table configuration
     */
    public static ShadowTableConfiguration mergeConfiguration(final ShadowTableConfiguration existingConfiguration, final ShadowTableConfiguration newConfiguration) {
        existingConfiguration.getDataSourceNames().addAll(newConfiguration.getDataSourceNames());
        existingConfiguration.getShadowAlgorithmNames().addAll(newConfiguration.getShadowAlgorithmNames());
        return existingConfiguration;
    }
}
