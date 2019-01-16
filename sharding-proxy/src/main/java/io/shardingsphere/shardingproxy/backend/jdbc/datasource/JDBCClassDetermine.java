/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import io.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class JDBCClassDetermine {

    /*
    for MySQL-connector-java 8.x compatibility
    TODO getXADataSourceClassName
     */
    private static String mysqlclass = "";

    private Pattern pattern = Pattern.compile("jdbc:(mysql|postgresql):.*", Pattern.CASE_INSENSITIVE);
    
    /**
     * Get driver class name.
     * @param url url
     * @return driver class name
     */
    public String getDriverClassName(final String url) {

        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String databaseType = matcher.group(1).toLowerCase();
            switch (databaseType) {
                case "mysql":
                    new MySQLDataSourceMetaData(url);
                    if (mysqlclass.isEmpty()) {
                        try {
                            Class.forName("com.mysql.jdbc.Driver");
                            mysqlclass = "com.mysql.jdbc.Driver";
                        } catch (ClassNotFoundException e) {
                            try {
                                Class.forName("com.mysql.cj.jdbc.Driver");
                                mysqlclass = "com.mysql.cj.jdbc.Driver";
                            } catch (ClassNotFoundException ex) {
                                throw new UnsupportedOperationException(String.format("Cannot support url `%s`,no vailed mysql driver found", url));
                            }
                        }
                    }
                    return mysqlclass;
                case "postgresql":
                    new PostgreSQLDataSourceMetaData(url);
                    return "org.postgresql.Driver";
                default:
                    throw new UnsupportedOperationException(String.format("Cannot support url `%s`", url));
            }
        }
        throw new UnsupportedOperationException(String.format("Cannot support url `%s`", url));
    }
}
