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

package org.apache.shardingsphere.proxy.backend.text;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.DatabaseAdminBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.DistSQLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.extra.ExtraTextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.sctl.ShardingCTLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.sctl.utils.SCTLUtils;
import org.apache.shardingsphere.proxy.backend.text.skip.SkipBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.transaction.TransactionBackendHandlerFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Text protocol backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TextProtocolBackendHandlerFactory {
    
    static {
        ShardingSphereServiceLoader.register(ExtraTextProtocolBackendHandler.class);
    }
    
    /**
     * Create new instance of text protocol backend handler.
     *
     * @param databaseType database type
     * @param sql SQL to be executed
     * @param backendConnection backend connection
     * @return text protocol backend handler
     * @throws SQLException SQL exception
     */
    public static TextProtocolBackendHandler newInstance(final DatabaseType databaseType, final String sql, final BackendConnection backendConnection) throws SQLException {
        String trimSQL = SCTLUtils.trimComment(sql);
        if (Strings.isNullOrEmpty(trimSQL)) {
            return new SkipBackendHandler(new EmptyStatement());
        }
        // TODO Parse sctl SQL with ANTLR
        if (trimSQL.toUpperCase().startsWith(ShardingCTLBackendHandlerFactory.SCTL)) {
            return ShardingCTLBackendHandlerFactory.newInstance(trimSQL, backendConnection);
        }
        SQLStatement sqlStatement = new ShardingSphereSQLParserEngine(getBackendDatabaseType(databaseType, backendConnection).getName()).parse(sql, false);
        Optional<ExtraTextProtocolBackendHandler> extraHandler = findExtraTextProtocolBackendHandler(sqlStatement);
        if (extraHandler.isPresent()) {
            return extraHandler.get();
        }
        String schemaName = backendConnection.getSchemaName();
        SQLCheckEngine.check(sqlStatement, Collections.emptyList(), 
                getRules(schemaName), schemaName, ProxyContext.getInstance().getMetaDataContexts().getMetaDataMap(), backendConnection.getGrantee());
        if (sqlStatement instanceof TCLStatement) {
            return TransactionBackendHandlerFactory.newInstance((TCLStatement) sqlStatement, sql, backendConnection);
        }
        Optional<TextProtocolBackendHandler> distSQLBackendHandler = DistSQLBackendHandlerFactory.newInstance(databaseType, sqlStatement, backendConnection);
        if (distSQLBackendHandler.isPresent()) {
            return distSQLBackendHandler.get();
        }
        Optional<TextProtocolBackendHandler> databaseAdminBackendHandler = DatabaseAdminBackendHandlerFactory.newInstance(databaseType, sqlStatement, backendConnection);
        return databaseAdminBackendHandler.orElseGet(() -> DatabaseBackendHandlerFactory.newInstance(sqlStatement, sql, backendConnection));
    }
    
    private static DatabaseType getBackendDatabaseType(final DatabaseType defaultDatabaseType, final BackendConnection backendConnection) {
        return Strings.isNullOrEmpty(backendConnection.getSchemaName())
                ? defaultDatabaseType : ProxyContext.getInstance().getMetaDataContexts().getMetaData(backendConnection.getSchemaName()).getResource().getDatabaseType();
    }
    
    private static Optional<ExtraTextProtocolBackendHandler> findExtraTextProtocolBackendHandler(final SQLStatement sqlStatement) {
        return ShardingSphereServiceLoader.getSingletonServiceInstances(ExtraTextProtocolBackendHandler.class).stream().filter(each -> each.accept(sqlStatement)).findFirst();
    }
    
    private static Collection<ShardingSphereRule> getRules(final String schemaName) {
        MetaDataContexts contexts = ProxyContext.getInstance().getMetaDataContexts();
        if (Strings.isNullOrEmpty(schemaName)) {
            return contexts.getGlobalRuleMetaData().getRules();
        }
        Collection<ShardingSphereRule> result;
        result = new LinkedList<>(contexts.getMetaData(schemaName).getRuleMetaData().getRules());
        result.addAll(contexts.getGlobalRuleMetaData().getRules());
        return result;
    }
}
