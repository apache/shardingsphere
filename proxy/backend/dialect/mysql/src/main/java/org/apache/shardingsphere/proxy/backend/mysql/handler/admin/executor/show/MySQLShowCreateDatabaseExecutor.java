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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.checker.AuthorityChecker;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.database.MySQLShowCreateDatabaseStatement;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Show create database executor for MySQL.
 */
@RequiredArgsConstructor
@Getter
public final class MySQLShowCreateDatabaseExecutor implements DatabaseAdminQueryExecutor {
    
    private final MySQLShowCreateDatabaseStatement sqlStatement;
    
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        mergedResult = new LocalDataMergedResult(getQueryResultRows(connectionSession, sqlStatement.getDatabaseName(), metaData));
    }
    
    private Collection<LocalDataQueryResultRow> getQueryResultRows(final ConnectionSession connectionSession, final String databaseName, final ShardingSphereMetaData metaData) {
        ShardingSpherePreconditions.checkState(metaData.containsDatabase(databaseName), () -> new UnknownDatabaseException(databaseName));
        AuthorityRule authorityRule = metaData.getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        AuthorityChecker authorityChecker = new AuthorityChecker(authorityRule, connectionSession.getConnectionContext().getGrantee());
        ShardingSpherePreconditions.checkState(authorityChecker.isAuthorized(databaseName), () -> new UnknownDatabaseException(databaseName));
        return Collections.singleton(new LocalDataQueryResultRow(databaseName, String.format("CREATE DATABASE `%s`;", databaseName)));
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columnMetaData = Arrays.asList(
                new RawQueryResultColumnMetaData("", "Database", "Database", Types.VARCHAR, "VARCHAR", 255, 0),
                new RawQueryResultColumnMetaData("", "Create Database", "Create Database", Types.VARCHAR, "VARCHAR", 255, 0));
        return new RawQueryResultMetaData(columnMetaData);
    }
}
