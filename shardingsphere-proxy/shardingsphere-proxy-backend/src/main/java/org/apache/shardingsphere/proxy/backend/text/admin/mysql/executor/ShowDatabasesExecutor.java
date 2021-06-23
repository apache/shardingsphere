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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import lombok.Getter;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.executor.check.SQLCheckException;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.sharding.merge.dal.common.SingleLocalDataMergedResult;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Show databases executor.
 */
@Getter
public final class ShowDatabasesExecutor implements DatabaseAdminQueryExecutor {
    
    private MergedResult mergedResult;
    
    @Override
    public void execute(final BackendConnection backendConnection) {
        mergedResult = new SingleLocalDataMergedResult(getSchemaNames(backendConnection));
    }
    
    private Collection<Object> getSchemaNames(final BackendConnection backendConnection) {
        try {
            MetaDataContexts contexts = ProxyContext.getInstance().getMetaDataContexts();
            SQLCheckEngine.check(new MySQLShowDatabasesStatement(), Collections.emptyList(), 
                    contexts.getGlobalRuleMetaData().getRules(), backendConnection.getSchemaName(), contexts.getMetaDataMap(), backendConnection.getGrantee());
            return new ArrayList<>(ProxyContext.getInstance().getAllSchemaNames());
        } catch (final SQLCheckException ex) {
            Collection<Object> result = new LinkedList<>();
            for (String each : ProxyContext.getInstance().getAllSchemaNames()) {
                if (SQLCheckEngine.check(each, getRules(each), backendConnection.getGrantee())) {
                    result.add(each);
                }
            }
            return result;
        }
    }
    
    private Collection<ShardingSphereRule> getRules(final String schemaName) {
        Collection<ShardingSphereRule> result;
        result = new LinkedList<>(ProxyContext.getInstance().getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().getRules());
        result.addAll(ProxyContext.getInstance().getMetaDataContexts().getGlobalRuleMetaData().getRules());
        return result;
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        return new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("SCHEMATA", "Database", "SCHEMA_NAME", Types.VARCHAR, "VARCHAR", 255, 0)));
    }
}
