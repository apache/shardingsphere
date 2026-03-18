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

import java.sql.Types;
import java.util.Collections;

/**
 * OpenGauss select version executor.
 */
@Getter
public final class OpenGaussSelectVersionExecutor implements DatabaseAdminQueryExecutor {
    
    private static final String FUNCTION_NAME = "VERSION()";
    
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        String version = "ShardingSphere-Proxy " + ShardingSphereVersion.VERSION + ("-" + ShardingSphereVersion.BUILD_COMMIT_ID_ABBREV) + (ShardingSphereVersion.BUILD_DIRTY ? "-dirty" : "");
        mergedResult = new LocalDataMergedResult(Collections.singleton(new LocalDataQueryResultRow(version)));
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        return new RawQueryResultMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("", "version", "version", Types.VARCHAR, "VARCHAR", 200, 0)));
    }
    
    /**
     * Accept.
     *
     * @param functionName function name.
     * @return true or false
     */
    public static boolean accept(final String functionName) {
        return FUNCTION_NAME.equalsIgnoreCase(functionName);
    }
}
