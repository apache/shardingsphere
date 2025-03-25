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

package org.apache.shardingsphere.test.e2e.env.container.atomic.util;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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

/**
 * Batched SQL utility class.
 */
public final class SQLScriptUtils {
    
    private static final int BATCH_SIZE = 100;
    
    /**
     * Execute SQL script.
     *
     * @param dataSource data source
     * @param scriptFilePath script file path
     */
    @SneakyThrows({SQLException.class, IOException.class})
    public static void execute(final DataSource dataSource, final String scriptFilePath) {
        try (
                Connection connection = dataSource.getConnection();
                Reader reader = getReader(scriptFilePath)) {
            Statement statement = connection.createStatement();
            ScriptReader r = new ScriptReader(reader);
            r.setSkipRemarks(true);
            while (true) {
                String sql = r.readStatement();
                if (null == sql) {
                    break;
                }
                if (StringUtils.isBlank(sql)) {
                    continue;
                }
                statement.execute(sql);
            }
        }
    }
    
    /**
     * Execute SQL script.
     *
     * @param dataSource data source
     * @param scriptFilePath script file path
     */
    @SneakyThrows({SQLException.class, IOException.class})
    public static void executeBatch(final DataSource dataSource, final String scriptFilePath) {
        try (
                Connection connection = dataSource.getConnection();
                Reader reader = getReader(scriptFilePath)) {
            Statement statement = connection.createStatement();
            ScriptReader r = new ScriptReader(reader);
            r.setSkipRemarks(true);
            int count = 0;
            while (true) {
                String sql = r.readStatement();
                if (null == sql) {
                    break;
                }
                if (StringUtils.isBlank(sql)) {
                    continue;
                }
                statement.addBatch(sql);
                count++;
                if (0 == count % BATCH_SIZE) {
                    statement.executeBatch();
                    statement.clearBatch();
                }
            }
            if (0 != count % BATCH_SIZE) {
                statement.executeBatch();
            }
        }
    }
    
    private static Reader getReader(final String scriptFilePath) throws FileNotFoundException {
        InputStream resourceAsStream = SQLScriptUtils.class.getClassLoader().getResourceAsStream(StringUtils.removeStart(scriptFilePath, "/"));
        if (resourceAsStream != null) {
            return new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8));
        } else {
            return new FileReader(scriptFilePath);
        }
    }
}
