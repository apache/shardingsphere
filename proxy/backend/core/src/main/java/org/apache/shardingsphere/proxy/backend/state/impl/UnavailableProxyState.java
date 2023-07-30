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

package org.apache.shardingsphere.proxy.backend.state.impl;

import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.UnlockClusterStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.proxy.backend.exception.UnavailableException;
import org.apache.shardingsphere.proxy.backend.state.ProxyClusterState;
import org.apache.shardingsphere.proxy.backend.state.SupportedSQLStatementJudgeEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Unavailable proxy state.
 */
public final class UnavailableProxyState implements ProxyClusterState {
    
    private static final Collection<Class<? extends SQLStatement>> SUPPORTED_SQL_STATEMENTS = Arrays.asList(
            ImportMetaDataStatement.class, ShowStatement.class, QueryableRALStatement.class,
            RQLStatement.class, UnlockClusterStatement.class, MySQLShowDatabasesStatement.class, MySQLUseStatement.class);
    
    private static final Collection<Class<? extends SQLStatement>> UNSUPPORTED_SQL_STATEMENTS = Collections.singleton(SQLStatement.class);
    
    private final SupportedSQLStatementJudgeEngine judgeEngine = new SupportedSQLStatementJudgeEngine(SUPPORTED_SQL_STATEMENTS, UNSUPPORTED_SQL_STATEMENTS);
    
    @Override
    public void check(final SQLStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(judgeEngine.isSupported(sqlStatement), UnavailableException::new);
    }
    
    @Override
    public String getType() {
        return "UNAVAILABLE";
    }
}
