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
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.alter.AlterTransactionRuleStatement;
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
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.query.QueryableRALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.update.UpdatableRALBackendHandlerFactory;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RALBackendHandlerFactory {
    
    private static Map<String, Class<? extends RALBackendHandler>> handlerClz = new LinkedHashMap<>();
    
    static {
        handlerClz.put(CreateTrafficRuleStatement.class.getName(), CreateTrafficRuleHandler.class);
        handlerClz.put(AlterSQLParserRuleStatement.class.getName(), AlterSQLParserRuleHandler.class);
        handlerClz.put(AlterTrafficRuleStatement.class.getName(), AlterTrafficRuleHandler.class);
        handlerClz.put(AlterTransactionRuleStatement.class.getName(), AlterTransactionRuleHandler.class);
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
            result = QueryableRALBackendHandlerFactory.newInstance((QueryableRALStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof UpdatableRALStatement) {
            result = UpdatableRALBackendHandlerFactory.newInstance((UpdatableRALStatement) sqlStatement);
        }
        if (sqlStatement instanceof CommonDistSQLStatement) {
            result = CommonDistSQLBackendHandlerFactory.newInstance((CommonDistSQLStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof AdvancedDistSQLStatement) {
            result = AdvancedDistSQLBackendHandlerFactory.newInstance(databaseType, (AdvancedDistSQLStatement) sqlStatement, connectionSession);
        }
        if (result == null) {
            HandlerParameter parameter = new HandlerParameter().setStatement(sqlStatement).setConnectionSession(connectionSession).setDatabaseType(databaseType);
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
        Class<? extends RALBackendHandler> clz = handlerClz.get(sqlStatement.getClass().getName());
        if (null == clz) {
            throw new UnsupportedOperationException(sqlStatement.getClass().getCanonicalName());
        }
        return newInstance(clz).init(parameter);
    }
}
