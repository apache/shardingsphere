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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.AlgorithmInUsedException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.checker.ShadowRuleStatementChecker;
import org.apache.shardingsphere.shadow.distsql.handler.converter.ShadowRuleStatementConverter;
import org.apache.shardingsphere.shadow.distsql.handler.supporter.ShadowRuleStatementSupporter;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowAlgorithmSegment;
import org.apache.shardingsphere.shadow.distsql.parser.segment.ShadowRuleSegment;
import org.apache.shardingsphere.shadow.distsql.parser.statement.AlterShadowRuleStatement;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Map;

/**
 * Alter shadow rule statement updater.
 */
public final class AlterShadowRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterShadowRuleStatement, ShadowRuleConfiguration> {
    
    @Override
    public ShadowRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShadowRuleStatement sqlStatement) {
        return ShadowRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShadowRuleConfiguration currentRuleConfig, final ShadowRuleConfiguration toBeAlteredRuleConfig) {
        updateDataSources(currentRuleConfig, toBeAlteredRuleConfig.getDataSources());
        updateTables(currentRuleConfig.getTables(), toBeAlteredRuleConfig.getTables());
        currentRuleConfig.getShadowAlgorithms().putAll(toBeAlteredRuleConfig.getShadowAlgorithms());
    }
    
    private void updateDataSources(final ShadowRuleConfiguration currentRuleConfig, final Collection<ShadowDataSourceConfiguration> toBeAlteredDataSources) {
        currentRuleConfig.getDataSources().addAll(toBeAlteredDataSources);
    }
    
    private void updateTables(final Map<String, ShadowTableConfiguration> currentTables, final Map<String, ShadowTableConfiguration> toBeAlteredTables) {
        toBeAlteredTables.forEach(currentTables::replace);
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterShadowRuleStatement sqlStatement, final ShadowRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        ShadowRuleStatementChecker.checkRuleConfigurationExists(databaseName, currentRuleConfig);
        checkRuleNames(databaseName, sqlStatement.getRules(), currentRuleConfig);
        checkStorageUnits(database, sqlStatement.getRules());
        checkAlgorithms(databaseName, sqlStatement.getRules());
        checkAlgorithmType(sqlStatement);
    }
    
    private void checkRuleNames(final String databaseName, final Collection<ShadowRuleSegment> segments, final ShadowRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = ShadowRuleStatementSupporter.getRuleNames(currentRuleConfig);
        Collection<String> requiredRuleNames = ShadowRuleStatementSupporter.getRuleNames(segments);
        ShadowRuleStatementChecker.checkDuplicated(requiredRuleNames, duplicated -> new DuplicateRuleException("shadow", databaseName, duplicated));
        ShadowRuleStatementChecker.checkExisted(requiredRuleNames, currentRuleNames, notExistedRules -> new MissingRequiredRuleException("Shadow", notExistedRules));
    }
    
    private void checkStorageUnits(final ShardingSphereDatabase database, final Collection<ShadowRuleSegment> segments) {
        ShadowRuleStatementChecker.checkStorageUnitsExist(ShadowRuleStatementSupporter.getStorageUnitNames(segments), database);
    }
    
    private void checkAlgorithmType(final AlterShadowRuleStatement sqlStatement) {
        sqlStatement.getRules().stream().flatMap(each -> each.getShadowTableRules().values().stream()).flatMap(Collection::stream)
                .map(ShadowAlgorithmSegment::getAlgorithmSegment).forEach(each -> TypedSPILoader.checkService(ShadowAlgorithm.class, each.getName(), each.getProps()));
    }
    
    private void checkAlgorithms(final String databaseName, final Collection<ShadowRuleSegment> segments) {
        Collection<String> requiredAlgorithms = ShadowRuleStatementSupporter.getAlgorithmNames(segments);
        ShadowRuleStatementChecker.checkDuplicated(requiredAlgorithms, duplicated -> new AlgorithmInUsedException("Shadow", databaseName, duplicated));
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getRuleConfigurationClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShadowRuleStatement.class.getName();
    }
}
