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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mask.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.distsql.handler.converter.MaskRuleStatementConverter;
import org.apache.shardingsphere.mask.distsql.segment.MaskColumnSegment;
import org.apache.shardingsphere.mask.distsql.segment.MaskRuleSegment;
import org.apache.shardingsphere.mask.distsql.statement.CreateMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Create mask rule executor.
 */
@Setter
public final class CreateMaskRuleExecutor implements DatabaseRuleCreateExecutor<CreateMaskRuleStatement, MaskRule, MaskRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private MaskRule rule;
    
    private boolean ifNotExists;
    
    @Override
    public void checkBeforeUpdate(final CreateMaskRuleStatement sqlStatement) {
        ifNotExists = sqlStatement.isIfNotExists();
        if (!ifNotExists) {
            checkDuplicatedRuleNames(sqlStatement);
        }
        checkAlgorithms(sqlStatement);
    }
    
    private void checkDuplicatedRuleNames(final CreateMaskRuleStatement sqlStatement) {
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement);
        ShardingSpherePreconditions.checkMustEmpty(duplicatedRuleNames, () -> new DuplicateRuleException("mask", database.getName(), duplicatedRuleNames));
    }
    
    private Collection<String> getDuplicatedRuleNames(final CreateMaskRuleStatement sqlStatement) {
        if (null == rule) {
            return Collections.emptyList();
        }
        Collection<String> currentRuleNames = rule.getConfiguration().getTables().stream().map(MaskTableRuleConfiguration::getName).collect(Collectors.toCollection(CaseInsensitiveSet::new));
        return sqlStatement.getRules().stream().map(MaskRuleSegment::getTableName).filter(currentRuleNames::contains).collect(Collectors.toList());
    }
    
    private void checkAlgorithms(final CreateMaskRuleStatement sqlStatement) {
        Collection<MaskColumnSegment> columns = new LinkedList<>();
        sqlStatement.getRules().forEach(each -> columns.addAll(each.getColumns()));
        columns.forEach(each -> TypedSPILoader.checkService(MaskAlgorithm.class, each.getAlgorithm().getName(), each.getAlgorithm().getProps()));
    }
    
    @Override
    public MaskRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateMaskRuleStatement sqlStatement) {
        return MaskRuleStatementConverter.convert(getToBeCreatedRuleSegments(sqlStatement));
    }
    
    private Collection<MaskRuleSegment> getToBeCreatedRuleSegments(final CreateMaskRuleStatement sqlStatement) {
        if (!ifNotExists) {
            return sqlStatement.getRules();
        }
        Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement);
        return sqlStatement.getRules().stream().filter(each -> !duplicatedRuleNames.contains(each.getTableName())).collect(Collectors.toList());
    }
    
    @Override
    public Class<MaskRule> getRuleClass() {
        return MaskRule.class;
    }
    
    @Override
    public Class<CreateMaskRuleStatement> getType() {
        return CreateMaskRuleStatement.class;
    }
}
