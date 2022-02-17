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
import org.apache.shardingsphere.distsql.parser.statement.ral.CommonDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.RefreshTableMetadataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterTransactionRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.drop.DropTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.LabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetInstanceStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.SetVariableStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.set.UnlabelInstanceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AlterTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.CreateTrafficRuleStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler.HandlerParameter;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced.AdvancedDistSQLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.CommonDistSQLBackendHandlerFactory;
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
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.status.SetReadwriteSplittingStatusStatement;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RALBackendHandlerFactory {
    
    private static Map<String, Class<? extends RALBackendHandler>> handlerMap = new LinkedHashMap<>();
    
    static {
        handlerMap.put(LabelInstanceStatement.class.getName(), LabelInstanceHandler.class);
        handlerMap.put(UnlabelInstanceStatement.class.getName(), UnlabelInstanceHandler.class);
        handlerMap.put(SetInstanceStatusStatement.class.getName(), SetInstanceStatusHandler.class);
        handlerMap.put(SetVariableStatement.class.getName(), SetVariableHandler.class);
        handlerMap.put(SetReadwriteSplittingStatusStatement.class.getName(), SetReadwriteSplittingStatusHandler.class);
        handlerMap.put(RefreshTableMetadataStatement.class.getName(), RefreshTableMetadataHandler.class);
        handlerMap.put(CreateTrafficRuleStatement.class.getName(), CreateTrafficRuleHandler.class);
        handlerMap.put(AlterTrafficRuleStatement.class.getName(), AlterTrafficRuleHandler.class);
        handlerMap.put(DropTrafficRuleStatement.class.getName(), DropTrafficRuleHandler.class);
        handlerMap.put(AlterSQLParserRuleStatement.class.getName(), AlterSQLParserRuleHandler.class);
        handlerMap.put(AlterTransactionRuleStatement.class.getName(), AlterTransactionRuleHandler.class);
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
        if (sqlStatement instanceof QueryableRALStatement) {
            result = QueryableScalingRALBackendHandlerFactory.newInstance((QueryableRALStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof UpdatableRALStatement) {
            result = UpdatableScalingRALBackendHandlerFactory.newInstance((UpdatableRALStatement) sqlStatement);
        }
        if (sqlStatement instanceof CommonDistSQLStatement) {
            result = CommonDistSQLBackendHandlerFactory.newInstance((CommonDistSQLStatement) sqlStatement, connectionSession);
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
        Class<? extends RALBackendHandler> clazz = handlerMap.get(sqlStatement.getClass().getName());
        if (null == clazz) {
            throw new UnsupportedOperationException(String.format("Unsupported statement : %s", sqlStatement.getClass().getCanonicalName()));
        }
        return newInstance(clazz).init(parameter);
    }
}
