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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.checker;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.RuleExportEngine;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule statement checker.
 */
public final class ReadwriteSplittingRuleStatementChecker {
    
    /**
     * Check create readwrite splitting rule statement.
     *
     * @param database database
     * @param segments segments
     * @param currentRuleConfig current rule config
     */
    public static void checkCreation(final ShardingSphereDatabase database, final Collection<ReadwriteSplittingRuleSegment> segments, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkDuplicateRuleNames(databaseName, segments, currentRuleConfig, database.getResourceMetaData());
        checkResourcesExist(databaseName, segments, database);
        checkDuplicateResourceNames(databaseName, segments, currentRuleConfig, true);
        checkLoadBalancers(segments);
    }
    
    /**
     *  Check alter readwrite splitting rule statement.
     * 
     * @param database database
     * @param segments segments
     * @param currentRuleConfig current rule config
     */
    public static void checkAlteration(final ShardingSphereDatabase database, final Collection<ReadwriteSplittingRuleSegment> segments, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkRuleConfigurationExist(database, currentRuleConfig);
        checkDuplicateRuleNamesWithSelf(databaseName, segments);
        checkRuleNamesExist(segments, currentRuleConfig, databaseName);
        checkResourcesExist(databaseName, segments, database);
        checkDuplicateResourceNames(databaseName, segments, currentRuleConfig, false);
        checkLoadBalancers(segments);
    }
    
    /**
     *  Check current rule configuration exist.
     * 
     * @param database database
     * @param currentRuleConfig current rule config
     */
    public static void checkRuleConfigurationExist(final ShardingSphereDatabase database, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Readwrite splitting", database.getName()));
    }
    
    private static void checkRuleNamesExist(final Collection<ReadwriteSplittingRuleSegment> segments, final ReadwriteSplittingRuleConfiguration currentRuleConfig, final String databaseName) {
        Collection<String> requiredRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toList());
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistRuleNames = requiredRuleNames.stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistRuleNames.isEmpty(), () -> new MissingRequiredRuleException(databaseName, notExistRuleNames));
    }
    
    private static void checkDuplicateRuleNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResourceMetaData resourceMetaData) {
        checkDuplicateRuleNamesWithSelf(databaseName, segments);
        checkDuplicateRuleNamesWithResourceMetaData(segments, resourceMetaData);
        checkDuplicateRuleNamesWithRuleConfiguration(databaseName, segments, currentRuleConfig);
    }
    
    private static void checkDuplicateRuleNamesWithSelf(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> requiredRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toList());
        Collection<String> duplicateRuleNames = getDuplicate(requiredRuleNames);
        ShardingSpherePreconditions.checkState(duplicateRuleNames.isEmpty(), () -> new DuplicateRuleException("Readwrite splitting", databaseName, duplicateRuleNames));
    }
    
    private static Collection<String> getDuplicate(final Collection<String> require) {
        return require.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    
    private static void checkDuplicateRuleNamesWithResourceMetaData(final Collection<ReadwriteSplittingRuleSegment> segments, final ShardingSphereResourceMetaData resourceMetaData) {
        Collection<String> currentRuleNames = new LinkedList<>();
        if (null != resourceMetaData && null != resourceMetaData.getDataSources()) {
            currentRuleNames.addAll(resourceMetaData.getDataSources().keySet());
        }
        Collection<String> duplicateRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(duplicateRuleNames.isEmpty(), () -> new InvalidRuleConfigurationException("Readwrite splitting", duplicateRuleNames,
                Collections.singleton(String.format("%s already exists in resource", duplicateRuleNames))));
    }
    
    private static void checkDuplicateRuleNamesWithRuleConfiguration(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                                     final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = new LinkedList<>();
        if (null != currentRuleConfig) {
            currentRuleNames.addAll(currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList()));
        }
        Collection<String> duplicateRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(duplicateRuleNames.isEmpty(), () -> new DuplicateRuleException("Readwrite splitting", databaseName, duplicateRuleNames));
    }
    
    private static void checkResourcesExist(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments, final ShardingSphereDatabase database) {
        Collection<String> requireResources = new LinkedHashSet<>();
        Collection<String> requireDiscoverableResources = new LinkedHashSet<>();
        segments.forEach(each -> {
            if (Strings.isNullOrEmpty(each.getAutoAwareResource())) {
                requireResources.add(each.getWriteDataSource());
                requireResources.addAll(each.getReadDataSources());
            } else {
                requireDiscoverableResources.add(each.getAutoAwareResource());
            }
        });
        Collection<String> notExistResources = database.getResourceMetaData().getNotExistedResources(requireResources);
        ShardingSpherePreconditions.checkState(notExistResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistResources));
        Collection<String> logicResources = getLogicResources(database);
        Collection<String> notExistLogicResources = requireDiscoverableResources.stream().filter(each -> !logicResources.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistLogicResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistLogicResources));
    }
    
    @SuppressWarnings("unchecked")
    private static Collection<String> getLogicResources(final ShardingSphereDatabase database) {
        Collection<String> result = new LinkedHashSet<>();
        Optional<ExportableRule> exportableRule = database.getRuleMetaData().findRules(ExportableRule.class).stream()
                .filter(each -> new RuleExportEngine(each).containExportableKey(Collections.singletonList(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES))).findAny();
        exportableRule.ifPresent(optional -> {
            Map<String, Object> exportData = new RuleExportEngine(optional).export(Collections.singletonList(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES));
            Collection<String> logicResources = ((Map<String, String>) exportData.getOrDefault(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.emptyMap())).keySet();
            result.addAll(logicResources);
        });
        return result;
    }
    
    private static void checkDuplicateResourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                    final ReadwriteSplittingRuleConfiguration currentRuleConfig, final boolean isCreating) {
        Collection<String> existedWriteDataSourceNames = new HashSet<>();
        Collection<String> existedReadDataSourceNames = new HashSet<>();
        if (null != currentRuleConfig) {
            Collection<String> toBeAlteredRuleNames = isCreating ? Collections.emptySet() : getToBeAlteredRuleNames(segments);
            for (ReadwriteSplittingDataSourceRuleConfiguration each : currentRuleConfig.getDataSources()) {
                if (null != each.getStaticStrategy() && !toBeAlteredRuleNames.contains(each.getName())) {
                    existedWriteDataSourceNames.add(each.getStaticStrategy().getWriteDataSourceName());
                    existedReadDataSourceNames.addAll(each.getStaticStrategy().getReadDataSourceNames());
                }
            }
        }
        checkDuplicateWriteResourceNames(databaseName, segments, existedWriteDataSourceNames);
        checkDuplicateReadResourceNames(databaseName, segments, existedReadDataSourceNames);
    }
    
    private static Collection<String> getToBeAlteredRuleNames(final Collection<ReadwriteSplittingRuleSegment> segments) {
        return segments.stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private static void checkDuplicateWriteResourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments, final Collection<String> writeDataSourceNames) {
        for (final ReadwriteSplittingRuleSegment each : segments) {
            if (!Strings.isNullOrEmpty(each.getWriteDataSource())) {
                String writeDataSource = each.getWriteDataSource();
                ShardingSpherePreconditions.checkState(writeDataSourceNames.add(writeDataSource), () -> new InvalidRuleConfigurationException("Readwrite splitting", each.getName(),
                        String.format("Can not config duplicate write resource `%s` in database `%s`", writeDataSource, databaseName)));
            }
        }
    }
    
    private static void checkDuplicateReadResourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                        final Collection<String> readDataSourceNames) {
        for (ReadwriteSplittingRuleSegment each : segments) {
            if (null != each.getReadDataSources()) {
                for (String readDataSource : each.getReadDataSources()) {
                    ShardingSpherePreconditions.checkState(readDataSourceNames.add(readDataSource), () -> new InvalidRuleConfigurationException("Readwrite splitting", each.getName(),
                            String.format("Can not config duplicate read resource `%s` in database `%s`", readDataSource, databaseName)));
                }
            }
        }
    }
    
    private static void checkLoadBalancers(final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> notExistedLoadBalancers = segments.stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).filter(Objects::nonNull).distinct()
                .filter(each -> !ReadQueryLoadBalanceAlgorithmFactory.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistedLoadBalancers.isEmpty(), () -> new InvalidAlgorithmConfigurationException("Load balancers", notExistedLoadBalancers));
    }
}
