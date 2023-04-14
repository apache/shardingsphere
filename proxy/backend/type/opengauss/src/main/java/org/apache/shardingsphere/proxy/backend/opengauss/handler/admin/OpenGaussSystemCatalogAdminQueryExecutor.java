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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin;

import lombok.Getter;
import org.apache.calcite.adapter.java.ReflectiveSchema;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.impl.ScalarFunctionImpl;
import org.apache.shardingsphere.infra.autogen.version.ShardingSphereVersion;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.metadata.JDBCQueryResultMetaData;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.memory.JDBCMemoryQueryResult;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.schema.OpenGaussDatabase;
import org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.schema.OpenGaussSystemCatalog;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sharding.merge.common.IteratorStreamMergedResult;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * Select database executor for openGauss.
 */
@SuppressWarnings("unused")
public final class OpenGaussSystemCatalogAdminQueryExecutor implements DatabaseAdminQueryExecutor {
    
    private static final String PG_CATALOG = "pg_catalog";
    
    private static final String DAT_COMPATIBILITY = "PG";
    
    private final String sql;
    
    @Getter
    private QueryResultMetaData queryResultMetaData;
    
    @Getter
    private MergedResult mergedResult;
    
    public OpenGaussSystemCatalogAdminQueryExecutor(final String sql) {
        this.sql = SQLUtils.trimSemicolon(sql);
    }
    
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:calcite:caseSensitive=false")) {
            prepareCalciteConnection(connection);
            connection.setSchema(PG_CATALOG);
            try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
                queryResultMetaData = new JDBCQueryResultMetaData(resultSet.getMetaData());
                mergedResult = new IteratorStreamMergedResult(Collections.singletonList(new JDBCMemoryQueryResult(resultSet, connectionSession.getProtocolType())));
            }
        }
    }
    
    private void prepareCalciteConnection(final Connection connection) throws SQLException {
        CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
        calciteConnection.getRootSchema().add(PG_CATALOG, new ReflectiveSchema(constructOgCatalog()));
        calciteConnection.getRootSchema().add("version", ScalarFunctionImpl.create(getClass(), "version"));
        calciteConnection.getRootSchema().add("gs_password_deadline", ScalarFunctionImpl.create(getClass(), "gsPasswordDeadline"));
        calciteConnection.getRootSchema().add("intervaltonum", ScalarFunctionImpl.create(getClass(), "intervalToNum"));
        calciteConnection.getRootSchema().add("gs_password_notifyTime", ScalarFunctionImpl.create(getClass(), "gsPasswordNotifyTime"));
    }
    
    private OpenGaussSystemCatalog constructOgCatalog() {
        Collection<String> allDatabaseNames = ProxyContext.getInstance().getAllDatabaseNames();
        OpenGaussDatabase[] openGaussDatabases = new OpenGaussDatabase[allDatabaseNames.size()];
        int i = 0;
        for (String each : allDatabaseNames) {
            openGaussDatabases[i++] = new OpenGaussDatabase(each, DAT_COMPATIBILITY);
        }
        return new OpenGaussSystemCatalog(openGaussDatabases);
    }
    
    /**
     * Get version of ShardingSphere-Proxy.
     *
     * @return version message
     */
    public static String version() {
        return "ShardingSphere-Proxy " + ShardingSphereVersion.VERSION + ("-" + ShardingSphereVersion.BUILD_GIT_COMMIT_ID_ABBREV) + (ShardingSphereVersion.BUILD_GIT_DIRTY ? "-dirty" : "");
    }
    
    /**
     * The type interval is not supported in standard JDBC.
     * Indicates the number of remaining days before the password of the current user expires.
     *
     * @return 90 days
     */
    public static int gsPasswordDeadline() {
        return 90;
    }
    
    /**
     * The type interval is not supported in standard JDBC.
     * Convert interval to num.
     *
     * @param result result
     * @return result
     */
    public static int intervalToNum(final int result) {
        return result;
    }
    
    /**
     * Specifies the number of days prior to password expiration that a user will receive a reminder.
     *
     * @return 7 days
     */
    public static int gsPasswordNotifyTime() {
        return 7;
    }
}
