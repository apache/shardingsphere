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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.sharding.merge.dal.common.SingleLocalDataMergedResult;

import java.sql.Types;
import java.util.Collections;

/**
 * Show transaction executor.
 */
@Getter
@RequiredArgsConstructor
public final class ShowTransactionExecutor implements DatabaseAdminQueryExecutor {
    
    public static final String TRANSACTION_READ_ONLY = "@@session.transaction_read_only";
    
    public static final String TRANSACTION_ISOLATION = "@@session.transaction_isolation";
    
    private MergedResult mergedResult;
    
    private final String functionName;
    
    @Override
    public void execute(final ConnectionSession connectionSession) {
        String row = functionName.equals(TRANSACTION_ISOLATION) ? "REPEATABLE-READ" : "0";
        mergedResult = new SingleLocalDataMergedResult(Collections.singleton(row));
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        return new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", functionName, functionName, Types.VARCHAR, "VARCHAR", 100, 0)));
    }
}
