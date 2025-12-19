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
 * OpenGauss select password notify time executor.
 */
public final class OpenGaussSelectPasswordNotifyTimeExecutor implements DatabaseAdminQueryExecutor {
    
    private static final int DEFAULT_PASSWORD_NOTIFY_TIME = 7;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        mergedResult = new LocalDataMergedResult(Collections.singleton(new LocalDataQueryResultRow(DEFAULT_PASSWORD_NOTIFY_TIME)));
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        String columnName = "gs_password_notifytime";
        return new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", columnName, columnName, Types.VARCHAR, "VARCHAR", 50, 0)));
    }
    
    /**
     * Accept.
     *
     * @param functionName function name.
     * @return true or false
     */
    public static boolean accept(final String functionName) {
        return "gs_password_notifytime()".equalsIgnoreCase(functionName) || "pg_catalog.gs_password_notifytime()".equalsIgnoreCase(functionName);
    }
}
