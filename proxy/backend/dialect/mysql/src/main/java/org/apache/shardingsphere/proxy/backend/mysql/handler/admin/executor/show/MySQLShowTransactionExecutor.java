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
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultColumnMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata.RawQueryResultMetaData;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.MySQLShowTransactionStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Show transaction executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLShowTransactionExecutor implements DatabaseAdminQueryExecutor {
    
    private final MySQLShowTransactionStatement sqlStatement;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        String databaseName = getDatabaseName(connectionSession);
        if (null != databaseName) {
            ShardingSpherePreconditions.checkState(metaData.containsDatabase(databaseName), () -> new UnknownDatabaseException(databaseName));
        }
        queryResultMetaData = createQueryResultMetaData();
        mergedResult = new LocalDataMergedResult(getQueryResultRows(databaseName, metaData));
    }
    
    private String getDatabaseName(final ConnectionSession connectionSession) {
        return null == sqlStatement.getFromDatabase() ? connectionSession.getUsedDatabaseName() : sqlStatement.getFromDatabase().getDatabase().getIdentifier().getValue();
    }
    
    private QueryResultMetaData createQueryResultMetaData() {
        List<RawQueryResultColumnMetaData> columnNames = new ArrayList<>(6);
        columnNames.add(new RawQueryResultColumnMetaData("", "TransactionId", "TransactionId", Types.BIGINT, "BIGINT", 20, 0));
        columnNames.add(new RawQueryResultColumnMetaData("", "Label", "Label", Types.VARCHAR, "VARCHAR", 255, 0));
        columnNames.add(new RawQueryResultColumnMetaData("", "Database", "Database", Types.VARCHAR, "VARCHAR", 255, 0));
        columnNames.add(new RawQueryResultColumnMetaData("", "TransactionStatus", "TransactionStatus", Types.VARCHAR, "VARCHAR", 50, 0));
        columnNames.add(new RawQueryResultColumnMetaData("", "LoadStartTime", "LoadStartTime", Types.VARCHAR, "VARCHAR", 50, 0));
        columnNames.add(new RawQueryResultColumnMetaData("", "LoadFinishTime", "LoadFinishTime", Types.VARCHAR, "VARCHAR", 50, 0));
        return new RawQueryResultMetaData(columnNames);
    }
    
    private Collection<LocalDataQueryResultRow> getQueryResultRows(final String databaseName, final ShardingSphereMetaData metaData) {
        if (null == databaseName) {
            return Collections.emptyList();
        }
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        if (!database.isComplete()) {
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}
