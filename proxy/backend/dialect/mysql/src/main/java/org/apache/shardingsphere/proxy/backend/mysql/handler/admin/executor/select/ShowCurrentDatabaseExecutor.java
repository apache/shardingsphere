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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select;

import lombok.Getter;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.Types;
import java.util.Collections;

/**
 * Show current database executor.
 */
@Getter
public final class ShowCurrentDatabaseExecutor implements DatabaseAdminQueryExecutor {
    
    public static final String FUNCTION_NAME = "DATABASE()";
    
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        mergedResult = new LocalDataMergedResult(Collections.singleton(new LocalDataQueryResultRow(connectionSession.getUsedDatabaseName())));
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        return new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("SCHEMATA", FUNCTION_NAME, FUNCTION_NAME, Types.VARCHAR, "VARCHAR", 100, 0)));
    }
}
