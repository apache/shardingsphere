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

package org.apache.shardingsphere.shadow.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.statement.DropShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop shadow rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShadowRule.class)
@Setter
public final class DropShadowRuleExecutor implements DatabaseRuleDropExecutor<DropShadowRuleStatement, ShadowRule, ShadowRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShadowRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropShadowRuleStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkRuleExisted(sqlStatement);
        }
    }
    
    private void checkRuleExisted(final DropShadowRuleStatement sqlStatement) {
        ShadowRuleStatementChecker.checkExisted(sqlStatement.getNames(), getDataSourceNames(),
                notExistedRuleNames -> new MissingRequiredRuleException("Shadow", database.getName(), notExistedRuleNames));
    }
    
    private Collection<String> getDataSourceNames() {
        return rule.getConfiguration().getDataSources().stream().map(ShadowDataSourceConfiguration::getName).collect(Collectors.toList());
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShadowRuleStatement sqlStatement) {
        return !Collections.disjoint(sqlStatement.getNames(), getDataSourceNames());
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeDroppedRuleConfiguration(final DropShadowRuleStatement sqlStatement) {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getDataSources().add(new ShadowDataSourceConfiguration(each, null, null));
            dropRule(each);
        }
        rule.getConfiguration().getTables().forEach((key, value) -> value.getDataSourceNames().removeIf(sqlStatement.getNames()::contains));
        for (Entry<String, ShadowTableConfiguration> each : rule.getConfiguration().getTables().entrySet()) {
            if (each.getValue().getDataSourceNames().isEmpty()) {
                result.getTables().put(each.getKey(), each.getValue());
            }
        }
        rule.getConfiguration().getTables().entrySet().removeIf(each -> each.getValue().getDataSourceNames().isEmpty());
        UnusedAlgorithmFinder.findUnusedShadowAlgorithm(rule.getConfiguration()).forEach(each -> result.getShadowAlgorithms().put(each, rule.getConfiguration().getShadowAlgorithms().get(each)));
        return result;
    }
    
    @Override
    public ShadowRuleConfiguration buildToBeAlteredRuleConfiguration(final DropShadowRuleStatement sqlStatement) {
        Map<String, ShadowTableConfiguration> tables = new LinkedHashMap<>();
        Collection<String> toBeDroppedDataSourceNames = sqlStatement.getNames();
        for (Entry<String, ShadowTableConfiguration> each : rule.getConfiguration().getTables().entrySet()) {
            if (!toBeDroppedDataSourceNames.containsAll(each.getValue().getDataSourceNames())) {
                List<String> currentDataSources = new LinkedList<>(each.getValue().getDataSourceNames());
                currentDataSources.removeAll(toBeDroppedDataSourceNames);
                tables.put(each.getKey(), new ShadowTableConfiguration(currentDataSources, each.getValue().getShadowAlgorithmNames()));
            }
        }
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.setTables(tables);
        return result;
    }
    
    private void dropRule(final String ruleName) {
        Optional<ShadowDataSourceConfiguration> dataSourceRuleConfig = rule.getConfiguration().getDataSources().stream().filter(each -> ruleName.equals(each.getName())).findAny();
        dataSourceRuleConfig.ifPresent(optional -> rule.getConfiguration().getDataSources().remove(optional));
    }
    
    @Override
    public Class<ShadowRule> getRuleClass() {
        return ShadowRule.class;
    }
    
    @Override
    public Class<DropShadowRuleStatement> getType() {
        return DropShadowRuleStatement.class;
    }
}
