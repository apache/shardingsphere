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
import org.apache.shardingsphere.distsql.parser.statement.ral.AdvancedDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.ExportSchemaConfigurationStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.HintDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.drop.DropTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.LabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.UnlabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAllVariablesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAuthorityRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowInstanceModeStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowTrafficRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AlterTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateTrafficRuleStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler.HandlerParameter;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced.AdvancedDistSQLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.HintDistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ExportSchemaConfigurationHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowAllVariablesHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowAuthorityRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowInstanceHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowInstanceModeHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowReadwriteSplittingReadResourcesHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowSQLParserRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowTableMetadataHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowTrafficRulesHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowTransactionRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable.ShowVariableHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.AlterSQLParserRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.AlterTrafficRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.AlterTransactionRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.CreateTrafficRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.DropTrafficRuleHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.LabelInstanceHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.RefreshTableMetadataHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.SetInstanceStatusHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.SetReadwriteSplittingStatusHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.SetVariableHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable.UnlabelInstanceHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.query.QueryableScalingRALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.update.UpdatableScalingRALBackendHandlerFactory;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.ShowReadwriteSplittingReadResourcesStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.SetReadwriteSplittingStatusStatement;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RALBackendHandlerFactory {
    
    private static final Map<String, Class<? extends RALBackendHandler>> UPDATABLE_HANDLER_MAP = new LinkedHashMap<>();
    
    private static final Map<String, Class<? extends RALBackendHandler>> QUERYABLE_HANDLER_MAP = new LinkedHashMap<>();
    
    static {
        initUpdatableHandlerMap();
        initQueryableHandlerMap();
    }
    
    private static void initUpdatableHandlerMap() {
        UPDATABLE_HANDLER_MAP.put(LabelInstanceStatement.class.getName(), LabelInstanceHandler.class);
        UPDATABLE_HANDLER_MAP.put(UnlabelInstanceStatement.class.getName(), UnlabelInstanceHandler.class);
        UPDATABLE_HANDLER_MAP.put(SetInstanceStatusStatement.class.getName(), SetInstanceStatusHandler.class);
        UPDATABLE_HANDLER_MAP.put(SetVariableStatement.class.getName(), SetVariableHandler.class);
        UPDATABLE_HANDLER_MAP.put(SetReadwriteSplittingStatusStatement.class.getName(), SetReadwriteSplittingStatusHandler.class);
        UPDATABLE_HANDLER_MAP.put(RefreshTableMetadataStatement.class.getName(), RefreshTableMetadataHandler.class);
        UPDATABLE_HANDLER_MAP.put(CreateTrafficRuleStatement.class.getName(), CreateTrafficRuleHandler.class);
        UPDATABLE_HANDLER_MAP.put(AlterTrafficRuleStatement.class.getName(), AlterTrafficRuleHandler.class);
        UPDATABLE_HANDLER_MAP.put(DropTrafficRuleStatement.class.getName(), DropTrafficRuleHandler.class);
        UPDATABLE_HANDLER_MAP.put(AlterSQLParserRuleStatement.class.getName(), AlterSQLParserRuleHandler.class);
        UPDATABLE_HANDLER_MAP.put(AlterTransactionRuleStatement.class.getName(), AlterTransactionRuleHandler.class);
    }
    
    private static void initQueryableHandlerMap() {
        QUERYABLE_HANDLER_MAP.put(ShowInstanceStatement.class.getName(), ShowInstanceHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowInstanceModeStatement.class.getName(), ShowInstanceModeHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowVariableStatement.class.getName(), ShowVariableHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowAllVariablesStatement.class.getName(), ShowAllVariablesHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowReadwriteSplittingReadResourcesStatement.class.getName(), ShowReadwriteSplittingReadResourcesHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowAuthorityRuleStatement.class.getName(), ShowAuthorityRuleHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowSQLParserRuleStatement.class.getName(), ShowSQLParserRuleHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowTableMetadataStatement.class.getName(), ShowTableMetadataHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowTrafficRulesStatement.class.getName(), ShowTrafficRulesHandler.class);
        QUERYABLE_HANDLER_MAP.put(ShowTransactionRuleStatement.class.getName(), ShowTransactionRuleHandler.class);
        QUERYABLE_HANDLER_MAP.put(ExportSchemaConfigurationStatement.class.getName(), ExportSchemaConfigurationHandler.class);
    }
    
    /**
     * Create new instance of RAL backend handler.
     *
     * @param databaseType database type
     * @param sqlStatement RAL statement
     * @param connectionSession connection session
     * @return RAL backend handler
     * @throws SQLException SQL exception
     */
    public static TextProtocolBackendHandler newInstance(final DatabaseType databaseType, final RALStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        TextProtocolBackendHandler result = null;
        if (sqlStatement instanceof HintDistSQLStatement) {
            return new HintDistSQLBackendHandler((HintDistSQLStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof QueryableRALStatement) {
            result = QueryableScalingRALBackendHandlerFactory.newInstance((QueryableRALStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof UpdatableRALStatement) {
            result = UpdatableScalingRALBackendHandlerFactory.newInstance((UpdatableRALStatement) sqlStatement);
        }
        if (sqlStatement instanceof AdvancedDistSQLStatement) {
            result = AdvancedDistSQLBackendHandlerFactory.newInstance(databaseType, (AdvancedDistSQLStatement) sqlStatement, connectionSession);
        }
        if (result == null) {
            HandlerParameter parameter = new HandlerParameter(sqlStatement, databaseType, connectionSession);
            result = getHandler(sqlStatement, parameter);
        }
        return result;
    }
    
    private static RALBackendHandler newInstance(final Class<? extends RALBackendHandler> clazz) {
        try {
            return clazz.newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new UnsupportedOperationException(String.format("Can not find public constructor for class `%s`", clazz.getName()));
        }
    }
    
    private static RALBackendHandler getHandler(final RALStatement sqlStatement, final HandlerParameter<RALStatement> parameter) {
        Class<? extends RALBackendHandler> clazz = UPDATABLE_HANDLER_MAP.getOrDefault(sqlStatement.getClass().getName(), QUERYABLE_HANDLER_MAP.get(sqlStatement.getClass().getName()));
        if (null == clazz) {
            throw new UnsupportedOperationException(String.format("Unsupported statement : %s", sqlStatement.getClass().getCanonicalName()));
        }
        return newInstance(clazz).init(parameter);
    }
}
