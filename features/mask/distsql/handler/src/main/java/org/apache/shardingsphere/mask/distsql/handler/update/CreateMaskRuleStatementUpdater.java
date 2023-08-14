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

package org.apache.shardingsphere.mask.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskColumnRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.handler.converter.MaskRuleStatementConverter;
import org.apache.shardingsphere.mask.distsql.parser.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.parser.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.parser.statement.CreateMaskRuleStatement;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Create mask rule statement updater.
 */
public final class CreateMaskRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateMaskRuleStatement, MaskRuleConfiguration> {
    
    private boolean ifNotExists;
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateMaskRuleStatement sqlStatement, final MaskRuleConfiguration currentRuleConfig) {
        ifNotExists = sqlStatement.isIfNotExists();
        if (!ifNotExists) {
            checkDuplicatedRuleNames(database.getName(), sqlStatement, currentRuleConfig);
        }
        checkAlgorithms(sqlStatement);
    }
    
    private void checkDuplicatedRuleNames(final String databaseName, final CreateMaskRuleStatement sqlStatement, final MaskRuleConfiguration currentRuleConfig) {
        if (null != currentRuleConfig) {
            Collection<String> currentRuleNames = currentRuleConfig.getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toList());
            Collection<String> duplicatedRuleNames = sqlStatement.getRules().stream().map(MaskRuleSegment::getTableName).filter(currentRuleNames::contains).collect(Collectors.toList());
            ShardingSpherePreconditions.checkState(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("mask", databaseName, duplicatedRuleNames));
        }
    }
    
    private void checkAlgorithms(final CreateMaskRuleStatement sqlStatement) {
        Collection<MaskColumnSegment> columns = new LinkedList<>();
        sqlStatement.getRules().forEach(each -> columns.addAll(each.getColumns()));
        columns.forEach(each -> TypedSPILoader.checkService(MaskAlgorithm.class, each.getAlgorithm().getName(), each.getAlgorithm().getProps()));
    }
    
    @Override
    public Class<MaskRuleConfiguration> getRuleConfigurationClass() {
        return MaskRuleConfiguration.class;
    }
    
    @Override
    public MaskRuleConfiguration buildToBeCreatedRuleConfiguration(final MaskRuleConfiguration currentRuleConfig, final CreateMaskRuleStatement sqlStatement) {
        return MaskRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final MaskRuleConfiguration currentRuleConfig, final MaskRuleConfiguration toBeCreatedRuleConfig) {
        if (ifNotExists) {
            removeDuplicatedRules(currentRuleConfig, toBeCreatedRuleConfig);
        }
        if (toBeCreatedRuleConfig.getTables().isEmpty()) {
            return;
        }
        currentRuleConfig.getTables().addAll(toBeCreatedRuleConfig.getTables());
        currentRuleConfig.getMaskAlgorithms().putAll(toBeCreatedRuleConfig.getMaskAlgorithms());
    }
    
    private void removeDuplicatedRules(final MaskRuleConfiguration currentRuleConfig, final MaskRuleConfiguration toBeCreatedRuleConfig) {
        Collection<String> currentTables = new LinkedList<>();
        Collection<String> toBeRemovedAlgorithms = new LinkedList<>();
        Collection<String> toBeRemovedTables = new LinkedList<>();
        currentRuleConfig.getTables().forEach(each -> currentTables.add(each.getName()));
        toBeCreatedRuleConfig.getTables().forEach(each -> {
            if (currentTables.contains(each.getName())) {
                toBeRemovedAlgorithms.addAll(each.getColumns().stream().map(MaskColumnRuleConfiguration::getMaskAlgorithm).collect(Collectors.toList()));
                toBeRemovedTables.add(each.getName());
            }
        });
        toBeCreatedRuleConfig.getTables().removeIf(each -> toBeRemovedTables.contains(each.getName()));
        toBeCreatedRuleConfig.getMaskAlgorithms().keySet().removeIf(toBeRemovedAlgorithms::contains);
    }
    
    @Override
    public Class<CreateMaskRuleStatement> getType() {
        return CreateMaskRuleStatement.class;
    }
}
