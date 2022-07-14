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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.HintRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.FormatStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.ParseStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.PreviewStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAllVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceModeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.QueryableScalingRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.UpdatableScalingRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ApplyDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.CreateTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DiscardDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DropTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.PrepareDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlabelInstanceStatement;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced.FormatSQLHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced.ParseDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced.PreviewHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.hint.HintRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ExportDatabaseConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowAllVariableHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowAuthorityRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowInstanceHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowInstanceModeHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowReadwriteSplittingReadResourcesHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowSQLParserRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowTableMetadataHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowTrafficRulesHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowTransactionRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.queryable.ShowVariableHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.scaling.query.QueryableScalingRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.scaling.query.QueryableScalingRALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.scaling.update.UpdatableScalingRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.AlterSQLParserRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.AlterTrafficRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.AlterTransactionRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.ApplyDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.CreateTrafficRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.DiscardDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.DropTrafficRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.ImportDatabaseConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.LabelInstanceHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.PrepareDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.RefreshTableMetadataHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.SetInstanceStatusHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.SetReadwriteSplittingStatusHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.SetVariableHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable.UnlabelInstanceHandler;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.SetReadwriteSplittingStatusStatement;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RALBackendHandlerFactory {
    
    private static final Map<Class<? extends RALStatement>, Class<? extends RALBackendHandler<?>>> HANDLERS = new HashMap<>();
    
    static {
        HANDLERS.put(LabelInstanceStatement.class, LabelInstanceHandler.class);
        HANDLERS.put(UnlabelInstanceStatement.class, UnlabelInstanceHandler.class);
        HANDLERS.put(SetInstanceStatusStatement.class, SetInstanceStatusHandler.class);
        HANDLERS.put(SetVariableStatement.class, SetVariableHandler.class);
        HANDLERS.put(SetReadwriteSplittingStatusStatement.class, SetReadwriteSplittingStatusHandler.class);
        HANDLERS.put(RefreshTableMetadataStatement.class, RefreshTableMetadataHandler.class);
        HANDLERS.put(CreateTrafficRuleStatement.class, CreateTrafficRuleHandler.class);
        HANDLERS.put(AlterTrafficRuleStatement.class, AlterTrafficRuleHandler.class);
        HANDLERS.put(DropTrafficRuleStatement.class, DropTrafficRuleHandler.class);
        HANDLERS.put(AlterSQLParserRuleStatement.class, AlterSQLParserRuleHandler.class);
        HANDLERS.put(AlterTransactionRuleStatement.class, AlterTransactionRuleHandler.class);
        HANDLERS.put(PrepareDistSQLStatement.class, PrepareDistSQLHandler.class);
        HANDLERS.put(ApplyDistSQLStatement.class, ApplyDistSQLHandler.class);
        HANDLERS.put(DiscardDistSQLStatement.class, DiscardDistSQLHandler.class);
        HANDLERS.put(ImportDatabaseConfigurationStatement.class, ImportDatabaseConfigurationHandler.class);
        HANDLERS.put(ShowInstanceStatement.class, ShowInstanceHandler.class);
        HANDLERS.put(ShowInstanceModeStatement.class, ShowInstanceModeHandler.class);
        HANDLERS.put(ShowVariableStatement.class, ShowVariableHandler.class);
        HANDLERS.put(ShowAllVariableStatement.class, ShowAllVariableHandler.class);
        HANDLERS.put(ShowReadwriteSplittingReadResourcesStatement.class, ShowReadwriteSplittingReadResourcesHandler.class);
        HANDLERS.put(ShowAuthorityRuleStatement.class, ShowAuthorityRuleHandler.class);
        HANDLERS.put(ShowSQLParserRuleStatement.class, ShowSQLParserRuleHandler.class);
        HANDLERS.put(ShowTableMetadataStatement.class, ShowTableMetadataHandler.class);
        HANDLERS.put(ShowTrafficRulesStatement.class, ShowTrafficRulesHandler.class);
        HANDLERS.put(ShowTransactionRuleStatement.class, ShowTransactionRuleHandler.class);
        HANDLERS.put(ExportDatabaseConfigurationStatement.class, ExportDatabaseConfigurationHandler.class);
        HANDLERS.put(ParseStatement.class, ParseDistSQLHandler.class);
        HANDLERS.put(PreviewStatement.class, PreviewHandler.class);
        HANDLERS.put(FormatStatement.class, FormatSQLHandler.class);
    }
    
    /**
     * Create new instance of RAL backend handler.
     *
     * @param sqlStatement RAL statement
     * @param connectionSession connection session
     * @return created instance
     * @throws SQLException SQL exception
     */
    public static TextProtocolBackendHandler newInstance(final RALStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        if (sqlStatement instanceof HintRALStatement) {
            return new HintRALBackendHandler((HintRALStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof QueryableScalingRALStatement) {
            return new QueryableScalingRALBackendHandler(sqlStatement, connectionSession, QueryableScalingRALBackendHandlerFactory.newInstance((QueryableScalingRALStatement) sqlStatement));
        }
        if (sqlStatement instanceof UpdatableScalingRALStatement) {
            return new UpdatableScalingRALBackendHandler((UpdatableScalingRALStatement) sqlStatement);
        }
        return createRALBackendHandler(sqlStatement, connectionSession);
    }
    
    private static RALBackendHandler<?> newInstance(final Class<? extends RALBackendHandler<?>> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (final ReflectiveOperationException ex) {
            throw new UnsupportedOperationException(String.format("Can not find public constructor for class `%s`", clazz.getName()));
        }
    }
    
    private static RALBackendHandler<?> createRALBackendHandler(final RALStatement sqlStatement, final ConnectionSession connectionSession) {
        Class<? extends RALBackendHandler<?>> clazz = HANDLERS.get(sqlStatement.getClass());
        if (null == clazz) {
            throw new UnsupportedOperationException(String.format("Unsupported SQL statement : %s", sqlStatement.getClass().getCanonicalName()));
        }
        RALBackendHandler<?> result = newInstance(clazz);
        result.init(sqlStatement, connectionSession);
        return result;
    }
}
