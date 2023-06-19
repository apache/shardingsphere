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

package org.apache.shardingsphere.sharding.route.engine.validator.ddl.impl;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.exception.syntax.RenamedViewWithoutSameConfigurationException;
import org.apache.shardingsphere.sharding.route.engine.validator.ddl.ShardingDDLStatementValidator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterViewStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl.AlterViewStatementHandler;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Sharding alter view statement validator.
 */
public final class ShardingAlterViewStatementValidator extends ShardingDDLStatementValidator {
    
    @Override
    public void preValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext,
                            final List<Object> params, final ShardingSphereDatabase database, final ConfigurationProperties props) {
        AlterViewStatement alterViewStatement = (AlterViewStatement) sqlStatementContext.getSqlStatement();
        Optional<SelectStatement> selectStatement = AlterViewStatementHandler.getSelectStatement(alterViewStatement);
        if (selectStatement.isPresent()) {
            TableExtractor extractor = new TableExtractor();
            extractor.extractTablesFromSelect(selectStatement.get());
            validateShardingTable(shardingRule, "ALTER VIEW", extractor.getRewriteTables());
        }
        Optional<SimpleTableSegment> renamedView = AlterViewStatementHandler.getRenameView(alterViewStatement);
        if (renamedView.isPresent()) {
            String targetView = renamedView.get().getTableName().getIdentifier().getValue();
            String originView = alterViewStatement.getView().getTableName().getIdentifier().getValue();
            validateBroadcastShardingView(shardingRule, originView, targetView);
        }
    }
    
    private void validateBroadcastShardingView(final ShardingRule shardingRule, final String originView, final String targetView) {
        ShardingSpherePreconditions.checkState(!shardingRule.isShardingTable(originView) && !shardingRule.isShardingTable(targetView)
                || shardingRule.isAllBindingTables(Arrays.asList(originView, targetView)), () -> new RenamedViewWithoutSameConfigurationException(originView, targetView));
    }
    
    @Override
    public void postValidate(final ShardingRule shardingRule, final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext, final List<Object> params,
                             final ShardingSphereDatabase database, final ConfigurationProperties props, final RouteContext routeContext) {
    }
}
