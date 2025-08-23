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

package org.apache.shardingsphere.sharding.checker.sql.ddl;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterViewStatementContext;
import org.apache.shardingsphere.infra.checker.SupportedSQLChecker;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.checker.sql.util.ShardingSupportedCheckUtils;
import org.apache.shardingsphere.sharding.exception.metadata.EngagedViewException;
import org.apache.shardingsphere.sharding.exception.syntax.RenamedViewWithoutSameConfigurationException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.view.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Arrays;
import java.util.Optional;

/**
 * Alter view supported checker for sharding.
 */
public final class ShardingAlterViewSupportedChecker implements SupportedSQLChecker<AlterViewStatementContext, ShardingRule> {
    
    @Override
    public boolean isCheck(final SQLStatementContext sqlStatementContext) {
        return sqlStatementContext instanceof AlterViewStatementContext;
    }
    
    @Override
    public void check(final ShardingRule rule, final ShardingSphereDatabase database, final ShardingSphereSchema currentSchema, final AlterViewStatementContext sqlStatementContext) {
        AlterViewStatement alterViewStatement = sqlStatementContext.getSqlStatement();
        Optional<SelectStatement> selectStatement = alterViewStatement.getSelect();
        String originView = alterViewStatement.getView().getTableName().getIdentifier().getValue();
        selectStatement.ifPresent(optional -> checkAlterViewShardingTables(rule, optional, originView));
        alterViewStatement.getRenameView().ifPresent(optional -> checkBroadcastShardingView(rule, originView, optional.getTableName().getIdentifier().getValue()));
    }
    
    private void checkAlterViewShardingTables(final ShardingRule shardingRule, final SelectStatement selectStatement, final String viewName) {
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(selectStatement);
        ShardingSpherePreconditions.checkState(!ShardingSupportedCheckUtils.isShardingTablesNotBindingWithView(extractor.getRewriteTables(), shardingRule, viewName),
                () -> new EngagedViewException("sharding"));
    }
    
    private void checkBroadcastShardingView(final ShardingRule shardingRule, final String originView, final String targetView) {
        ShardingSpherePreconditions.checkState(!shardingRule.isShardingTable(originView) && !shardingRule.isShardingTable(targetView)
                || shardingRule.isAllConfigBindingTables(Arrays.asList(originView, targetView)), () -> new RenamedViewWithoutSameConfigurationException(originView, targetView));
    }
}
