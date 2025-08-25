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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.exception.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.spi.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredStrategyException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingDataSourceType;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.exception.ReadwriteSplittingRuleExceptionIdentifier;
import org.apache.shardingsphere.readwritesplitting.exception.actual.DuplicateReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule statement checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReadwriteSplittingRuleStatementChecker {
    
    /**
     * Check create readwrite-splitting rule statement.
     *
     * @param database database
     * @param segments segments
     * @param currentRuleConfig current rule config
     * @param ifNotExists rule if not exists
     */
    public static void checkCreation(final ShardingSphereDatabase database, final Collection<ReadwriteSplittingRuleSegment> segments,
                                     final ReadwriteSplittingRuleConfiguration currentRuleConfig, final boolean ifNotExists) {
        checkDuplicateRuleNames(database, segments, currentRuleConfig, ifNotExists);
        String databaseName = database.getName();
        checkDataSourcesExist(databaseName, segments, database);
        checkDuplicatedDataSourceNames(databaseName, segments, currentRuleConfig, true);
        checkTransactionalReadQueryStrategy(segments);
        checkLoadBalancers(segments);
    }
    
    /**
     * Check alter readwrite-splitting rule statement.
     *
     * @param database database
     * @param segments segments
     * @param currentRuleConfig current rule config
     */
    public static void checkAlteration(final ShardingSphereDatabase database, final Collection<ReadwriteSplittingRuleSegment> segments, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkDuplicateRuleNamesWithSelf(databaseName, segments);
        checkRuleNamesExist(segments, currentRuleConfig, databaseName);
        checkDataSourcesExist(databaseName, segments, database);
        checkDuplicatedDataSourceNames(databaseName, segments, currentRuleConfig, false);
        checkTransactionalReadQueryStrategy(segments);
        checkLoadBalancers(segments);
    }
    
    private static void checkRuleNamesExist(final Collection<ReadwriteSplittingRuleSegment> segments, final ReadwriteSplittingRuleConfiguration currentRuleConfig, final String databaseName) {
        Collection<String> requiredRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toList());
        Collection<String> currentRuleNames = currentRuleConfig.getDataSourceGroups().stream().map(ReadwriteSplittingDataSourceGroupRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = requiredRuleNames.stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(notExistedRuleNames, () -> new MissingRequiredRuleException("Readwrite-splitting", databaseName, notExistedRuleNames));
    }
    
    private static void checkDuplicateRuleNames(final ShardingSphereDatabase database,
                                                final Collection<ReadwriteSplittingRuleSegment> segments, final ReadwriteSplittingRuleConfiguration currentRuleConfig, final boolean ifNotExists) {
        checkDuplicateRuleNamesWithSelf(database.getName(), segments);
        checkDuplicateRuleNamesWithExistsDataSources(database, segments);
        if (!ifNotExists) {
            checkDuplicateRuleNamesWithRuleConfiguration(database.getName(), currentRuleConfig, segments);
        }
    }
    
    private static void checkDuplicateRuleNamesWithSelf(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> duplicatedRuleNames = getDuplicated(segments.stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toList()));
        ShardingSpherePreconditions.checkMustEmpty(duplicatedRuleNames, () -> new DuplicateRuleException("Readwrite-splitting", databaseName, duplicatedRuleNames));
    }
    
    private static Collection<String> getDuplicated(final Collection<String> required) {
        return required.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1L).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private static void checkDuplicateRuleNamesWithExistsDataSources(final ShardingSphereDatabase database, final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> currentRuleNames = new HashSet<>();
        ResourceMetaData resourceMetaData = database.getResourceMetaData();
        if (null != resourceMetaData && null != resourceMetaData.getStorageUnits()) {
            currentRuleNames.addAll(resourceMetaData.getStorageUnits().keySet());
        }
        currentRuleNames.addAll(getLogicDataSources(database));
        Collection<String> toBeCreatedRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(toBeCreatedRuleNames, () -> new InvalidRuleConfigurationException("Readwrite-splitting", toBeCreatedRuleNames,
                Collections.singleton(String.format("%s already exists in storage unit", toBeCreatedRuleNames))));
    }
    
    private static void checkDuplicateRuleNamesWithRuleConfiguration(final String databaseName, final ReadwriteSplittingRuleConfiguration currentRuleConfig,
                                                                     final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> currentRuleNames = new LinkedList<>();
        if (null != currentRuleConfig) {
            currentRuleNames.addAll(currentRuleConfig.getDataSourceGroups().stream().map(ReadwriteSplittingDataSourceGroupRuleConfiguration::getName).collect(Collectors.toList()));
        }
        Collection<String> toBeCreatedRuleNames = segments.stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(toBeCreatedRuleNames, () -> new DuplicateRuleException("Readwrite-splitting", databaseName, toBeCreatedRuleNames));
    }
    
    private static void checkDataSourcesExist(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments, final ShardingSphereDatabase database) {
        Collection<String> requiredDataSources = new LinkedHashSet<>();
        segments.forEach(each -> {
            requiredDataSources.add(each.getWriteDataSource());
            requiredDataSources.addAll(each.getReadDataSources());
        });
        Collection<String> notExistedDataSources = database.getResourceMetaData().getNotExistedDataSources(requiredDataSources);
        ShardingSpherePreconditions.checkMustEmpty(notExistedDataSources, () -> new MissingRequiredStorageUnitsException(databaseName, notExistedDataSources));
    }
    
    private static Collection<String> getLogicDataSources(final ShardingSphereDatabase database) {
        Collection<String> result = new LinkedHashSet<>();
        for (DataSourceMapperRuleAttribute each : database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)) {
            result.addAll(each.getDataSourceMapper().keySet());
        }
        return result;
    }
    
    private static void checkDuplicatedDataSourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                       final ReadwriteSplittingRuleConfiguration currentRuleConfig, final boolean isCreating) {
        Collection<String> existedWriteDataSourceNames = new HashSet<>();
        Collection<String> existedReadDataSourceNames = new HashSet<>();
        if (null != currentRuleConfig) {
            Collection<String> toBeAlteredRuleNames = isCreating ? Collections.emptySet() : getToBeAlteredRuleNames(segments);
            for (ReadwriteSplittingDataSourceGroupRuleConfiguration each : currentRuleConfig.getDataSourceGroups()) {
                if (toBeAlteredRuleNames.contains(each.getName())) {
                    continue;
                }
                existedWriteDataSourceNames.add(each.getWriteDataSourceName());
                existedReadDataSourceNames.addAll(each.getReadDataSourceNames());
            }
        }
        checkDuplicateWriteDataSourceNames(segments, databaseName, existedWriteDataSourceNames);
        checkDuplicateReadDataSourceNames(segments, databaseName, existedReadDataSourceNames);
    }
    
    private static Collection<String> getToBeAlteredRuleNames(final Collection<ReadwriteSplittingRuleSegment> segments) {
        return segments.stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private static void checkDuplicateWriteDataSourceNames(final Collection<ReadwriteSplittingRuleSegment> segments, final String databaseName,
                                                           final Collection<String> writeDataSourceNames) {
        for (ReadwriteSplittingRuleSegment each : segments) {
            if (Strings.isNullOrEmpty(each.getWriteDataSource())) {
                continue;
            }
            String writeDataSource = each.getWriteDataSource();
            ShardingSpherePreconditions.checkState(writeDataSourceNames.add(writeDataSource), () -> new DuplicateReadwriteSplittingActualDataSourceException(
                    ReadwriteSplittingDataSourceType.WRITE, writeDataSource, new ReadwriteSplittingRuleExceptionIdentifier(databaseName, "")));
        }
    }
    
    private static void checkDuplicateReadDataSourceNames(final Collection<ReadwriteSplittingRuleSegment> segments, final String databaseName,
                                                          final Collection<String> readDataSourceNames) {
        for (ReadwriteSplittingRuleSegment each : segments) {
            if (null != each.getReadDataSources()) {
                checkDuplicateReadDataSourceNames(each, databaseName, readDataSourceNames);
            }
        }
    }
    
    private static void checkDuplicateReadDataSourceNames(final ReadwriteSplittingRuleSegment segment, final String databaseName,
                                                          final Collection<String> readDataSourceNames) {
        for (String each : segment.getReadDataSources()) {
            ShardingSpherePreconditions.checkState(readDataSourceNames.add(each), () -> new DuplicateReadwriteSplittingActualDataSourceException(
                    ReadwriteSplittingDataSourceType.READ, each, new ReadwriteSplittingRuleExceptionIdentifier(databaseName, "")));
        }
    }
    
    private static void checkTransactionalReadQueryStrategy(final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> validStrategyNames = Arrays.stream(TransactionalReadQueryStrategy.values()).map(Enum::name).collect(Collectors.toSet());
        for (ReadwriteSplittingRuleSegment each : segments) {
            if (null != each.getTransactionalReadQueryStrategy()) {
                ShardingSpherePreconditions.checkContains(validStrategyNames, each.getTransactionalReadQueryStrategy().toUpperCase(),
                        () -> new MissingRequiredStrategyException("Transactional read query", Collections.singleton(each.getTransactionalReadQueryStrategy())));
            }
        }
    }
    
    private static void checkLoadBalancers(final Collection<ReadwriteSplittingRuleSegment> segments) {
        for (ReadwriteSplittingRuleSegment each : segments) {
            AlgorithmSegment loadBalancer = each.getLoadBalancer();
            if (null != loadBalancer) {
                TypedSPILoader.checkService(LoadBalanceAlgorithm.class, loadBalancer.getName(), loadBalancer.getProps());
                checkProperties(each);
            }
        }
    }
    
    private static void checkProperties(final ReadwriteSplittingRuleSegment each) {
        if ("WEIGHT".equalsIgnoreCase(each.getLoadBalancer().getName())) {
            ShardingSpherePreconditions.checkNotEmpty(each.getLoadBalancer().getProps(),
                    () -> new InvalidAlgorithmConfigurationException("Load balancer", each.getLoadBalancer().getName()));
            checkDataSource(each);
        }
    }
    
    private static void checkDataSource(final ReadwriteSplittingRuleSegment ruleSegment) {
        Collection<String> weightKeys = ruleSegment.getLoadBalancer().getProps().stringPropertyNames();
        weightKeys.forEach(each -> ShardingSpherePreconditions.checkContains(ruleSegment.getReadDataSources(), each,
                () -> new InvalidAlgorithmConfigurationException("Load balancer", ruleSegment.getLoadBalancer().getName(), String.format("Can not find read storage unit '%s'", each))));
        ruleSegment.getReadDataSources().forEach(each -> ShardingSpherePreconditions.checkContains(weightKeys, each,
                () -> new InvalidAlgorithmConfigurationException("Load balancer", ruleSegment.getLoadBalancer().getName(), String.format("Weight of '%s' is required", each))));
    }
}
