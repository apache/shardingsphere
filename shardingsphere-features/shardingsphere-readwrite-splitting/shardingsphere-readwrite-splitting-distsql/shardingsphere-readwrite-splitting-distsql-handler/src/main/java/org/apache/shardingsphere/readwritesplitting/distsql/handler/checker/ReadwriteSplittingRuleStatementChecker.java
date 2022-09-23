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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule statement checker.
 */
public final class ReadwriteSplittingRuleStatementChecker {
    
    /**
     * Check duplicate resource names for readwrite-splitting rule statement.
     * 
     * @param databaseName database name
     * @param segments segments
     * @param currentRuleConfig current rule config
     * @param isCreating whether is creating
     * @throws DistSQLException DistSQL Exception
     */
    public static void checkDuplicateResourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                   final ReadwriteSplittingRuleConfiguration currentRuleConfig, final boolean isCreating) throws DistSQLException {
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
    
    private static void checkDuplicateWriteResourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                         final Collection<String> writeDataSourceNames) throws DistSQLException {
        for (final ReadwriteSplittingRuleSegment each : segments) {
            if (!Strings.isNullOrEmpty(each.getWriteDataSource())) {
                String writeDataSource = each.getWriteDataSource();
                ShardingSpherePreconditions.checkState(writeDataSourceNames.add(writeDataSource), () -> new InvalidRuleConfigurationException("readwrite splitting", each.getName(),
                        String.format("Can not config duplicate write resource `%s` in database `%s`", writeDataSource, databaseName)));
            }
        }
    }
    
    private static void checkDuplicateReadResourceNames(final String databaseName, final Collection<ReadwriteSplittingRuleSegment> segments,
                                                        final Collection<String> readDataSourceNames) throws DistSQLException {
        for (final ReadwriteSplittingRuleSegment each : segments) {
            if (null != each.getReadDataSources()) {
                for (final String readDataSource : each.getReadDataSources()) {
                    ShardingSpherePreconditions.checkState(readDataSourceNames.add(readDataSource), () -> new InvalidRuleConfigurationException("readwrite splitting", each.getName(),
                            String.format("Can not config duplicate read resource `%s` in database `%s`", readDataSource, databaseName)));
                }
            }
        }
    }
}
