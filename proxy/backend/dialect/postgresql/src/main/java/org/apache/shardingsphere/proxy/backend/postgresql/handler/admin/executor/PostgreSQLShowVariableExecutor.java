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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Executor for PostgreSQL show statement.
 */
@RequiredArgsConstructor
public final class PostgreSQLShowVariableExecutor implements DatabaseAdminQueryExecutor {
    
    private static final Map<String, VariableRowDataGenerator> VARIABLE_ROW_DATA_GENERATORS = new LinkedHashMap<>(7, 1F);
    
    static {
        VARIABLE_ROW_DATA_GENERATORS.put("application_name", connectionSession -> new String[]{"application_name", "PostgreSQL", "Sets the application name to be reported in statistics and logs."});
        VARIABLE_ROW_DATA_GENERATORS.put("client_encoding", connectionSession -> new String[]{"client_encoding", "UTF8", "Sets the client's character set encoding."});
        VARIABLE_ROW_DATA_GENERATORS.put("integer_datetimes", connectionSession -> new String[]{"integer_datetimes", "on", "Shows whether datetimes are integer based."});
        VARIABLE_ROW_DATA_GENERATORS.put("timezone", connectionSession -> new String[]{"TimeZone", "Etc/UTC", "Sets the time zone for displaying and interpreting time stamps."});
        VARIABLE_ROW_DATA_GENERATORS.put("transaction_isolation", connectionSession -> {
            String result = connectionSession.getIsolationLevel().orElse(TransactionIsolationLevel.READ_COMMITTED).getIsolationLevel().replace("-", " ").toLowerCase(Locale.ROOT);
            return new String[]{"transaction_isolation", result, "Sets the current transaction's isolation level"};
        });
        VARIABLE_ROW_DATA_GENERATORS.put("transaction_read_only",
                connectionSession -> new String[]{"transaction_read_only", connectionSession.isReadOnly() ? "on" : "off", "Sets the current transaction's read-only status."});
        VARIABLE_ROW_DATA_GENERATORS.put("server_version", connectionSession -> new String[]{"server_version", ShardingSphereVersion.VERSION, "Shows the server version."});
    }
    
    private final ShowStatement showStatement;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        String name = showStatement.getName().toLowerCase(Locale.ROOT);
        if ("ALL".equalsIgnoreCase(name)) {
            executeShowAll(connectionSession);
            return;
        }
        queryResultMetaData = new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", "", name, Types.VARCHAR, "VARCHAR", -1, 0)));
        VariableRowDataGenerator variableRowDataGenerator = VARIABLE_ROW_DATA_GENERATORS.getOrDefault(name, unused -> new String[]{"", "", ""});
        mergedResult = new LocalDataMergedResult(Collections.singletonList(new LocalDataQueryResultRow(variableRowDataGenerator.getVariable(connectionSession)[1])));
    }
    
    private void executeShowAll(final ConnectionSession connectionSession) {
        queryResultMetaData = new RawQueryResultMetaData(Arrays.asList(
                new RawQueryResultColumnMetaData("", "", "name", Types.VARCHAR, "VARCHAR", -1, 0),
                new RawQueryResultColumnMetaData("", "", "setting", Types.VARCHAR, "VARCHAR", -1, 0),
                new RawQueryResultColumnMetaData("", "", "description", Types.VARCHAR, "VARCHAR", -1, 0)));
        mergedResult = new LocalDataMergedResult(VARIABLE_ROW_DATA_GENERATORS.values().stream().map(each -> new LocalDataQueryResultRow(each.getVariable(connectionSession)))
                .collect(Collectors.toList()));
    }
    
    private interface VariableRowDataGenerator {
        
        Object[] getVariable(ConnectionSession connectionSession);
    }
}
