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

package org.apache.shardingsphere.distsql.handler.executor.rul;

import com.google.common.base.Preconditions;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorConnectionContextAware;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.DistSQLConnectionContext;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.distsql.statement.type.rul.sql.PreviewStatement;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.EmptyRuleException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.cursor.CursorNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.CursorSQLStatementAttribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Preview executor.
 */
@Setter
public final class PreviewExecutor implements DistSQLQueryExecutor<PreviewStatement>, DistSQLExecutorDatabaseAware, DistSQLExecutorConnectionContextAware {
    
    private ShardingSphereDatabase database;
    
    private DistSQLConnectionContext connectionContext;
    
    @Override
    public Collection<String> getColumnNames(final PreviewStatement sqlStatement) {
        return Arrays.asList("data_source_name", "actual_sql");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final PreviewStatement sqlStatement, final ContextManager contextManager) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        String toBePreviewedSQL = sqlStatement.getSql();
        SQLStatement toBePreviewedStatement = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class).getSQLParserEngine(database.getProtocolType()).parse(toBePreviewedSQL, false);
        HintValueContext hintValueContext = connectionContext.getQueryContext().getHintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        String currentDatabaseName = connectionContext.getQueryContext().getConnectionContext().getCurrentDatabaseName().orElse(null);
        SQLStatementContext toBePreviewedStatementContext = new SQLBindEngine(metaData, currentDatabaseName, hintValueContext).bind(toBePreviewedStatement);
        QueryContext queryContext = new QueryContext(
                toBePreviewedStatementContext, toBePreviewedSQL, Collections.emptyList(), hintValueContext, connectionContext.getQueryContext().getConnectionContext(), metaData);
        if (toBePreviewedStatementContext.getSqlStatement().getAttributes().findAttribute(CursorSQLStatementAttribute.class).isPresent()
                && toBePreviewedStatementContext instanceof CursorHeldSQLStatementContext) {
            setUpCursorDefinition((CursorHeldSQLStatementContext) toBePreviewedStatementContext);
        }
        ShardingSpherePreconditions.checkState(database.isComplete(), () -> new EmptyRuleException(database.getName()));
        Collection<ExecutionUnit> executionUnits = getExecutionUnits(contextManager, metaData, queryContext);
        return executionUnits.stream().map(each -> new LocalDataQueryResultRow(each.getDataSourceName(), each.getSqlUnit().getSql())).collect(Collectors.toList());
    }
    
    private void setUpCursorDefinition(final CursorHeldSQLStatementContext toBePreviewedStatementContext) {
        Optional<CursorNameSegment> cursorNameSegment = toBePreviewedStatementContext.getSqlStatement().getAttributes().getAttribute(CursorSQLStatementAttribute.class).getCursorName();
        if (!cursorNameSegment.isPresent()) {
            return;
        }
        String cursorName = cursorNameSegment.get().getIdentifier().getValue().toLowerCase();
        CursorStatementContext cursorStatementContext = connectionContext.getQueryContext().getConnectionContext().getCursorContext().getCursorStatementContexts().get(cursorName);
        Preconditions.checkNotNull(cursorStatementContext, "Cursor %s does not exist.", cursorName);
        toBePreviewedStatementContext.setCursorStatementContext(cursorStatementContext);
    }
    
    private Collection<ExecutionUnit> getExecutionUnits(final ContextManager contextManager, final ShardingSphereMetaData metaData, final QueryContext queryContext) {
        for (PreviewExecutionUnitGenerator each : ShardingSphereServiceLoader.getServiceInstances(PreviewExecutionUnitGenerator.class)) {
            Optional<Collection<ExecutionUnit>> result = each.generate(database, queryContext, contextManager, connectionContext);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return new KernelProcessor().generateExecutionContext(queryContext, metaData.getGlobalRuleMetaData(), metaData.getProps()).getExecutionUnits();
    }
    
    @Override
    public Class<PreviewStatement> getType() {
        return PreviewStatement.class;
    }
}
