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

package org.apache.shardingsphere.example.parser.mysql.format;

import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLFormatEngine;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public final class MySQLParserFormatExample {
    
    private static final String DML_SELECT_SQL = "SELECT age AS b, name AS n FROM table1 JOIN table2 WHERE id = 1 AND name = 'lu';";
    
    private static final String DML_INSERT_SQL = "INSERT INTO user (name, age, status) VALUES ('z', 18, 1);";
    
    private static final String DML_DELETE_SQL = "DELETE FROM user WHERE id = 1;";
    
    private static final String DML_UPDATE_SQL = "UPDATE user SET name = 'j' WHERE id = 1;";
    
    private static final String DDL_CREATE_SQL = "CREATE TABLE user (id BIGINT(20) PRIMARY KEY AUTO_INCREMENT, name VARCHAR(20), age INT(2), status INT(1));";
    
    private static final String DDL_DROP_SQL = "DROP TABLE user;";
    
    private static final String DDL_ALTER_SQL = "ALTER TABLE user CHANGE name name_new VARCHAR(20);";
    
    private static final String DDL_SHOW_SQL = "SHOW COLUMNS FROM user;";
    
    private static final List<String> MYSQL_FORMAT_SQL_LIST;
    
    static {
        MYSQL_FORMAT_SQL_LIST = Arrays.asList(DML_SELECT_SQL, DML_INSERT_SQL, DML_DELETE_SQL, DML_UPDATE_SQL, DDL_CREATE_SQL, DDL_DROP_SQL, DDL_ALTER_SQL, DDL_SHOW_SQL);
    }
    
    // CHECKSTYLE:OFF
    public static void main(final String[] args) {
        // CHECKSTYLE:ON
        MYSQL_FORMAT_SQL_LIST.forEach(each -> {
            CacheOption cacheOption = new CacheOption(128, 1024L);
            SQLFormatEngine sqlFormatEngine = new SQLFormatEngine("MySQL", cacheOption);
            Properties props = new Properties();
            props.setProperty("parameterized", Boolean.FALSE.toString());
            System.out.println(sqlFormatEngine.format(each, false, props));
        });
    }
}
