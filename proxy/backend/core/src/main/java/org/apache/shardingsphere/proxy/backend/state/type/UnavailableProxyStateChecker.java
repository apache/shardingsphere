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

package org.apache.shardingsphere.proxy.backend.state.type;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.updatable.UnlockClusterStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.exception.ShardingSphereStateException;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.apache.shardingsphere.proxy.backend.state.DialectProxyStateSupportedSQLProvider;
import org.apache.shardingsphere.proxy.backend.state.ProxyClusterStateChecker;
import org.apache.shardingsphere.proxy.backend.state.ProxySQLSupportedJudgeEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Unavailable proxy state.
 */
public final class UnavailableProxyStateChecker implements ProxyClusterStateChecker {
    
    private static final Collection<Class<? extends SQLStatement>> SUPPORTED_SQL_STATEMENT_TYPES = Arrays.asList(
            ImportMetaDataStatement.class, ShowStatement.class, QueryableRALStatement.class, RQLStatement.class, UnlockClusterStatement.class);
    
    private static final Collection<Class<? extends SQLStatement>> UNSUPPORTED_SQL_STATEMENT_TYPES = Collections.singleton(SQLStatement.class);
    
    @Override
    public void check(final SQLStatement sqlStatement, final DatabaseType databaseType) {
        Collection<Class<? extends SQLStatement>> supportedDialectSQLStatementTypes = DatabaseTypedSPILoader.findService(DialectProxyStateSupportedSQLProvider.class, databaseType)
                .map(DialectProxyStateSupportedSQLProvider::getSupportedSQLStatementTypesOnUnavailableState).orElse(Collections.emptyList());
        ProxySQLSupportedJudgeEngine judgeEngine = new ProxySQLSupportedJudgeEngine(
                SUPPORTED_SQL_STATEMENT_TYPES, supportedDialectSQLStatementTypes, UNSUPPORTED_SQL_STATEMENT_TYPES, Collections.emptyList());
        ShardingSpherePreconditions.checkState(judgeEngine.isSupported(sqlStatement), () -> new ShardingSphereStateException(getType(), sqlStatement));
    }
    
    @Override
    public ShardingSphereState getType() {
        return ShardingSphereState.UNAVAILABLE;
    }
}
