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

package org.apache.shardingsphere.proxy.backend.hbase.result;

import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import java.util.Properties;

/**
 * SQL Statement which supported in HBase.
 */
public final class HBaseSupportedSQLStatement {
    
    public static final String HBASE_DATABASE_TABLE_NAME = "t_test_order";
    
    private static final String SELECT_STATEMENT = "select /*+ hbase */ * from t_order where id = 1";
    
    private static final String SHOW_TABLE_STATEMENT = "show /*+ hbase */ tables";
    
    private static final String SHOW_CREATE_TABLE_STATEMENT = "show /*+ hbase */ create table t_test_order";
    
    private static final String UPDATE_STATEMENT = "update /*+ hbase */ t_test_order set age = 10 where rowKey = 1";
    
    private static final String DELETE_STATEMENT = "delete /*+ hbase */ from t_test_order where rowKey = 'kid'";
    
    private static final String INSERT_STATEMENT = "insert /*+ hbase */ into t_test_order(rowKey, v1, v2) values(1, 2, 3)";
    
    private static final String SHOW_DATABASES = "show /*+ hbase */ databases";
    
    private static final String FLUSH_TABLES = "flush /*+ hbase */ tables t_test";
    
    /**
     * parse sql statement.
     *
     * @param sql sql
     * @return SQLStatement Object.
     */
    public static SQLStatement parseSQLStatement(final String sql) {
        return new SQLVisitorEngine("MySQL", "STATEMENT", false, new Properties()).visit(new SQLParserEngine("MySQL",
                new CacheOption(128, 4)).parse(sql, false));
    }
    
    /**
     * get SQL Statement for test.
     * @return SQLStatement.
     */
    public static String getInsertStatement() {
        return INSERT_STATEMENT;
    }
    
    /**
     * get SQL Statement for test.
     * @return SQLStatement.
     */
    public static String getDeleteStatement() {
        return DELETE_STATEMENT;
    }
    
    /**
     * get SQL Statement for test.
     * @return SQLStatement.
     */
    public static String getUpdateStatement() {
        return UPDATE_STATEMENT;
    }
    
    /**
     * get SQL Statement for test.
     * @return SQLStatement.
     */
    public static String getSelectStatement() {
        return SELECT_STATEMENT;
    }
    
    /**
     * get SQL Statement for test.
     * @return SQLStatement.
     */
    public static String getShowCreateTableStatement() {
        return SHOW_CREATE_TABLE_STATEMENT;
    }
    
    /**
     * get SQL Statement for test.
     * @return SQLStatement.
     */
    public static String getShowTableStatement() {
        return SHOW_TABLE_STATEMENT;
    }
    
    /**
     * get SQL Statement for test.
     * @return SQLStatement.
     */
    public static String getShowDatabaseStatement() {
        return SHOW_DATABASES;
    }
    
    /**
     * get SQL Statement for test.
     * @return SQLStatement.
     */
    public static String getFlushTablesStatement() {
        return FLUSH_TABLES;
    }
}
