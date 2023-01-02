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
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.RuleExportEngine;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.spi.ReadQueryLoadBalanceAlgorithm;

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
     * @param ifNotExists rule if not exists
     */
    public static void checkCreation(final ShardingSphereDatabase database, final Collection<ReadwriteSplittingRuleSegment> segments,
                                     final ReadwriteSplittingRuleConfiguration currentRuleConfig, final boolean ifNotExists) {
        String databaseName = database.getName();
        checkDuplicateRuleNames(databaseName, segments, currentRuleConfig, database.getResourceMetaData(), ifNotExists);
        checkDataSourcesExist(databaseName, segments, database);
        checkDuplicatedDataSourceNames(databaseName, segments, currentRuleConfig, true);
        checkLoadBalancers(segments);
    }
    
    /**
     * Check alter readwrite splitting rule statement.
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
        checkDataSourcesExist(databaseName, segments, database);
        checkDuplicatedDataSourceNames(databaseName, segments, currentRuleConfig, false);
        checkLoadBalancers(segments);
    }
    
    /**
     * Check current rule configuration exist.
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
        Collection<String> notExistedRuleNames = requiredRuleNames.stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistedRuleNames.isEmpty(), () -> new MissingRequiredRuleException(databaseName, notExistedRuleNames));
    }
    
    private static void checkDuplicateRuleNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments, final ReadwriteSplittingRuleConfiguration currentRuleConfig,
                                                final ShardingSphereResourceMetaData resourceMetaData, final boolean ifNotExists) {
        checkDuplicateRuleNamesWithSelf(databaseName, segments);
        checkDuplicateRuleNamesWithResourceMetaData(resourceMetaData, segments);
        if (!ifNotExists) {
            checkDuplicateRuleNamesWithRuleConfiguration(databaseName, currentRuleConfig, segments);
        }
    }
    
    private static void checkDuplicateRuleNamesWithSelf(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> duplicatedRuleNames = getDuplicated(segments.stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toList()));
        ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("Readwrite splitting", databaseName, duplicatedRuleNames));
    }
    
    private static Collection<String> getDuplicated(final Collection<String> required) {
        return required.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
    }
    
    private static void checkDuplicateRuleNamesWithResourceMetaData(final ShardingSphereResourceMetaData resourceMetaData, final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> currentRuleNames = new LinkedList<>();
        if (null != resourceMetaData && null != resourceMetaData.getDataSources()) {
            currentRuleNames.addAll(resourceMetaData.getDataSources().keySet());
        }
        Collection<String> toBeCreatedRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(toBeCreatedRuleNames.isEmpty(), () -> new InvalidRuleConfigurationException("Readwrite splitting", toBeCreatedRuleNames,
                Collections.singleton(String.format("%s already exists in storage unit", toBeCreatedRuleNames))));
    }
    
    private static void checkDuplicateRuleNamesWithRuleConfiguration(final String databaseName, final ReadwriteSplittingRuleConfiguration currentRuleConfig,
                                                                     final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> currentRuleNames = new LinkedList<>();
        if (null != currentRuleConfig) {
            currentRuleNames.addAll(currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList()));
        }
        Collection<String> toBeCreatedRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(toBeCreatedRuleNames.isEmpty(), () -> new DuplicateRuleException("Readwrite splitting", databaseName, toBeCreatedRuleNames));
    }
    
    private static void checkDataSourcesExist(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments, final ShardingSphereDatabase database) {
        Collection<String> requiredDataSources = new LinkedHashSet<>();
        Collection<String> requiredLogicalDataSources = new LinkedHashSet<>();
        segments.forEach(each -> {
            if (Strings.isNullOrEmpty(each.getAutoAwareResource())) {
                requiredDataSources.add(each.getWriteDataSource());
                requiredDataSources.addAll(each.getReadDataSources());
            } else {
                requiredLogicalDataSources.add(each.getAutoAwareResource());
            }
        });
        Collection<String> notExistedDataSources = database.getResourceMetaData().getNotExistedResources(requiredDataSources);
        ShardingSpherePreconditions.checkState(notExistedDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedDataSources));
        Collection<String> logicalDataSources = getLogicDataSources(database);
        Collection<String> notExistedLogicalDataSources = requiredLogicalDataSources.stream().filter(each -> !logicalDataSources.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistedLogicalDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(databaseName, notExistedLogicalDataSources));
    }
    
    @SuppressWarnings("unchecked")
    private static Collection<String> getLogicDataSources(final ShardingSphereDatabase database) {
        Collection<String> result = new LinkedHashSet<>();
        Optional<ExportableRule> exportableRule = database.getRuleMetaData().findRules(ExportableRule.class).stream()
                .filter(each -> new RuleExportEngine(each).containExportableKey(Collections.singletonList(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES))).findAny();
        exportableRule.ifPresent(optional -> {
            Map<String, Object> exportData = new RuleExportEngine(optional).export(Collections.singletonList(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES));
            Collection<String> logicalDataSources = ((Map<String, String>) exportData.getOrDefault(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.emptyMap())).keySet();
            result.addAll(logicalDataSources);
        });
        return result;
    }
    
    private static void checkDuplicatedDataSourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                       final ReadwriteSplittingRuleConfiguration currentRuleConfig, final boolean isCreating) {
        Collection<String> existedWriteDataSourceNames = new HashSet<>();
        Collection<String> existedReadDataSourceNames = new HashSet<>();
        if (null != currentRuleConfig) {
            Collection<String> toBeAlteredRuleNames = isCreating ? Collections.emptySet() : getToBeAlteredRuleNames(segments);
            currentRuleConfig.getDataSources().forEach(each -> {
                if (null != each.getStaticStrategy() && !toBeAlteredRuleNames.contains(each.getName())) {
                    existedWriteDataSourceNames.add(each.getStaticStrategy().getWriteDataSourceName());
                    existedReadDataSourceNames.addAll(each.getStaticStrategy().getReadDataSourceNames());
                }
            });
        }
        checkDuplicateWriteDataSourceNames(databaseName, segments, existedWriteDataSourceNames);
        checkDuplicateReadDataSourceNames(databaseName, segments, existedReadDataSourceNames);
    }
    
    private static Collection<String> getToBeAlteredRuleNames(final Collection<ReadwriteSplittingRuleSegment> segments) {
        return segments.stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private static void checkDuplicateWriteDataSourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments, final Collection<String> writeDataSourceNames) {
        segments.forEach(each -> {
            if (!Strings.isNullOrEmpty(each.getWriteDataSource())) {
                String writeDataSource = each.getWriteDataSource();
                ShardingSpherePreconditions.checkState(writeDataSourceNames.add(writeDataSource), () -> new InvalidRuleConfigurationException("Readwrite splitting", each.getName(),
                        String.format("Can not config duplicate write storage unit `%s` in database `%s`", writeDataSource, databaseName)));
            }
        });
    }
    
    private static void checkDuplicateReadDataSourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments, final Collection<String> readDataSourceNames) {
        segments.forEach(each -> {
            if (null != each.getReadDataSources()) {
                each.getReadDataSources().forEach(readDataSource -> {
                    ShardingSpherePreconditions.checkState(readDataSourceNames.add(readDataSource), () -> new InvalidRuleConfigurationException("Readwrite splitting", each.getName(),
                            String.format("Can not config duplicate read storage unit `%s` in database `%s`", readDataSource, databaseName)));
                });
            }
        });
    }
    
    private static void checkLoadBalancers(final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> notExistedLoadBalancers = segments.stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).filter(Objects::nonNull).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReadQueryLoadBalanceAlgorithm.class, each).isPresent()).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistedLoadBalancers.isEmpty(), () -> new InvalidAlgorithmConfigurationException("Load balancers", notExistedLoadBalancers));
    }
}
