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
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.function.MySQLShowFunctionStatusStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Show function status executor for MySQL.
 */
@RequiredArgsConstructor
@Getter
public final class MySQLShowFunctionStatusExecutor implements DatabaseAdminQueryExecutor {
    
    private final MySQLShowFunctionStatusStatement sqlStatement;
    
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        mergedResult = new LocalDataMergedResult(Collections.emptyList());
    }
    
    @Override
    public QueryResultMetaData getQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columns = new ArrayList<>(11);
        columns.add(new RawQueryResultColumnMetaData("", "Db", "Db", Types.VARCHAR, "VARCHAR", 255, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Name", "Name", Types.VARCHAR, "VARCHAR", 255, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Type", "Type", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Definer", "Definer", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Modified", "Modified", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Created", "Created", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Security_type", "Security_type", Types.VARCHAR, "VARCHAR", 64, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Comment", "Comment", Types.VARCHAR, "VARCHAR", 120, 0));
        columns.add(new RawQueryResultColumnMetaData("", "character_set_client", "character_set_client", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "collation_connection", "collation_connection", Types.VARCHAR, "VARCHAR", 20, 0));
        columns.add(new RawQueryResultColumnMetaData("", "Database_Collation", "Database_Collation", Types.VARCHAR, "VARCHAR", 20, 0));
        return new RawQueryResultMetaData(columns);
    }
}
