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

import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDBC driver that delegates regular SQL to H2 and intercepts encrypt and mask DistSQL.
 */
public final class FeatureDistSQLTestDriver implements Driver {
    
    private static final String URL_PREFIX = "jdbc:feature-distsql:";
    
    private static final Pattern SHOW_ENCRYPT_RULES_PATTERN = Pattern.compile("(?is)^\\s*SHOW\\s+ENCRYPT\\s+RULES\\s+FROM\\s+([A-Za-z0-9_$]+)\\s*$");
    
    private static final Pattern SHOW_ENCRYPT_TABLE_RULE_PATTERN = Pattern.compile("(?is)^\\s*SHOW\\s+ENCRYPT\\s+TABLE\\s+RULE\\s+([A-Za-z0-9_$]+)\\s+FROM\\s+([A-Za-z0-9_$]+)\\s*$");
    
    private static final Pattern SHOW_MASK_RULES_PATTERN = Pattern.compile("(?is)^\\s*SHOW\\s+MASK\\s+RULES\\s+FROM\\s+([A-Za-z0-9_$]+)\\s*$");
    
    private static final Pattern SHOW_MASK_RULE_PATTERN = Pattern.compile("(?is)^\\s*SHOW\\s+MASK\\s+RULE\\s+([A-Za-z0-9_$]+)\\s+FROM\\s+([A-Za-z0-9_$]+)\\s*$");
    
    private static final Pattern CREATE_OR_ALTER_ENCRYPT_RULE_PATTERN =
            Pattern.compile("(?is)^\\s*(CREATE|ALTER)\\s+ENCRYPT\\s+RULE\\s+([A-Za-z0-9_$]+)\\s*\\(");
    
    private static final Pattern DROP_ENCRYPT_RULE_PATTERN = Pattern.compile("(?is)^\\s*DROP\\s+ENCRYPT\\s+RULE\\s+([A-Za-z0-9_$]+)\\s*$");
    
    private static final Pattern CREATE_OR_ALTER_MASK_RULE_PATTERN =
            Pattern.compile("(?is)^\\s*(CREATE|ALTER)\\s+MASK\\s+RULE\\s+([A-Za-z0-9_$]+)\\s*\\(");
    
    private static final Pattern DROP_MASK_RULE_PATTERN = Pattern.compile("(?is)^\\s*DROP\\s+MASK\\s+RULE\\s+([A-Za-z0-9_$]+)\\s*$");
    
    private static final Pattern INFORMATION_SCHEMA_COLUMNS_PATTERN = Pattern.compile(
            "(?is)^\\s*SELECT\\s+DISTINCT\\s+COLUMN_NAME\\s+FROM\\s+INFORMATION_SCHEMA\\.COLUMNS\\s+WHERE\\s+"
                    + "(?:TABLE_SCHEMA\\s*=\\s*'([^']*)'\\s+AND\\s+)?TABLE_NAME\\s*=\\s*'([^']+)'\\s+AND\\s+COLUMN_NAME\\s+IN\\s*\\(([^)]*)\\)\\s*$");
    
    private static final ConcurrentMap<String, FeatureRuleCatalog> RULE_CATALOGS = new ConcurrentHashMap<>();
    
    static {
        try {
            DriverManager.registerDriver(new FeatureDistSQLTestDriver());
        } catch (final SQLException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    /**
     * Convert one ordinary JDBC URL into a feature DistSQL test URL.
     *
     * @param jdbcUrl ordinary JDBC URL
     * @return feature DistSQL JDBC URL
     */
    public static String createJdbcUrl(final String jdbcUrl) {
        return jdbcUrl.startsWith("jdbc:") ? URL_PREFIX + jdbcUrl.substring("jdbc:".length()) : jdbcUrl;
    }
    
    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        try {
            Class.forName("org.h2.Driver");
        } catch (final ClassNotFoundException ex) {
            throw new IllegalStateException("H2 driver is not available for feature workflow tests.", ex);
        }
        String actualJdbcUrl = "jdbc:" + url.substring(URL_PREFIX.length());
        Connection connection = DriverManager.getConnection(actualJdbcUrl, info);
        FeatureRuleCatalog ruleCatalog = RULE_CATALOGS.computeIfAbsent(actualJdbcUrl, key -> new FeatureRuleCatalog());
        return createConnectionProxy(connection, ruleCatalog);
    }
    
    @Override
    public boolean acceptsURL(final String url) {
        return null != url && url.startsWith(URL_PREFIX);
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
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Parent logger is not supported.");
    }
    
    private Connection createConnectionProxy(final Connection delegate, final FeatureRuleCatalog ruleCatalog) {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                new ConnectionInvocationHandler(delegate, ruleCatalog));
    }
    
    private ResultSet createResultSetProxy(final List<Map<String, Object>> rows) {
        return (ResultSet) Proxy.newProxyInstance(ResultSet.class.getClassLoader(), new Class<?>[]{ResultSet.class}, new ResultSetInvocationHandler(rows));
    }
    
    private static Object getDefaultValue(final Class<?> returnType) {
        if (Void.TYPE == returnType) {
            return null;
        }
        if (Boolean.TYPE == returnType) {
            return false;
        }
        if (Byte.TYPE == returnType) {
            return (byte) 0;
        }
        if (Short.TYPE == returnType) {
            return (short) 0;
        }
        if (Integer.TYPE == returnType) {
            return 0;
        }
        if (Long.TYPE == returnType) {
            return 0L;
        }
        if (Float.TYPE == returnType) {
            return 0F;
        }
        if (Double.TYPE == returnType) {
            return 0D;
        }
        if (Character.TYPE == returnType) {
            return '\0';
        }
        return null;
    }
    
    private final class ConnectionInvocationHandler implements InvocationHandler {
        
        private final Connection delegate;
        
        private final FeatureRuleCatalog ruleCatalog;
        
        private ConnectionInvocationHandler(final Connection delegate, final FeatureRuleCatalog ruleCatalog) {
            this.delegate = delegate;
            this.ruleCatalog = ruleCatalog;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if ("createStatement".equals(method.getName()) && (null == args || 0 == args.length)) {
                Statement statement;
                try {
                    statement = (Statement) method.invoke(delegate, args);
                } catch (final InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
                return Proxy.newProxyInstance(Statement.class.getClassLoader(), new Class<?>[]{Statement.class},
                        new StatementInvocationHandler(statement, ruleCatalog));
            }
            try {
                return method.invoke(delegate, args);
            } catch (final InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
    
    private final class StatementInvocationHandler implements InvocationHandler {
        
        private final Statement delegate;
        
        private final FeatureRuleCatalog ruleCatalog;
        
        private StatementInvocationHandler(final Statement delegate, final FeatureRuleCatalog ruleCatalog) {
            this.delegate = delegate;
            this.ruleCatalog = ruleCatalog;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            String methodName = method.getName();
            if ("executeQuery".equals(methodName) && 1 == method.getParameterCount() && args[0] instanceof String) {
                List<Map<String, Object>> rows = ruleCatalog.tryExecuteQuery(delegate, String.valueOf(args[0]));
                if (null != rows) {
                    return createResultSetProxy(rows);
                }
            }
            if ("execute".equals(methodName) && 1 == method.getParameterCount() && args[0] instanceof String) {
                if (ruleCatalog.tryExecute(String.valueOf(args[0]))) {
                    return false;
                }
            }
            try {
                return method.invoke(delegate, args);
            } catch (final InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
    
    private static final class ResultSetInvocationHandler implements InvocationHandler {
        
        private final List<Map<String, Object>> rows;
        
        private final List<String> columnNames;
        
        private int currentIndex = -1;
        
        private boolean closed;
        
        private ResultSetInvocationHandler(final List<Map<String, Object>> rows) {
            this.rows = rows;
            columnNames = rows.isEmpty() ? List.of() : new LinkedList<>(rows.get(0).keySet());
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            switch (method.getName()) {
                case "next":
                    return !closed && ++currentIndex < rows.size();
                case "getObject":
                    return getObject(args);
                case "getMetaData":
                    return Proxy.newProxyInstance(ResultSetMetaData.class.getClassLoader(), new Class<?>[]{ResultSetMetaData.class},
                            new ResultSetMetaDataInvocationHandler(columnNames));
                case "close":
                    closed = true;
                    return null;
                case "isClosed":
                    return closed;
                case "wasNull":
                    return false;
                case "unwrap":
                    return proxy;
                case "isWrapperFor":
                    return false;
                default:
                    return getDefaultValue(method.getReturnType());
            }
        }
        
        private Object getObject(final Object[] args) {
            if (0 > currentIndex || currentIndex >= rows.size()) {
                return null;
            }
            Map<String, Object> row = rows.get(currentIndex);
            if (args[0] instanceof Integer) {
                int columnIndex = (Integer) args[0];
                return row.get(columnNames.get(columnIndex - 1));
            }
            return row.get(String.valueOf(args[0]));
        }
    }
    
    private static final class ResultSetMetaDataInvocationHandler implements InvocationHandler {
        
        private final List<String> columnNames;
        
        private ResultSetMetaDataInvocationHandler(final List<String> columnNames) {
            this.columnNames = columnNames;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) {
            switch (method.getName()) {
                case "getColumnCount":
                    return columnNames.size();
                case "getColumnLabel":
                case "getColumnName":
                    return columnNames.get((Integer) args[0] - 1);
                case "getColumnTypeName":
                    return "VARCHAR";
                case "getColumnType":
                    return java.sql.Types.VARCHAR;
                case "isNullable":
                    return ResultSetMetaData.columnNullable;
                case "unwrap":
                    return proxy;
                case "isWrapperFor":
                    return false;
                default:
                    return getDefaultValue(method.getReturnType());
            }
        }
    }
    
    private static final class FeatureRuleCatalog {
        
        private final Map<String, Map<String, EncryptRuleRow>> encryptRulesByTable = new LinkedHashMap<>(4, 1F);
        
        private final Map<String, Map<String, MaskRuleRow>> maskRulesByTable = new LinkedHashMap<>(4, 1F);
        
        private synchronized boolean tryExecute(final String sql) {
            Matcher encryptMatcher = CREATE_OR_ALTER_ENCRYPT_RULE_PATTERN.matcher(sql);
            if (encryptMatcher.find()) {
                encryptRulesByTable.put(encryptMatcher.group(2), parseEncryptRules(sql, encryptMatcher.group(2)));
                return true;
            }
            Matcher dropEncryptMatcher = DROP_ENCRYPT_RULE_PATTERN.matcher(sql);
            if (dropEncryptMatcher.find()) {
                encryptRulesByTable.remove(dropEncryptMatcher.group(1));
                return true;
            }
            Matcher maskMatcher = CREATE_OR_ALTER_MASK_RULE_PATTERN.matcher(sql);
            if (maskMatcher.find()) {
                maskRulesByTable.put(maskMatcher.group(2), parseMaskRules(sql, maskMatcher.group(2)));
                return true;
            }
            Matcher dropMaskMatcher = DROP_MASK_RULE_PATTERN.matcher(sql);
            if (dropMaskMatcher.find()) {
                maskRulesByTable.remove(dropMaskMatcher.group(1));
                return true;
            }
            return false;
        }
        
        private synchronized List<Map<String, Object>> tryExecuteQuery(final Statement statement, final String sql) throws SQLException {
            if ("SHOW ENCRYPT ALGORITHM PLUGINS".equalsIgnoreCase(sql.trim())) {
                return List.of(createPluginRow("AES"), createPluginRow("MD5"));
            }
            if ("SHOW MASK ALGORITHM PLUGINS".equalsIgnoreCase(sql.trim())) {
                return List.of(createPluginRow("MASK_FROM_X_TO_Y"), createPluginRow("KEEP_FIRST_N_LAST_M"), createPluginRow("MD5"));
            }
            Matcher showEncryptRulesMatcher = SHOW_ENCRYPT_RULES_PATTERN.matcher(sql);
            if (showEncryptRulesMatcher.find()) {
                return createEncryptRuleRows("");
            }
            Matcher showEncryptTableRuleMatcher = SHOW_ENCRYPT_TABLE_RULE_PATTERN.matcher(sql);
            if (showEncryptTableRuleMatcher.find()) {
                return createEncryptRuleRows(showEncryptTableRuleMatcher.group(1));
            }
            Matcher showMaskRulesMatcher = SHOW_MASK_RULES_PATTERN.matcher(sql);
            if (showMaskRulesMatcher.find()) {
                return createMaskRuleRows("");
            }
            Matcher showMaskRuleMatcher = SHOW_MASK_RULE_PATTERN.matcher(sql);
            if (showMaskRuleMatcher.find()) {
                return createMaskRuleRows(showMaskRuleMatcher.group(1));
            }
            Matcher informationSchemaColumnsMatcher = INFORMATION_SCHEMA_COLUMNS_PATTERN.matcher(sql);
            if (informationSchemaColumnsMatcher.find()) {
                return createInformationSchemaColumnRows(statement.getConnection(),
                        stripQuotes(informationSchemaColumnsMatcher.group(1)),
                        stripQuotes(informationSchemaColumnsMatcher.group(2)),
                        parseColumnLiterals(informationSchemaColumnsMatcher.group(3)));
            }
            return null;
        }
        
        private List<Map<String, Object>> createEncryptRuleRows(final String tableName) {
            List<Map<String, Object>> result = new LinkedList<>();
            for (Entry<String, Map<String, EncryptRuleRow>> entry : encryptRulesByTable.entrySet()) {
                if (!tableName.isEmpty() && !tableName.equalsIgnoreCase(entry.getKey())) {
                    continue;
                }
                for (EncryptRuleRow each : entry.getValue().values()) {
                    Map<String, Object> row = new LinkedHashMap<>(12, 1F);
                    row.put("table", each.getTableName());
                    row.put("logic_column", each.getLogicColumn());
                    row.put("cipher_column", each.getCipherColumn());
                    row.put("assisted_query_column", each.getAssistedQueryColumn());
                    row.put("like_query_column", each.getLikeQueryColumn());
                    row.put("encryptor_type", each.getEncryptAlgorithmType());
                    row.put("encryptor_props", each.getEncryptAlgorithmProperties());
                    row.put("assisted_query_type", each.getAssistedQueryAlgorithmType());
                    row.put("assisted_query_props", each.getAssistedQueryAlgorithmProperties());
                    row.put("like_query_type", each.getLikeQueryAlgorithmType());
                    row.put("like_query_props", each.getLikeQueryAlgorithmProperties());
                    result.add(row);
                }
            }
            return result;
        }
        
        private List<Map<String, Object>> createMaskRuleRows(final String tableName) {
            List<Map<String, Object>> result = new LinkedList<>();
            for (Entry<String, Map<String, MaskRuleRow>> entry : maskRulesByTable.entrySet()) {
                if (!tableName.isEmpty() && !tableName.equalsIgnoreCase(entry.getKey())) {
                    continue;
                }
                for (MaskRuleRow each : entry.getValue().values()) {
                    Map<String, Object> row = new LinkedHashMap<>(6, 1F);
                    row.put("table", each.getTableName());
                    row.put("column", each.getColumnName());
                    row.put("logic_column", each.getColumnName());
                    row.put("algorithm_type", each.getAlgorithmType());
                    row.put("algorithm_props", each.getAlgorithmProperties());
                    row.put("mask_algorithm", each.getAlgorithmType());
                    row.put("props", each.getAlgorithmProperties());
                    result.add(row);
                }
            }
            return result;
        }
        
        private Map<String, EncryptRuleRow> parseEncryptRules(final String sql, final String tableName) {
            Map<String, EncryptRuleRow> result = new LinkedHashMap<>(4, 1F);
            for (String each : extractColumnSegments(sql)) {
                EncryptRuleRow row = parseEncryptRule(each, tableName);
                result.put(row.getLogicColumn(), row);
            }
            return result;
        }
        
        private Map<String, MaskRuleRow> parseMaskRules(final String sql, final String tableName) {
            Map<String, MaskRuleRow> result = new LinkedHashMap<>(4, 1F);
            for (String each : extractColumnSegments(sql)) {
                MaskRuleRow row = parseMaskRule(each, tableName);
                result.put(row.getColumnName(), row);
            }
            return result;
        }
        
        private EncryptRuleRow parseEncryptRule(final String segment, final String tableName) {
            String logicColumn = "";
            String cipherColumn = "";
            String assistedQueryColumn = "";
            String likeQueryColumn = "";
            AlgorithmDefinition encryptAlgorithm = AlgorithmDefinition.empty();
            AlgorithmDefinition assistedQueryAlgorithm = AlgorithmDefinition.empty();
            AlgorithmDefinition likeQueryAlgorithm = AlgorithmDefinition.empty();
            for (String each : splitTopLevelTokens(stripWrappingParentheses(segment))) {
                String actualToken = each.trim();
                if (actualToken.startsWith("NAME=")) {
                    logicColumn = readAssignmentValue(actualToken);
                    continue;
                }
                if (actualToken.startsWith("CIPHER=")) {
                    cipherColumn = readAssignmentValue(actualToken);
                    continue;
                }
                if (actualToken.startsWith("ASSISTED_QUERY=")) {
                    assistedQueryColumn = readAssignmentValue(actualToken);
                    continue;
                }
                if (actualToken.startsWith("LIKE_QUERY=")) {
                    likeQueryColumn = readAssignmentValue(actualToken);
                    continue;
                }
                if (actualToken.startsWith("ENCRYPT_ALGORITHM(")) {
                    encryptAlgorithm = parseAlgorithmDefinition(readFunctionBody(actualToken, "ENCRYPT_ALGORITHM"));
                    continue;
                }
                if (actualToken.startsWith("ASSISTED_QUERY_ALGORITHM(")) {
                    assistedQueryAlgorithm = parseAlgorithmDefinition(readFunctionBody(actualToken, "ASSISTED_QUERY_ALGORITHM"));
                    continue;
                }
                if (actualToken.startsWith("LIKE_QUERY_ALGORITHM(")) {
                    likeQueryAlgorithm = parseAlgorithmDefinition(readFunctionBody(actualToken, "LIKE_QUERY_ALGORITHM"));
                }
            }
            return new EncryptRuleRow(tableName, logicColumn, cipherColumn, assistedQueryColumn, likeQueryColumn,
                    encryptAlgorithm.getType(), encryptAlgorithm.getProperties(),
                    assistedQueryAlgorithm.getType(), assistedQueryAlgorithm.getProperties(),
                    likeQueryAlgorithm.getType(), likeQueryAlgorithm.getProperties());
        }
        
        private MaskRuleRow parseMaskRule(final String segment, final String tableName) {
            String columnName = "";
            AlgorithmDefinition algorithm = AlgorithmDefinition.empty();
            for (String each : splitTopLevelTokens(stripWrappingParentheses(segment))) {
                String actualToken = each.trim();
                if (actualToken.startsWith("NAME=")) {
                    columnName = readAssignmentValue(actualToken);
                    continue;
                }
                if (actualToken.startsWith("TYPE(")) {
                    algorithm = parseAlgorithmDefinition(actualToken);
                }
            }
            return new MaskRuleRow(tableName, columnName, algorithm.getType(), algorithm.getProperties());
        }
        
        private AlgorithmDefinition parseAlgorithmDefinition(final String text) {
            String type = "";
            Map<String, String> props = Map.of();
            for (String each : splitTopLevelTokens(readFunctionBody(text, "TYPE"))) {
                String actualToken = each.trim();
                if (actualToken.startsWith("NAME=")) {
                    type = readAssignmentValue(actualToken).toUpperCase(Locale.ENGLISH);
                    continue;
                }
                if (actualToken.startsWith("PROPERTIES(")) {
                    props = WorkflowSqlUtils.createPropertyMap(readFunctionBody(actualToken, "PROPERTIES"));
                }
            }
            return new AlgorithmDefinition(type, props);
        }
        
        private List<String> extractColumnSegments(final String sql) {
            int columnsKeywordIndex = indexOfIgnoreCase(sql, "COLUMNS(");
            if (-1 == columnsKeywordIndex) {
                return List.of();
            }
            int openParenthesisIndex = sql.indexOf('(', columnsKeywordIndex);
            String body = readBalancedSection(sql, openParenthesisIndex);
            List<String> result = new LinkedList<>();
            int depth = 0;
            int startIndex = -1;
            for (int index = 0; index < body.length(); index++) {
                char current = body.charAt(index);
                if ('(' == current) {
                    if (0 == depth) {
                        startIndex = index;
                    }
                    depth++;
                    continue;
                }
                if (')' == current) {
                    depth--;
                    if (0 == depth && -1 != startIndex) {
                        result.add(body.substring(startIndex, index + 1));
                        startIndex = -1;
                    }
                }
            }
            return result;
        }
        
        private List<String> splitTopLevelTokens(final String text) {
            List<String> result = new LinkedList<>();
            int depth = 0;
            int startIndex = 0;
            for (int index = 0; index < text.length(); index++) {
                char current = text.charAt(index);
                if ('(' == current) {
                    depth++;
                    continue;
                }
                if (')' == current) {
                    depth--;
                    continue;
                }
                if (',' == current && 0 == depth) {
                    result.add(text.substring(startIndex, index).trim());
                    startIndex = index + 1;
                }
            }
            result.add(text.substring(startIndex).trim());
            return result.stream().filter(each -> !each.isEmpty()).toList();
        }
        
        private List<String> parseColumnLiterals(final String columnList) {
            List<String> result = new LinkedList<>();
            for (String each : columnList.split(",")) {
                String actualColumn = stripQuotes(each);
                if (!actualColumn.isEmpty()) {
                    result.add(actualColumn);
                }
            }
            return result;
        }
        
        private List<Map<String, Object>> createInformationSchemaColumnRows(final Connection connection, final String schemaName,
                                                                            final String tableName, final List<String> expectedColumnNames) throws SQLException {
            Map<String, String> expectedColumnNamesByLowerCase = new LinkedHashMap<>(expectedColumnNames.size(), 1F);
            for (String each : expectedColumnNames) {
                expectedColumnNamesByLowerCase.put(each.toLowerCase(Locale.ENGLISH), each);
            }
            List<Map<String, Object>> result = new LinkedList<>();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getColumns(null, emptyToNull(schemaName), tableName, null)) {
                while (resultSet.next()) {
                    String actualColumnName = resultSet.getString("COLUMN_NAME");
                    if (expectedColumnNamesByLowerCase.containsKey(actualColumnName.toLowerCase(Locale.ENGLISH))) {
                        Map<String, Object> row = new LinkedHashMap<>(1, 1F);
                        row.put("column_name", actualColumnName);
                        result.add(row);
                    }
                }
            }
            return result;
        }
        
        private String stripWrappingParentheses(final String value) {
            String actualValue = value.trim();
            return actualValue.startsWith("(") && actualValue.endsWith(")") ? actualValue.substring(1, actualValue.length() - 1) : actualValue;
        }
        
        private String readAssignmentValue(final String token) {
            return stripQuotes(token.substring(token.indexOf('=') + 1).trim());
        }
        
        private String readFunctionBody(final String token, final String functionName) {
            int functionIndex = indexOfIgnoreCase(token, functionName + "(");
            int openParenthesisIndex = token.indexOf('(', functionIndex);
            return readBalancedSection(token, openParenthesisIndex);
        }
        
        private String readBalancedSection(final String text, final int openParenthesisIndex) {
            int depth = 0;
            for (int index = openParenthesisIndex; index < text.length(); index++) {
                char current = text.charAt(index);
                if ('(' == current) {
                    depth++;
                    continue;
                }
                if (')' == current) {
                    depth--;
                    if (0 == depth) {
                        return text.substring(openParenthesisIndex + 1, index);
                    }
                }
            }
            return "";
        }
        
        private int indexOfIgnoreCase(final String text, final String fragment) {
            return text.toUpperCase(Locale.ENGLISH).indexOf(fragment.toUpperCase(Locale.ENGLISH));
        }
        
        private String stripQuotes(final String value) {
            String actualValue = value.trim();
            if (2 <= actualValue.length()) {
                char first = actualValue.charAt(0);
                char last = actualValue.charAt(actualValue.length() - 1);
                if ('\'' == first && '\'' == last || '"' == first && '"' == last) {
                    return actualValue.substring(1, actualValue.length() - 1);
                }
            }
            return actualValue;
        }
        
        private String emptyToNull(final String value) {
            return null == value || value.isEmpty() ? null : value;
        }
        
        private Map<String, Object> createPluginRow(final String type) {
            Map<String, Object> result = new LinkedHashMap<>(1, 1F);
            result.put("type", type);
            return result;
        }
    }
    
    private static final class AlgorithmDefinition {
        
        private final String type;
        
        private final Map<String, String> properties;
        
        private AlgorithmDefinition(final String type, final Map<String, String> properties) {
            this.type = type;
            this.properties = properties;
        }
        
        private static AlgorithmDefinition empty() {
            return new AlgorithmDefinition("", Map.of());
        }
        
        private String getType() {
            return type;
        }
        
        private Map<String, String> getProperties() {
            return properties;
        }
    }
    
    private static final class EncryptRuleRow {
        
        private final String tableName;
        
        private final String logicColumn;
        
        private final String cipherColumn;
        
        private final String assistedQueryColumn;
        
        private final String likeQueryColumn;
        
        private final String encryptAlgorithmType;
        
        private final Map<String, String> encryptAlgorithmProperties;
        
        private final String assistedQueryAlgorithmType;
        
        private final Map<String, String> assistedQueryAlgorithmProperties;
        
        private final String likeQueryAlgorithmType;
        
        private final Map<String, String> likeQueryAlgorithmProperties;
        
        private EncryptRuleRow(final String tableName, final String logicColumn, final String cipherColumn, final String assistedQueryColumn, final String likeQueryColumn,
                               final String encryptAlgorithmType, final Map<String, String> encryptAlgorithmProperties,
                               final String assistedQueryAlgorithmType, final Map<String, String> assistedQueryAlgorithmProperties,
                               final String likeQueryAlgorithmType, final Map<String, String> likeQueryAlgorithmProperties) {
            this.tableName = tableName;
            this.logicColumn = logicColumn;
            this.cipherColumn = cipherColumn;
            this.assistedQueryColumn = assistedQueryColumn;
            this.likeQueryColumn = likeQueryColumn;
            this.encryptAlgorithmType = encryptAlgorithmType;
            this.encryptAlgorithmProperties = encryptAlgorithmProperties;
            this.assistedQueryAlgorithmType = assistedQueryAlgorithmType;
            this.assistedQueryAlgorithmProperties = assistedQueryAlgorithmProperties;
            this.likeQueryAlgorithmType = likeQueryAlgorithmType;
            this.likeQueryAlgorithmProperties = likeQueryAlgorithmProperties;
        }
        
        private String getTableName() {
            return tableName;
        }
        
        private String getLogicColumn() {
            return logicColumn;
        }
        
        private String getCipherColumn() {
            return cipherColumn;
        }
        
        private String getAssistedQueryColumn() {
            return assistedQueryColumn;
        }
        
        private String getLikeQueryColumn() {
            return likeQueryColumn;
        }
        
        private String getEncryptAlgorithmType() {
            return encryptAlgorithmType;
        }
        
        private Map<String, String> getEncryptAlgorithmProperties() {
            return encryptAlgorithmProperties;
        }
        
        private String getAssistedQueryAlgorithmType() {
            return assistedQueryAlgorithmType;
        }
        
        private Map<String, String> getAssistedQueryAlgorithmProperties() {
            return assistedQueryAlgorithmProperties;
        }
        
        private String getLikeQueryAlgorithmType() {
            return likeQueryAlgorithmType;
        }
        
        private Map<String, String> getLikeQueryAlgorithmProperties() {
            return likeQueryAlgorithmProperties;
        }
    }
    
    private static final class MaskRuleRow {
        
        private final String tableName;
        
        private final String columnName;
        
        private final String algorithmType;
        
        private final Map<String, String> algorithmProperties;
        
        private MaskRuleRow(final String tableName, final String columnName, final String algorithmType, final Map<String, String> algorithmProperties) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.algorithmType = algorithmType;
            this.algorithmProperties = algorithmProperties;
        }
        
        private String getTableName() {
            return tableName;
        }
        
        private String getColumnName() {
            return columnName;
        }
        
        private String getAlgorithmType() {
            return algorithmType;
        }
        
        private Map<String, String> getAlgorithmProperties() {
            return algorithmProperties;
        }
    }
}
