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
import org.apache.shardingsphere.sql.parser.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * SQL Statement which supported in HBase.
 */
public final class HBaseSupportedSQLStatement {
    
    public static final String HBASE_DATABASE_TABLE_NAME = "t_test_order";
    
    private static final String INSERT = "insert /*+ hbase */ into t_test_order(rowKey, v1, v2) values(1, 2, 3)";
    
    private static final String UPDATE = "update /*+ hbase */ t_test_order set age = 10 where rowKey = 1";
    
    private static final String DELETE = "delete /*+ hbase */ from t_test_order where rowKey = 'kid'";
    
    private static final String SELECT = "select /*+ hbase */ * from t_order where id = 1";
    
    private static final String SHOW_DATABASES = "show /*+ hbase */ databases";
    
    private static final String SHOW_TABLES = "show /*+ hbase */ tables";
    
    private static final String SHOW_CREATE_TABLE = "show /*+ hbase */ create table t_test_order";
    
    private static final String FLUSH_TABLES = "flush /*+ hbase */ tables t_test";
    
    /**
     * parse SQL statement.
     *
     * @param sql SQL
     * @return SQL statement
     */
    public static SQLStatement parseSQLStatement(final String sql) {
        return new SQLStatementVisitorEngine("MySQL").visit(new SQLParserEngine("MySQL", new CacheOption(128, 4)).parse(sql, false));
    }
    
    /**
     * Get insert statement.
     * 
     * @return insert statement
     */
    public static String getInsertStatement() {
        return INSERT;
    }
    
    /**
     * Get update statement.
     *
     * @return update statement
     */
    public static String getUpdateStatement() {
        return UPDATE;
    }
    
    /**
     * Get delete statement.
     * 
     * @return delete statement
     */
    public static String getDeleteStatement() {
        return DELETE;
    }
    
    /**
     * Get select statement.
     * 
     * @return select statement
     */
    public static String getSelectStatement() {
        return SELECT;
    }
    
    /**
     * Get show databases statement.
     *
     * @return show databases statement
     */
    public static String getShowDatabaseStatement() {
        return SHOW_DATABASES;
    }
    
    /**
     * Get show tables statement.
     * 
     * @return show tables statement
     */
    public static String getShowTablesStatement() {
        return SHOW_TABLES;
    }
    
    /**
     * Get show create table statement.
     *
     * @return show create table statement
     */
    public static String getShowCreateTableStatement() {
        return SHOW_CREATE_TABLE;
    }
    
    /**
     * Get flush tables statement.
     * 
     * @return flush tables statement
     */
    public static String getFlushTablesStatement() {
        return FLUSH_TABLES;
    }
}
