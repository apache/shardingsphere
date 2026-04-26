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

package org.apache.shardingsphere.mcp.bootstrap.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Lightweight JDBC driver for bootstrap tests that only need stable H2-like metadata.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BootstrapMockRuntimeDriver implements Driver {
    
    private static final BootstrapMockRuntimeDriver INSTANCE = new BootstrapMockRuntimeDriver();
    
    private static final String JDBC_URL_PREFIX = "jdbc:bootstrap-mock:";
    
    private static final String DATABASE_PRODUCT_NAME = "H2";
    
    private static final String DATABASE_PRODUCT_VERSION = "2.2.224";
    
    private static final String DEFAULT_SCHEMA = "public";
    
    private static final List<String> TABLE_NAMES = List.of("order_items", "orders");
    
    private static final List<String> VIEW_NAMES = List.of("active_orders");
    
    private static final Map<String, List<String>> OBJECT_COLUMNS = createObjectColumns();
    
    private static final Map<String, List<String>> TABLE_INDEXES = Map.of(
            "orders", List.of("PRIMARY_KEY_C", "idx_orders_status"),
            "order_items", List.of("PRIMARY_KEY_C"));
    
    static {
        try {
            DriverManager.registerDriver(INSTANCE);
        } catch (final SQLException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    /**
     * Create one JDBC URL for the bootstrap mock runtime.
     *
     * @param databaseName database name
     * @return JDBC URL
     */
    public static String createJdbcUrl(final String databaseName) {
        return JDBC_URL_PREFIX + databaseName;
    }
    
    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        return acceptsURL(url) ? createConnection(url) : null;
    }
    
    @Override
    public boolean acceptsURL(final String url) {
        return null != url && url.startsWith(JDBC_URL_PREFIX);
    }
    
    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) {
        return new DriverPropertyInfo[0];
    }
    
    @Override
    public int getMajorVersion() {
        return 1;
    }
    
    @Override
    public int getMinorVersion() {
        return 0;
    }
    
    @Override
    public boolean jdbcCompliant() {
        return false;
    }
    
    @Override
    public Logger getParentLogger() {
        return Logger.getGlobal();
    }
    
    private static Map<String, List<String>> createObjectColumns() {
        Map<String, List<String>> result = new LinkedHashMap<>(3, 1F);
        result.put("orders", List.of("amount", "order_id", "status"));
        result.put("order_items", List.of("item_id", "order_id", "sku"));
        result.put("active_orders", List.of("order_id", "status"));
        return result;
    }
    
    private static Connection createConnection(final String jdbcUrl) {
        ConnectionState state = new ConnectionState(jdbcUrl);
        return (Connection) Proxy.newProxyInstance(BootstrapMockRuntimeDriver.class.getClassLoader(),
                new Class[]{Connection.class}, (proxy, method, args) -> handleConnectionMethod(state, proxy, method, args));
    }
    
    private static Object handleConnectionMethod(final ConnectionState state, final Object proxy, final Method method, final Object[] args) throws SQLException {
        String methodName = method.getName();
        if ("getMetaData".equals(methodName)) {
            return createDatabaseMetaData(state.jdbcUrl);
        }
        if ("createStatement".equals(methodName)) {
            return createStatement(state);
        }
        if ("setSchema".equals(methodName)) {
            state.schema = Objects.toString(args[0], DEFAULT_SCHEMA);
            return null;
        }
        if ("getSchema".equals(methodName)) {
            return state.schema;
        }
        if ("setAutoCommit".equals(methodName) || "commit".equals(methodName) || "rollback".equals(methodName) || "close".equals(methodName)) {
            state.closed = "close".equals(methodName);
            return null;
        }
        if ("getAutoCommit".equals(methodName)) {
            return true;
        }
        if ("isClosed".equals(methodName)) {
            return state.closed;
        }
        if ("isValid".equals(methodName)) {
            return !state.closed;
        }
        return handleCommonObjectMethod(proxy, method, args);
    }
    
    private static DatabaseMetaData createDatabaseMetaData(final String jdbcUrl) {
        return (DatabaseMetaData) Proxy.newProxyInstance(BootstrapMockRuntimeDriver.class.getClassLoader(),
                new Class[]{DatabaseMetaData.class}, (proxy, method, args) -> handleDatabaseMetaDataMethod(jdbcUrl, proxy, method, args));
    }
    
    private static Object handleDatabaseMetaDataMethod(final String jdbcUrl, final Object proxy, final Method method, final Object[] args) {
        String methodName = method.getName();
        if ("getDatabaseProductName".equals(methodName)) {
            return DATABASE_PRODUCT_NAME;
        }
        if ("getDatabaseProductVersion".equals(methodName)) {
            return DATABASE_PRODUCT_VERSION;
        }
        if ("getURL".equals(methodName)) {
            return jdbcUrl;
        }
        if ("getTables".equals(methodName)) {
            return createTablesResultSet((String[]) args[3]);
        }
        if ("getColumns".equals(methodName)) {
            return createColumnsResultSet(Objects.toString(args[2], ""));
        }
        if ("getIndexInfo".equals(methodName)) {
            return createIndexesResultSet(Objects.toString(args[2], ""));
        }
        return handleCommonObjectMethod(proxy, method, args);
    }
    
    private static Statement createStatement(final ConnectionState connectionState) {
        StatementState state = new StatementState(connectionState);
        return (Statement) Proxy.newProxyInstance(BootstrapMockRuntimeDriver.class.getClassLoader(),
                new Class[]{Statement.class}, (proxy, method, args) -> handleStatementMethod(state, proxy, method, args));
    }
    
    private static Object handleStatementMethod(final StatementState state, final Object proxy, final Method method, final Object[] args) throws SQLException {
        String methodName = method.getName();
        if ("execute".equals(methodName)) {
            state.resultSet = createQueryResultSet(Objects.toString(args[0], ""));
            state.updateCount = -1;
            return true;
        }
        if ("executeQuery".equals(methodName)) {
            state.resultSet = createStatementQueryResultSet(Objects.toString(args[0], ""));
            state.updateCount = -1;
            return state.resultSet;
        }
        if ("getResultSet".equals(methodName)) {
            return state.resultSet;
        }
        if ("getUpdateCount".equals(methodName)) {
            return state.updateCount;
        }
        if ("setMaxRows".equals(methodName)) {
            state.maxRows = (int) args[0];
            return null;
        }
        if ("getMaxRows".equals(methodName)) {
            return state.maxRows;
        }
        if ("setQueryTimeout".equals(methodName)) {
            state.queryTimeout = (int) args[0];
            return null;
        }
        if ("getQueryTimeout".equals(methodName)) {
            return state.queryTimeout;
        }
        if ("getConnection".equals(methodName)) {
            return state.getConnection();
        }
        if ("close".equals(methodName)) {
            state.closed = true;
            return null;
        }
        if ("isClosed".equals(methodName)) {
            return state.closed;
        }
        return handleCommonObjectMethod(proxy, method, args);
    }
    
    private static ResultSet createTablesResultSet(final String[] types) {
        List<String> actualTypes = null == types ? List.of("TABLE", "VIEW") : List.of(types);
        List<List<Object>> rows = new LinkedList<>();
        if (actualTypes.contains("TABLE")) {
            for (String each : TABLE_NAMES) {
                rows.add(Arrays.asList(null, DEFAULT_SCHEMA, each));
            }
        }
        if (actualTypes.contains("VIEW")) {
            for (String each : VIEW_NAMES) {
                rows.add(Arrays.asList(null, DEFAULT_SCHEMA, each));
            }
        }
        return createResultSet(List.of(
                new ColumnDefinition("TABLE_CAT", "VARCHAR", false),
                new ColumnDefinition("TABLE_SCHEM", "VARCHAR", false),
                new ColumnDefinition("TABLE_NAME", "VARCHAR", true)), rows);
    }
    
    private static ResultSet createColumnsResultSet(final String objectName) {
        List<List<Object>> rows = new LinkedList<>();
        for (String each : OBJECT_COLUMNS.getOrDefault(objectName, List.of())) {
            rows.add(List.of(each));
        }
        return createResultSet(List.of(new ColumnDefinition("COLUMN_NAME", "VARCHAR", true)), rows);
    }
    
    private static ResultSet createIndexesResultSet(final String tableName) {
        List<List<Object>> rows = new LinkedList<>();
        for (String each : TABLE_INDEXES.getOrDefault(tableName, List.of())) {
            rows.add(List.of(each));
        }
        return createResultSet(List.of(new ColumnDefinition("INDEX_NAME", "VARCHAR", true)), rows);
    }
    
    private static ResultSet createStatementQueryResultSet(final String sql) throws SQLSyntaxErrorException {
        String normalizedSql = normalizeSql(sql);
        if ("SELECT SEQUENCE_SCHEMA, SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES".equals(normalizedSql)) {
            return createResultSet(List.of(
                    new ColumnDefinition("SEQUENCE_SCHEMA", "VARCHAR", true),
                    new ColumnDefinition("SEQUENCE_NAME", "VARCHAR", true)), List.of(List.of(DEFAULT_SCHEMA, "order_seq")));
        }
        if ("SELECT VERSION()".equals(normalizedSql)) {
            return createResultSet(List.of(new ColumnDefinition("VERSION()", "VARCHAR", true)), List.of(List.of(DATABASE_PRODUCT_VERSION)));
        }
        if ("SELECT @@VERSION_COMMENT".equals(normalizedSql)) {
            return createResultSet(List.of(new ColumnDefinition("@@version_comment", "VARCHAR", true)), List.of(List.of("bootstrap-mock")));
        }
        if ("SELECT 1".equals(normalizedSql)) {
            return createSelectOneResultSet();
        }
        throw new SQLSyntaxErrorException("Unsupported bootstrap mock query: " + sql);
    }
    
    private static ResultSet createQueryResultSet(final String sql) throws SQLSyntaxErrorException {
        String normalizedSql = normalizeSql(sql);
        if ("SELECT 1".equals(normalizedSql)) {
            return createSelectOneResultSet();
        }
        throw new SQLSyntaxErrorException("Unsupported bootstrap mock execute: " + sql);
    }
    
    private static ResultSet createSelectOneResultSet() {
        return createResultSet(List.of(new ColumnDefinition("1", "INTEGER", true)), List.of(List.of(1)));
    }
    
    private static ResultSet createResultSet(final List<ColumnDefinition> columns, final List<List<Object>> rows) {
        ResultSetState state = new ResultSetState(columns, rows);
        return (ResultSet) Proxy.newProxyInstance(BootstrapMockRuntimeDriver.class.getClassLoader(),
                new Class[]{ResultSet.class}, (proxy, method, args) -> handleResultSetMethod(state, proxy, method, args));
    }
    
    private static Object handleResultSetMethod(final ResultSetState state, final Object proxy, final Method method, final Object[] args) {
        String methodName = method.getName();
        if ("next".equals(methodName)) {
            if (state.rowIndex + 1 < state.rows.size()) {
                state.rowIndex++;
                return true;
            }
            state.rowIndex = state.rows.size();
            return false;
        }
        if ("getString".equals(methodName)) {
            Object value = state.getValue(args[0]);
            return null == value ? null : String.valueOf(value);
        }
        if ("getInt".equals(methodName)) {
            Object value = state.getValue(args[0]);
            return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(String.valueOf(value));
        }
        if ("getObject".equals(methodName)) {
            return state.getValue(args[0]);
        }
        if ("findColumn".equals(methodName)) {
            return state.findColumn(String.valueOf(args[0])) + 1;
        }
        if ("getMetaData".equals(methodName)) {
            return createResultSetMetaData(state.columns);
        }
        if ("close".equals(methodName)) {
            state.closed = true;
            return null;
        }
        if ("isClosed".equals(methodName)) {
            return state.closed;
        }
        if ("wasNull".equals(methodName)) {
            return false;
        }
        return handleCommonObjectMethod(proxy, method, args);
    }
    
    private static ResultSetMetaData createResultSetMetaData(final List<ColumnDefinition> columns) {
        return (ResultSetMetaData) Proxy.newProxyInstance(BootstrapMockRuntimeDriver.class.getClassLoader(),
                new Class[]{ResultSetMetaData.class}, (proxy, method, args) -> handleResultSetMetaDataMethod(columns, proxy, method, args));
    }
    
    private static Object handleResultSetMetaDataMethod(final List<ColumnDefinition> columns, final Object proxy, final Method method, final Object[] args) {
        String methodName = method.getName();
        if ("getColumnCount".equals(methodName)) {
            return columns.size();
        }
        if ("getColumnLabel".equals(methodName) || "getColumnName".equals(methodName)) {
            return columns.get(((int) args[0]) - 1).label();
        }
        if ("getColumnTypeName".equals(methodName)) {
            return columns.get(((int) args[0]) - 1).typeName();
        }
        if ("getColumnType".equals(methodName)) {
            return columns.get(((int) args[0]) - 1).sqlType();
        }
        if ("isNullable".equals(methodName)) {
            return columns.get(((int) args[0]) - 1).notNull() ? ResultSetMetaData.columnNoNulls : ResultSetMetaData.columnNullable;
        }
        if ("isSigned".equals(methodName)) {
            return Types.INTEGER == columns.get(((int) args[0]) - 1).sqlType();
        }
        if ("getColumnDisplaySize".equals(methodName)) {
            return 32;
        }
        return handleCommonObjectMethod(proxy, method, args);
    }
    
    private static Object handleCommonObjectMethod(final Object proxy, final Method method, final Object[] args) {
        String methodName = method.getName();
        if ("toString".equals(methodName)) {
            return proxy.getClass().getName();
        }
        if ("hashCode".equals(methodName)) {
            return System.identityHashCode(proxy);
        }
        if ("equals".equals(methodName)) {
            return proxy == args[0];
        }
        if ("unwrap".equals(methodName)) {
            return null;
        }
        if ("isWrapperFor".equals(methodName)) {
            return false;
        }
        return getDefaultValue(method.getReturnType());
    }
    
    private static Object getDefaultValue(final Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (boolean.class == returnType) {
            return false;
        }
        if (int.class == returnType || short.class == returnType || byte.class == returnType) {
            return 0;
        }
        if (long.class == returnType) {
            return 0L;
        }
        if (float.class == returnType) {
            return 0F;
        }
        if (double.class == returnType) {
            return 0D;
        }
        if (char.class == returnType) {
            return '\0';
        }
        return null;
    }
    
    private static String normalizeSql(final String sql) {
        return sql.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ENGLISH);
    }
    
    private static final class ConnectionState {
        
        private final String jdbcUrl;
        
        private boolean closed;
        
        private String schema = DEFAULT_SCHEMA;
        
        private ConnectionState(final String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
        }
    }
    
    private static final class StatementState {
        
        private final ConnectionState connectionState;
        
        private ResultSet resultSet;
        
        private int updateCount = -1;
        
        private int maxRows;
        
        private int queryTimeout;
        
        private boolean closed;
        
        private StatementState(final ConnectionState connectionState) {
            this.connectionState = connectionState;
        }
        
        private Connection getConnection() {
            return createConnection(connectionState.jdbcUrl);
        }
    }
    
    private static final class ResultSetState {
        
        private final List<ColumnDefinition> columns;
        
        private final List<List<Object>> rows;
        
        private int rowIndex = -1;
        
        private boolean closed;
        
        private ResultSetState(final List<ColumnDefinition> columns, final List<List<Object>> rows) {
            this.columns = columns;
            this.rows = rows;
        }
        
        private Object getValue(final Object key) {
            List<Object> row = rows.get(rowIndex);
            return key instanceof Integer ? row.get(((int) key) - 1) : row.get(findColumn(String.valueOf(key)));
        }
        
        private int findColumn(final String columnLabel) {
            for (int index = 0; index < columns.size(); index++) {
                if (columns.get(index).label().equalsIgnoreCase(columnLabel)) {
                    return index;
                }
            }
            throw new IllegalArgumentException("Unknown column label: " + columnLabel);
        }
    }
    
    private record ColumnDefinition(String label, String typeName, boolean notNull) {
        
        private int sqlType() {
            return "INTEGER".equalsIgnoreCase(typeName) ? Types.INTEGER : Types.VARCHAR;
        }
    }
}
