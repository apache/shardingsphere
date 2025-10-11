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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.executor;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor.PostgreSQLShowVariableExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ShowStatement;

import java.sql.Types;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * OpenGauss show variable executor.
 */
@RequiredArgsConstructor
public final class OpenGaussShowVariableExecutor implements DatabaseAdminQueryExecutor {
    
    private static final Map<String, OpenGaussShowVariableExecutor.VariableRowDataGenerator> VARIABLE_ROW_DATA_GENERATORS = new CaseInsensitiveMap<>(1, 1F);
    
    static {
        VARIABLE_ROW_DATA_GENERATORS.put("sql_compatibility", connectionSession -> new String[]{"sql_compatibility", "PG", "Show sql_compatibility value."});
    }
    
    private final ShowStatement showStatement;
    
    private final PostgreSQLShowVariableExecutor delegate;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    public OpenGaussShowVariableExecutor(final ShowStatement showStatement) {
        this.showStatement = showStatement;
        delegate = new PostgreSQLShowVariableExecutor(showStatement);
    }
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        String name = showStatement.getName().toLowerCase(Locale.ROOT);
        if (VARIABLE_ROW_DATA_GENERATORS.containsKey(name)) {
            queryResultMetaData = new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", "", name, Types.VARCHAR, "VARCHAR", -1, 0)));
            OpenGaussShowVariableExecutor.VariableRowDataGenerator variableRowDataGenerator = VARIABLE_ROW_DATA_GENERATORS.getOrDefault(name, unused -> new String[]{"", "", ""});
            mergedResult = new LocalDataMergedResult(Collections.singletonList(new LocalDataQueryResultRow(variableRowDataGenerator.getVariable(connectionSession)[1])));
        } else {
            delegated(connectionSession, metaData);
        }
    }
    
    private void delegated(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        delegate.execute(connectionSession, metaData);
        queryResultMetaData = delegate.getQueryResultMetaData();
        mergedResult = delegate.getMergedResult();
    }
    
    private interface VariableRowDataGenerator {
        
        Object[] getVariable(ConnectionSession connectionSession);
    }
}
