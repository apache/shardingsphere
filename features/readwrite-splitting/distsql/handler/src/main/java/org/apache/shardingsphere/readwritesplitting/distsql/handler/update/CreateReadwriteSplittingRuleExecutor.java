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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleCreateExecutor;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.checker.ReadwriteSplittingRuleStatementChecker;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Create readwrite-splitting rule executor.
 */
@Setter
public final class CreateReadwriteSplittingRuleExecutor implements DatabaseRuleCreateExecutor<CreateReadwriteSplittingRuleStatement, ReadwriteSplittingRule, ReadwriteSplittingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ReadwriteSplittingRule rule;
    
    @Override
    public void checkBeforeUpdate(final CreateReadwriteSplittingRuleStatement sqlStatement) {
        ReadwriteSplittingRuleStatementChecker.checkCreation(database, sqlStatement.getRules(), null == rule ? null : rule.getConfiguration(), sqlStatement.isIfNotExists());
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateReadwriteSplittingRuleStatement sqlStatement) {
        Collection<ReadwriteSplittingRuleSegment> segments = sqlStatement.getRules();
        if (sqlStatement.isIfNotExists()) {
            Collection<String> duplicatedRuleNames = getDuplicatedRuleNames(sqlStatement.getRules());
            segments.removeIf(each -> duplicatedRuleNames.contains(each.getName()));
        }
        return ReadwriteSplittingRuleStatementConverter.convert(segments);
    }
    
    private Collection<String> getDuplicatedRuleNames(final Collection<ReadwriteSplittingRuleSegment> segments) {
        Collection<String> currentRuleNames = new LinkedList<>();
        if (null != rule) {
            currentRuleNames.addAll(rule.getConfiguration().getDataSourceGroups().stream().map(ReadwriteSplittingDataSourceGroupRuleConfiguration::getName).collect(Collectors.toList()));
        }
        return segments.stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getRuleClass() {
        return ReadwriteSplittingRule.class;
    }
    
    @Override
    public Class<CreateReadwriteSplittingRuleStatement> getType() {
        return CreateReadwriteSplittingRuleStatement.class;
    }
}
