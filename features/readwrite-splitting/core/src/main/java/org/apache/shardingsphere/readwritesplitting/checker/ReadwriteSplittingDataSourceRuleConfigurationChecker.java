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

package org.apache.shardingsphere.readwritesplitting.checker;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.expr.entry.InlineExpressionParserFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingDataSourceType;
import org.apache.shardingsphere.readwritesplitting.exception.ReadwriteSplittingRuleExceptionIdentifier;
import org.apache.shardingsphere.readwritesplitting.exception.actual.DuplicateReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.actual.MissingRequiredReadwriteSplittingActualDataSourceException;
import org.apache.shardingsphere.readwritesplitting.exception.actual.ReadwriteSplittingActualDataSourceNotFoundException;
import org.apache.shardingsphere.readwritesplitting.exception.logic.MissingRequiredReadwriteSplittingDataSourceRuleNameException;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;

/**
 * Readwrite-splitting data source rule configuration checker.
 */
public final class ReadwriteSplittingDataSourceRuleConfigurationChecker {
    
    private final String databaseName;
    
    private final ReadwriteSplittingDataSourceGroupRuleConfiguration config;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ReadwriteSplittingRuleExceptionIdentifier exceptionIdentifier;
    
    public ReadwriteSplittingDataSourceRuleConfigurationChecker(final String databaseName,
                                                                final ReadwriteSplittingDataSourceGroupRuleConfiguration config, final Map<String, DataSource> dataSourceMap) {
        this.databaseName = databaseName;
        this.config = config;
        this.dataSourceMap = dataSourceMap;
        exceptionIdentifier = new ReadwriteSplittingRuleExceptionIdentifier(databaseName, config.getName());
    }
    
    /**
     * Check data sources.
     *
     * @param builtWriteDataSourceNames built write data source names
     * @param builtReadDataSourceNames built read data source names
     * @param builtRules built rules
     */
    public void check(final Collection<String> builtWriteDataSourceNames, final Collection<String> builtReadDataSourceNames, final Collection<ShardingSphereRule> builtRules) {
        ShardingSpherePreconditions.checkNotEmpty(config.getName(), () -> new MissingRequiredReadwriteSplittingDataSourceRuleNameException(databaseName));
        ShardingSpherePreconditions.checkNotEmpty(config.getWriteDataSourceName(),
                () -> new MissingRequiredReadwriteSplittingActualDataSourceException(ReadwriteSplittingDataSourceType.WRITE, exceptionIdentifier));
        ShardingSpherePreconditions.checkNotEmpty(config.getReadDataSourceNames(),
                () -> new MissingRequiredReadwriteSplittingActualDataSourceException(ReadwriteSplittingDataSourceType.READ, exceptionIdentifier));
        checkActualSourceNames(ReadwriteSplittingDataSourceType.WRITE, config.getWriteDataSourceName(), builtWriteDataSourceNames, builtRules);
        config.getReadDataSourceNames().forEach(each -> checkActualSourceNames(ReadwriteSplittingDataSourceType.READ, each, builtReadDataSourceNames, builtRules));
    }
    
    private void checkActualSourceNames(final ReadwriteSplittingDataSourceType type, final String actualDataSourceName,
                                        final Collection<String> builtActualDataSourceNames, final Collection<ShardingSphereRule> builtRules) {
        for (String each : InlineExpressionParserFactory.newInstance(actualDataSourceName).splitAndEvaluate()) {
            ShardingSpherePreconditions.checkState(dataSourceMap.containsKey(each) || containsInOtherRules(each, builtRules),
                    () -> new ReadwriteSplittingActualDataSourceNotFoundException(type, each, exceptionIdentifier));
            ShardingSpherePreconditions.checkState(builtActualDataSourceNames.add(each), () -> new DuplicateReadwriteSplittingActualDataSourceException(type, each, exceptionIdentifier));
        }
    }
    
    private boolean containsInOtherRules(final String dataSourceName, final Collection<ShardingSphereRule> builtRules) {
        return builtRules.stream().map(each -> each.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class))
                .anyMatch(optional -> optional.isPresent() && optional.get().getDataSourceMapper().containsKey(dataSourceName));
    }
}
