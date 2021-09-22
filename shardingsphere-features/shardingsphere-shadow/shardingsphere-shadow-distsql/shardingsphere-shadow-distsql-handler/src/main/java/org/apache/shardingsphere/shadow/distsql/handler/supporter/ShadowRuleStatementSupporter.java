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
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
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
 * Show rule statement supporter.
 */
public final class ShadowRuleStatementSupporter {
    
    /**
     * Get the rule name from the configuration.
     * @param configuration configuration
     * @return the value corresponding to the rule name
     */
    public static List<String> getRuleName(final ShadowRuleConfiguration configuration) {
        if (null == configuration) {
            return Collections.emptyList();
        }
        return new ArrayList<>(configuration.getDataSources().keySet());
    }
    
    /**
     * Get the rule name from the rules.
     * @param rules rules
     * @return the value corresponding to the rule name
     */
    public static List<String> getRuleName(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().map(ShadowRuleSegment::getRuleName).collect(Collectors.toList());
    }
    
    /**
     * Get the table from the configuration.
     * @param configuration configuration
     * @return the value corresponding to the table
     */
    public static List<String> getTable(final ShadowRuleConfiguration configuration) {
        if (null == configuration) {
            return Collections.emptyList();
        }
        return new ArrayList<>(configuration.getTables().keySet());
    }
    
    /**
     * Get the table from the rules.
     * @param rules rules
     * @return the value corresponding to the table
     */
    public static List<String> getTable(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().flatMap(each -> each.getShadowTableRules().keySet().stream()).collect(Collectors.toList());
    }
    
    /**
     * Get the source resource from the configuration.
     * @param configuration configuration
     * @return the value corresponding to the source resource
     */
    public static List<String> getSourceResource(final ShadowRuleConfiguration configuration) {
        if (null == configuration) {
            return Collections.emptyList();
        }
        return configuration.getDataSources().values().stream().map(ShadowDataSourceConfiguration::getSourceDataSourceName).collect(Collectors.toList());
    }
    
    /**
     * Get the source resource from the rules.
     * @param rules rules
     * @return the value corresponding to the source resource
     */
    public static List<String> getSourceResource(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().map(ShadowRuleSegment::getSource).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    /**
     * Get the resource from the rules.
     * @param rules rules
     * @return the value corresponding to the resource
     */
    public static List<String> getResource(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().map(each -> Arrays.asList(each.getSource(), each.getShadow())).flatMap(Collection::stream).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    /**
     * Get the algorithms from the configuration.
     * @param configuration configuration
     * @return the value corresponding to the algorithm
     */
    public static List<String> getAlgorithm(final ShadowRuleConfiguration configuration) {
        if (null == configuration) {
            return Collections.emptyList();
        }
        return new ArrayList<>(configuration.getShadowAlgorithms().keySet());
    }
    
    /**
     * Get the algorithms from the rules.
     * @param rules configuration
     * @return the value corresponding to the algorithm
     */
    public static List<String> getAlgorithm(final Collection<ShadowRuleSegment> rules) {
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        return rules.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream).map(ShadowAlgorithmSegment::getAlgorithmName).collect(Collectors.toList());
    }
    
    /**
     * Get the algorithm segment from the rules.
     * @param rules configuration
     * @return the value corresponding to the algorithm segment
     */
    public static List<ShadowAlgorithmSegment> getShadowAlgorithmSegment(final Collection<ShadowRuleSegment> rules) {
        return rules.stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
