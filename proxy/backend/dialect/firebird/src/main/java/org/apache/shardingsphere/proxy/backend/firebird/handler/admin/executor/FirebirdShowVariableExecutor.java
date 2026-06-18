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

package org.apache.shardingsphere.proxy.backend.firebird.handler.admin.executor;

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
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;

import java.sql.Types;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Executor for Firebird show statement.
 */
@RequiredArgsConstructor
public final class FirebirdShowVariableExecutor implements DatabaseAdminQueryExecutor {
    
    private static final Map<String, VariableRowDataGenerator> VARIABLE_ROW_DATA_GENERATORS = new LinkedHashMap<>(7, 1);
    
    static {
        VARIABLE_ROW_DATA_GENERATORS.put("server_version", connectionSession -> new String[]{"server_version", ShardingSphereVersion.VERSION, "Shows the server version."});
        // TODO add SHOW statements support
    }
    
    private final ShowStatement showStatement;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        String name = Optional.ofNullable(showStatement.getName()).orElse("").toLowerCase(Locale.ROOT);
        queryResultMetaData = new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", "", name, Types.VARCHAR, "VARCHAR", -1, 0)));
        VariableRowDataGenerator variableRowDataGenerator = VARIABLE_ROW_DATA_GENERATORS.getOrDefault(name, unused -> new String[]{"", "", ""});
        mergedResult = new LocalDataMergedResult(Collections.singletonList(new LocalDataQueryResultRow(variableRowDataGenerator.getVariable(connectionSession)[1])));
    }
    
    private interface VariableRowDataGenerator {
        
        /**
         * Get variable.
         *
         * @param connectionSession connection session
         * @return variable
         */
        Object[] getVariable(ConnectionSession connectionSession);
    }
}
