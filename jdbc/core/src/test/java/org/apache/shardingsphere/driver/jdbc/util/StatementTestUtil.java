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

package org.apache.shardingsphere.driver.jdbc.util;

import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.h2.tools.RunScript;

import javax.sql.DataSource;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatementTestUtil {
    
    /**
     * Create data source with init file.
     * 
     * @param dataSourceName data source name
     * @param fileName file name
     * @return data source
     * @throws SQLException sql exception
     */
    public static DataSource createDataSourcesWithInitFile(final String dataSourceName, final String fileName) throws SQLException {
        DataSource result = buildDataSource(dataSourceName);
        initializeSchema(result, fileName);
        return result;
    }
    
    private static DataSource buildDataSource(final String dataSourceName) {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setJdbcUrl(String.format("jdbc:h2:mem:%s;DATABASE_TO_UPPER=false;MODE=MySQL", dataSourceName));
        result.setUsername("sa");
        result.setPassword("");
        result.setMaximumPoolSize(50);
        return result;
    }
    
    private static void initializeSchema(final DataSource dataSource, final String fileName) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            RunScript.execute(connection, new InputStreamReader(Objects.requireNonNull(StatementTestUtil.class.getClassLoader().getResourceAsStream(fileName))));
        }
    }
}
