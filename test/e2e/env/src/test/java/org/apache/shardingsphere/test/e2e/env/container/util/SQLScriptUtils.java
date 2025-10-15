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

package org.apache.shardingsphere.test.e2e.env.container.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.sqlbatch.DialectSQLBatchOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.h2.util.ScriptReader;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

/**
 * SQL script utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLScriptUtils {
    
    private static final int SQL_BATCH_SIZE = 100;
    
    /**
     * Execute SQL script.
     *
     * @param dataSource data source
     * @param scriptFilePath script file path
     */
    @SneakyThrows({SQLException.class, IOException.class})
    public static void execute(final DataSource dataSource, final String scriptFilePath) {
        Collection<String> sqls = readSQLs(scriptFilePath);
        try (Connection connection = dataSource.getConnection()) {
            execute(connection, sqls);
        }
    }
    
    /**
     * Execute SQL script.
     *
     * @param connection connection
     * @param scriptFilePath script file path
     */
    @SneakyThrows({SQLException.class, IOException.class})
    public static void execute(final Connection connection, final String scriptFilePath) {
        execute(connection, readSQLs(scriptFilePath));
    }
    
    private static void execute(final Connection connection, final Collection<String> sqls) throws SQLException {
        DatabaseType databaseType = DatabaseTypeFactory.get(connection.getMetaData());
        DialectSQLBatchOption sqlBatchOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getSQLBatchOption();
        if (sqlBatchOption.isSupportSQLBatch()) {
            executeBatch(connection, sqls);
            return;
        }
        for (String each : sqls) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(each);
            }
        }
    }
    
    private static Collection<String> readSQLs(final String scriptFilePath) throws IOException {
        Collection<String> result = new LinkedList<>();
        try (
                Reader reader = getReader(scriptFilePath);
                ScriptReader scriptReader = new ScriptReader(reader)) {
            scriptReader.setSkipRemarks(true);
            while (true) {
                String sql = scriptReader.readStatement();
                if (null == sql) {
                    break;
                }
                if (!StringUtils.isBlank(sql)) {
                    result.add(sql);
                }
            }
        }
        return result;
    }
    
    private static Reader getReader(final String scriptFilePath) throws FileNotFoundException {
        InputStream resourceAsStream = SQLScriptUtils.class.getClassLoader().getResourceAsStream(Strings.CS.removeStart(scriptFilePath, "/"));
        return null == resourceAsStream ? new FileReader(scriptFilePath) : new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
    }
    
    private static void executeBatch(final Connection connection, final Collection<String> sqls) throws SQLException {
        int count = 0;
        try (Statement statement = connection.createStatement()) {
            for (String each : sqls) {
                statement.addBatch(each);
                count++;
                if (0 == count % SQL_BATCH_SIZE) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
            }
            if (0 != count % SQL_BATCH_SIZE) {
                statement.executeBatch();
            }
        }
    }
}
