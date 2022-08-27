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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.HintRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableGlobalRuleRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableGlobalRuleRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ConvertYamlConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowAllVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowInstanceListStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowModeInfoStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.QueryableScalingRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.UpdatableScalingRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ApplyDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DiscardDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.LabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.PrepareDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlabelInstanceStatement;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint.HintRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.migration.query.QueryableScalingRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.migration.query.QueryableScalingRALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.migration.update.UpdatableScalingRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ConvertYamlConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ExportDatabaseConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowAllVariablesHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowInstanceInfoHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowInstanceListHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowModeInfoHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowReadwriteSplittingReadResourcesHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowSQLParserRuleHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowSQLTranslatorRuleHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowTableMetadataHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowTransactionRuleHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable.ShowVariableHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.AlterSQLParserRuleHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.AlterTransactionRuleHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.ApplyDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.DiscardDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.ImportDatabaseConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.LabelInstanceHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.PrepareDistSQLHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.RefreshTableMetadataHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.SetInstanceStatusHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.SetReadwriteSplittingStatusHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.SetVariableHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.UnlabelInstanceHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
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
        HANDLERS.put(AlterSQLParserRuleStatement.class, AlterSQLParserRuleHandler.class);
        HANDLERS.put(AlterTransactionRuleStatement.class, AlterTransactionRuleHandler.class);
        HANDLERS.put(PrepareDistSQLStatement.class, PrepareDistSQLHandler.class);
        HANDLERS.put(ApplyDistSQLStatement.class, ApplyDistSQLHandler.class);
        HANDLERS.put(DiscardDistSQLStatement.class, DiscardDistSQLHandler.class);
        HANDLERS.put(ImportDatabaseConfigurationStatement.class, ImportDatabaseConfigurationHandler.class);
        HANDLERS.put(ShowInstanceListStatement.class, ShowInstanceListHandler.class);
        HANDLERS.put(ShowVariableStatement.class, ShowVariableHandler.class);
        HANDLERS.put(ShowAllVariablesStatement.class, ShowAllVariablesHandler.class);
        HANDLERS.put(ShowReadwriteSplittingReadResourcesStatement.class, ShowReadwriteSplittingReadResourcesHandler.class);
        HANDLERS.put(ShowSQLParserRuleStatement.class, ShowSQLParserRuleHandler.class);
        HANDLERS.put(ShowTableMetadataStatement.class, ShowTableMetadataHandler.class);
        HANDLERS.put(ShowTransactionRuleStatement.class, ShowTransactionRuleHandler.class);
        HANDLERS.put(ExportDatabaseConfigurationStatement.class, ExportDatabaseConfigurationHandler.class);
        HANDLERS.put(ConvertYamlConfigurationStatement.class, ConvertYamlConfigurationHandler.class);
        HANDLERS.put(ShowSQLTranslatorRuleStatement.class, ShowSQLTranslatorRuleHandler.class);
        HANDLERS.put(ShowInstanceInfoStatement.class, ShowInstanceInfoHandler.class);
        HANDLERS.put(ShowModeInfoStatement.class, ShowModeInfoHandler.class);
    }
    
    /**
     * Create new instance of RAL backend handler.
     *
     * @param sqlStatement RAL statement
     * @param connectionSession connection session
     * @return created instance
     * @throws SQLException SQL exception
     */
    public static ProxyBackendHandler newInstance(final RALStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        if (sqlStatement instanceof HintRALStatement) {
            return new HintRALBackendHandler((HintRALStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof QueryableScalingRALStatement) {
            DistSQLResultSet resultSet = QueryableScalingRALBackendHandlerFactory.newInstance((QueryableScalingRALStatement) sqlStatement);
            return new QueryableScalingRALBackendHandler((QueryableScalingRALStatement) sqlStatement, (DatabaseDistSQLResultSet) resultSet);
        }
        if (sqlStatement instanceof UpdatableScalingRALStatement) {
            return new UpdatableScalingRALBackendHandler((UpdatableScalingRALStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof QueryableGlobalRuleRALStatement) {
            return QueryableGlobalRuleRALBackendHandlerFactory.newInstance((QueryableGlobalRuleRALStatement) sqlStatement);
        }
        if (sqlStatement instanceof UpdatableGlobalRuleRALStatement) {
            return UpdatableGlobalRuleRALBackendHandlerFactory.newInstance((UpdatableGlobalRuleRALStatement) sqlStatement);
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
