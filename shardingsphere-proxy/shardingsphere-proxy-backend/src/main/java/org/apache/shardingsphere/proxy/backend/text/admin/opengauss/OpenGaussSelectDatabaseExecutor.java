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

package org.apache.shardingsphere.proxy.backend.text.admin.opengauss;

import lombok.Getter;
import org.apache.calcite.adapter.java.ReflectiveSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.opengauss.schema.OgCatalog;
import org.apache.shardingsphere.proxy.backend.text.admin.opengauss.schema.OgDatabase;
import org.apache.shardingsphere.sharding.merge.dql.iterator.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;

/**
 * Select database executor for openGauss.
 */
public final class OpenGaussSelectDatabaseExecutor implements DatabaseAdminQueryExecutor {
    
    private static final String PG_CATALOG = "pg_catalog";
    
    private static final String DAT_COMPATIBILITY = "PG";
    
    private final String sql;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    public OpenGaussSelectDatabaseExecutor(final String sql) {
        this.sql = SQLUtil.trimSemicolon(sql);
    }
    
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        try (CalciteConnection connection = DriverManager.getConnection("jdbc:calcite:caseSensitive=false").unwrap(CalciteConnection.class)) {
            connection.getRootSchema().add(PG_CATALOG, new ReflectiveSchema(constructOgCatalog()));
            connection.setSchema(PG_CATALOG);
            try (Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
                queryResultMetaData = new JDBCQueryResultMetaData(resultSet.getMetaData());
                mergedResult = new IteratorStreamMergedResult(Collections.singletonList(new JDBCMemoryQueryResult(resultSet)));
            }
        }
    }
    
    private OgCatalog constructOgCatalog() {
        Collection<String> allSchemaNames = ProxyContext.getInstance().getAllSchemaNames();
        OgDatabase[] ogDatabases = new OgDatabase[allSchemaNames.size()];
        int i = 0;
        for (String each : allSchemaNames) {
            ogDatabases[i++] = new OgDatabase(each, DAT_COMPATIBILITY);
        }
        return new OgCatalog(ogDatabases);
    }
}
