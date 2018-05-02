/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.dbtest.common;

import static org.junit.Assert.assertTrue;

import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.shardingjdbc.dbtest.config.bean.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import io.shardingjdbc.dbtest.exception.DbTestException;
import org.junit.Assert;

public class DatabaseUtil {
    
    /**
     * Generating sql.
     *
     * @param table  table
     * @param config Map column,data
     * @return sql
     */
    public static String analyzeSql(final String table, final Map<String, String> config) {
        
        List<String> colsConfigs = new ArrayList<>();
        List<String> valueConfigs = new ArrayList<>();
        for (Map.Entry<String, String> stringStringEntry : config.entrySet()) {
            colsConfigs.add(stringStringEntry.getKey());
            valueConfigs.add("?");
        }
        
        StringBuilder sbsql = new StringBuilder("insert into ");
        sbsql.append(table);
        sbsql.append(" ( ");
        sbsql.append(StringUtils.join(colsConfigs, ","));
        sbsql.append(" )");
        sbsql.append(" values ");
        sbsql.append(" ( ");
        sbsql.append(StringUtils.join(valueConfigs, ","));
        sbsql.append(" )");
        
        return sbsql.toString();
    }
    
    /**
     * Insert initialization data.
     *
     * @param conn   Jdbc connection
     * @param sql    sql
     * @param datas  init data
     * @param config Table field type
     * @return Success or failure
     * @throws SQLException   SQL executes exceptions
     * @throws ParseException Precompiled anomaly
     */
    public static boolean insertUsePreparedStatement(final Connection conn, final String sql,
                                                     final List<Map<String, String>> datas, final List<ColumnDefinition> config)
            throws SQLException, ParseException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
            for (Map<String, String> data : datas) {
                int index = 1;
                for (Map.Entry<String, String> each : data.entrySet()) {
                    String key = each.getKey();
                    String datacol = each.getValue();
                    String type = "String";
                    if (config != null) {
                        for (ColumnDefinition eachColumnDefinition : config) {
                            if (key.equals(eachColumnDefinition.getName())) {
                                type = eachColumnDefinition.getType();
                            }
                        }
                    }
                    if (type == null) {
                        type = "String";
                    }
                    switch (type) {
                        case "byte":
                            pstmt.setByte(index, Byte.valueOf(datacol));
                            break;
                        case "short":
                            pstmt.setShort(index, Short.valueOf(datacol));
                            break;
                        case "int":
                            pstmt.setInt(index, Integer.valueOf(datacol));
                            break;
                        case "long":
                            pstmt.setLong(index, Long.valueOf(datacol));
                            break;
                        case "float":
                            pstmt.setFloat(index, Float.valueOf(datacol));
                            break;
                        case "double":
                            pstmt.setDouble(index, Double.valueOf(datacol));
                            break;
                        case "boolean":
                            pstmt.setBoolean(index, Boolean.valueOf(datacol));
                            break;
                        case "Date":
                            FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
                            pstmt.setDate(index, new Date(fdf.parse(datacol).getTime()));
                            break;
                        case "String":
                            pstmt.setString(index, datacol);
                            break;
                        default:
                            pstmt.setString(index, datacol);
                            break;
                    }
                    index++;
                }
                pstmt.executeUpdate();
            }
        }
        return true;
    }
    
    /**
     * clear table.
     *
     * @param conn  Jdbc connection
     * @param table table
     * @throws SQLException SQL executes exceptions
     */
    public static void cleanAllUsePreparedStatement(final Connection conn, final String table) throws SQLException {
        try (Statement pstmt = conn.createStatement();) {
            pstmt.execute("DELETE from " + table);
        }
    }
    
    /**
     * To determine if it is a query statement.
     *
     * @param sql sql
     * @return true select
     */
    public static boolean isSelect(final String sql) {
        String newSql = sql.trim();
        return newSql.startsWith("select");
    }
    
    /**
     * To determine whether the statement is an update.
     *
     * @param sql sql
     * @return true update
     */
    public static boolean isInsertOrUpdateOrDelete(final String sql) {
        String newSql = sql.trim();
        return newSql.startsWith("insert") || newSql.startsWith("update") || newSql.startsWith("delete");
    }
    
    /**
     * Use Statement Test data update.
     *
     * @param conn                Jdbc connection
     * @param sql                 sql
     * @param parameterDefinition parameter
     * @return Number of rows as a result of execution
     * @throws SQLException SQL executes exceptions
     */
    public static int updateUseStatementToExecuteUpdate(final Connection conn, final String sql,
                                                        final ParameterDefinition parameterDefinition) throws SQLException {
        int result = 0;
        try (Statement pstmt = conn.createStatement()) {
            String newSql = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
            newSql = sqlStatement(newSql, parameterDefinition.getValues());
            result = result + pstmt.executeUpdate(newSql);
        }
        return result;
    }
    
    /**
     * Processing statement sql.
     *
     * @param sql       sql
     * @param parameter parameter
     * @return sql
     */
    private static String sqlStatement(final String sql, final List<ParameterValueDefinition> parameter) {
        if (parameter == null) {
            return sql;
        }
        String result = sql;
        for (ParameterValueDefinition parameterDefinition : parameter) {
            
            String type = parameterDefinition.getType();
            String datacol = parameterDefinition.getValue();
            switch (type) {
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement(datacol.toString()));
                    break;
                case "boolean":
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement(Boolean.valueOf(datacol).toString()));
                    break;
                case "Date":
                    throw new DbTestException("Date type not supported for the time being");
                case "String":
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement("'" + datacol + "'"));
                    break;
                default:
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement("'" + datacol + "'"));
                    break;
            }
        }
        return result;
    }
    
    /**
     * Processing statement sql.
     *
     * @param sql       sql
     * @param parameter parameter
     * @return sql
     */
    private static String sqlReplaceStatement(final String sql, final List<ParameterValueDefinition> parameter) {
        if (parameter == null) {
            return sql;
        }
        String result = sql;
        for (ParameterValueDefinition parameterDefinition : parameter) {
            
            String type = parameterDefinition.getType();
            String datacol = parameterDefinition.getValue();
            switch (type) {
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                    result = Pattern.compile("#s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement(datacol.toString()));
                    break;
                case "boolean":
                    result = Pattern.compile("#s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement(Boolean.valueOf(datacol).toString()));
                    break;
                case "Date":
                    throw new DbTestException("Date type not supported for the time being");
                case "String":
                    result = Pattern.compile("#s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement("'" + datacol + "'"));
                    break;
                default:
                    result = Pattern.compile("#s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement("'" + datacol + "'"));
                    break;
            }
        }
        return result;
    }
    
    /**
     * Use Statement Test data update.
     *
     * @param conn                Jdbc connection
     * @param sql                 sql
     * @param parameterDefinition parameter
     * @return Implementation results
     * @throws SQLException SQL executes exceptions
     */
    public static int updateUseStatementToExecute(final Connection conn, final String sql,
                                                  final ParameterDefinition parameterDefinition) throws SQLException {
        int result = 0;
        try (Statement pstmt = conn.createStatement()) {
            String newSql = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
            newSql = sqlStatement(newSql, parameterDefinition.getValues());
            if (!pstmt.execute(newSql)) {
                result = result + pstmt.getUpdateCount();
            }
            
        }
        return result;
    }
    
    /**
     * Use PreparedStatement Test data update.
     *
     * @param conn                Jdbc connection
     * @param sql                 sql
     * @param parameterDefinition parameter
     * @return Number of rows as a result of execution
     * @throws SQLException   SQL executes exceptions
     * @throws ParseException ParseException
     */
    public static int updateUsePreparedStatementToExecuteUpdate(final Connection conn, final String sql,
                                                                final ParameterDefinition parameterDefinition) throws SQLException, ParseException {
        int result = 0;
        String newSql = sql.replaceAll("\\%s", "?");
        newSql = sqlReplaceStatement(newSql, parameterDefinition.getValueReplaces());
        try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {
            sqlPreparedStatement(parameterDefinition.getValues(), pstmt);
            result = result + pstmt.executeUpdate();
        }
        return result;
    }
    
    /**
     * Use PreparedStatement Test data update.
     *
     * @param conn                Jdbc connection
     * @param sql                 sql
     * @param parameterDefinition parameter
     * @return Implementation results
     * @throws SQLException   SQL executes exceptions
     * @throws ParseException ParseException
     */
    public static int updateUsePreparedStatementToExecute(final Connection conn, final String sql,
                                                          final ParameterDefinition parameterDefinition) throws SQLException, ParseException {
        int result = 0;
        String newSql = sql.replaceAll("\\%s", "?");
        newSql = sqlReplaceStatement(newSql, parameterDefinition.getValueReplaces());
        try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {
            sqlPreparedStatement(parameterDefinition.getValues(), pstmt);
            if (!pstmt.execute()) {
                result = result + pstmt.getUpdateCount();
            }
        }
        
        return result;
    }
    
    /**
     * Use PreparedStatement Test sql select.
     *
     * @param conn       Jdbc connection
     * @param sql        sql
     * @param parameters parameters
     * @return Query result set
     * @throws SQLException   SQL executes exceptions
     * @throws ParseException ParseException
     */
    public static DatasetDatabase selectUsePreparedStatement(final Connection conn, final String sql,
                                                             final ParameterDefinition parameters) throws SQLException, ParseException {
        List<ParameterValueDefinition> parameter = parameters.getValues();
        String newSql = sqlReplaceStatement(sql, parameters.getValueReplaces());
        newSql = newSql.replaceAll("\\%s", "?");
        try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {
            sqlPreparedStatement(parameter, pstmt);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return usePreparedStatementBackResultSet(resultSet);
            }
        }
    }
    
    /**
     * Use PreparedStatement Test sql select.
     *
     * @param conn       Jdbc connection
     * @param sql        sql
     * @param parameters parameters
     * @return Query result set
     * @throws SQLException   SQL executes exceptions
     * @throws ParseException ParseException
     */
    public static DatasetDatabase selectUsePreparedStatementToExecuteSelect(final Connection conn, final String sql,
                                                                            final ParameterDefinition parameters) throws SQLException, ParseException {
        List<ParameterValueDefinition> parameter = parameters.getValues();
        String newSql = sqlReplaceStatement(sql, parameters.getValueReplaces());
        newSql = newSql.replaceAll("\\%s", "?");
        try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {
            sqlPreparedStatement(parameter, pstmt);
            boolean flag = pstmt.execute();
            Assert.assertTrue("Not a query statement.", flag);
            try (ResultSet resultSet = pstmt.getResultSet()) {
                return usePreparedStatementBackResultSet(resultSet);
            }
        }
    }
    
    private static DatasetDatabase usePreparedStatementBackResultSet(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int colsint = rsmd.getColumnCount();
        List<ColumnDefinition> cols = new ArrayList<>();
        for (int i = 1; i < colsint + 1; i++) {
            String name = rsmd.getColumnName(i);
            String type = getDataType(rsmd.getColumnType(i), rsmd.getScale(i));
            ColumnDefinition columnDefinition = new ColumnDefinition();
            columnDefinition.setName(name);
            columnDefinition.setType(type);
            cols.add(columnDefinition);
        }
        
        Map<String, List<ColumnDefinition>> configs = new HashMap<>();
        configs.put("data", cols);
        
        List<Map<String, String>> ls = new ArrayList<>();
        Map<String, List<Map<String, String>>> datas = new HashMap<>();
        datas.put("data", ls);
        
        handleResultSet(resultSet, cols, ls);
        DatasetDatabase result = new DatasetDatabase();
        result.setMetadatas(configs);
        result.setDatas(datas);
        return result;
    }
    
    private static void handleResultSet(final ResultSet resultSet, final List<ColumnDefinition> cols, final List<Map<String, String>> ls) throws SQLException {
        while (resultSet.next()) {
            Map<String, String> data = new HashMap<>();
            for (ColumnDefinition each : cols) {
                String name = each.getName();
                String type = each.getType();
                switch (type) {
                    case "int":
                        data.put(name, String.valueOf(resultSet.getInt(name)));
                        break;
                    case "long":
                        data.put(name, String.valueOf(resultSet.getLong(name)));
                        break;
                    case "float":
                        data.put(name, String.valueOf(resultSet.getFloat(name)));
                        break;
                    case "double":
                        data.put(name, String.valueOf(resultSet.getDouble(name)));
                        break;
                    case "boolean":
                        data.put(name, String.valueOf(resultSet.getBoolean(name)));
                        break;
                    case "char":
                        data.put(name, String.valueOf(resultSet.getString(name)));
                        break;
                    case "String":
                        data.put(name, String.valueOf(resultSet.getString(name)));
                        break;
                    case "Date":
                        data.put(name, DateFormatUtils.format(new java.util.Date(resultSet.getDate(name).getTime()),
                                "yyyy-MM-dd"));
                        break;
                    case "Blob":
                        data.put(name, String.valueOf(resultSet.getBlob(name)));
                        break;
                    default:
                        data.put(name, resultSet.getString(name));
                        break;
                }
            }
            ls.add(data);
        }
    }
    
    /**
     * Use Statement Test sql select.
     *
     * @param conn       Jdbc connection
     * @param sql        sql
     * @param parameters parameters
     * @return Query result set
     * @throws SQLException SQL executes exceptions
     */
    public static DatasetDatabase selectUseStatement(final Connection conn, final String sql,
                                                     final ParameterDefinition parameters) throws SQLException {
        List<ParameterValueDefinition> parameter = parameters.getValues();
        String newSql = sqlReplaceStatement(sql, parameters.getValueReplaces());
        try (Statement pstmt = conn.createStatement()) {
            newSql = sqlStatement(newSql, parameter);
            try (ResultSet resultSet = pstmt.executeQuery(newSql)) {
                return useStatementBackResultSet(resultSet);
            }
        }
    }
    
    /**
     * Use Statement Test sql select.
     *
     * @param conn       Jdbc connection
     * @param sql        sql
     * @param parameters parameters
     * @return Query result set
     * @throws SQLException SQL executes exceptions
     */
    public static DatasetDatabase selectUseStatementToExecuteSelect(final Connection conn, final String sql,
                                                                    final ParameterDefinition parameters) throws SQLException {
        List<ParameterValueDefinition> parameter = parameters.getValues();
        String newSql = sqlReplaceStatement(sql, parameters.getValueReplaces());
        try (Statement pstmt = conn.createStatement()) {
            newSql = sqlStatement(newSql, parameter);
            try (ResultSet resultSet = pstmt.executeQuery(newSql)) {
                return useStatementBackResultSet(resultSet);
            }
        }
    }
    
    private static DatasetDatabase useStatementBackResultSet(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData rsmd = resultSet.getMetaData();
        int colsint = rsmd.getColumnCount();
        List<ColumnDefinition> cols = new ArrayList<>();
        for (int i = 1; i < colsint + 1; i++) {
            String name = rsmd.getColumnName(i);
            String type = getDataType(rsmd.getColumnType(i), rsmd.getScale(i));
            ColumnDefinition columnDefinition = new ColumnDefinition();
            columnDefinition.setName(name);
            columnDefinition.setType(type);
            cols.add(columnDefinition);
        }
        
        Map<String, List<ColumnDefinition>> configs = new HashMap<>();
        configs.put("data", cols);
        
        List<Map<String, String>> ls = new ArrayList<>();
        Map<String, List<Map<String, String>>> datas = new HashMap<>();
        datas.put("data", ls);
        
        handleResultSet(resultSet, cols, ls);
        DatasetDatabase result = new DatasetDatabase();
        result.setMetadatas(configs);
        result.setDatas(datas);
        return result;
    }
    
    
    /**
     * Sql parameter injection.
     *
     * @param parameter parameter
     * @param pstmt     PreparedStatement
     * @throws SQLException   SQL executes exceptions
     * @throws ParseException ParseException
     */
    private static void sqlPreparedStatement(final List<ParameterValueDefinition> parameter, final PreparedStatement pstmt)
            throws SQLException, ParseException {
        if (parameter == null) {
            return;
        }
        int index = 1;
        for (ParameterValueDefinition parameterDefinition : parameter) {
            String type = parameterDefinition.getType();
            String datacol = parameterDefinition.getValue();
            switch (type) {
                case "byte":
                    pstmt.setByte(index, Byte.valueOf(datacol));
                    break;
                case "short":
                    pstmt.setShort(index, Short.valueOf(datacol));
                    break;
                case "int":
                    pstmt.setInt(index, Integer.valueOf(datacol));
                    break;
                case "long":
                    pstmt.setLong(index, Long.valueOf(datacol));
                    break;
                case "float":
                    pstmt.setFloat(index, Float.valueOf(datacol));
                    break;
                case "double":
                    pstmt.setDouble(index, Double.valueOf(datacol));
                    break;
                case "boolean":
                    pstmt.setBoolean(index, Boolean.valueOf(datacol));
                    break;
                case "Date":
                    FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
                    pstmt.setDate(index, new Date(fdf.parse(datacol).getTime()));
                    break;
                case "String":
                    pstmt.setString(index, datacol);
                    break;
                default:
                    pstmt.setString(index, datacol);
                    break;
            }
            index++;
        }
    }
    
    /**
     * Database type to java type.
     *
     * @param type  database type
     * @param scale scale
     * @return java type
     */
    private static String getDataType(final int type, final int scale) {
        String result = null;
        switch (type) {
            case Types.INTEGER:
                result = "int";
                break;
            case Types.LONGVARCHAR:
                result = "long";
                break;
            case Types.BIGINT:
                result = "long";
                break;
            case Types.FLOAT:
                result = "float";
                break;
            case Types.DOUBLE:
                result = "double";
                break;
            case Types.BOOLEAN:
                result = "boolean";
                break;
            case Types.CHAR:
                result = "char";
                break;
            case Types.NUMERIC:
                switch (scale) {
                    case 0:
                        result = "double";
                        break;
                    case -127:
                        result = "float";
                        break;
                    default:
                        result = "double";
                }
                break;
            case Types.VARCHAR:
                result = "String";
                break;
            case Types.DATE:
                result = "Date";
                break;
            case Types.TIMESTAMP:
                result = "Date";
                break;
            case Types.BLOB:
                result = "Blob";
                break;
            default:
                result = "String";
        }
        return result;
    }
    
    /**
     * Comparative data set.
     *
     * @param expected expected
     * @param actual   actual
     * @param table    table
     * @param msg      msg
     */
    public static void assertConfigs(final DatasetDefinition expected, final List<ColumnDefinition> actual,final String table,final String msg) {
        Map<String, List<ColumnDefinition>> configs = expected.getMetadatas();
        List<ColumnDefinition> columnDefinitions = configs.get(table);
        for (ColumnDefinition each : columnDefinitions) {
            
            for (ColumnDefinition definition : actual) {
                if (each.getName().equals(definition.getName())) {
                    boolean flag2 = true;
                    if (StringUtils.isNotEmpty(each.getType())) {
                        if (!each.getType().equals(definition.getType())) {
                            flag2 = false;
                        }
                    }
                    if (flag2 && each.getDecimalDigits() != null) {
                        if (!each.getDecimalDigits().equals(definition.getDecimalDigits())) {
                            flag2 = false;
                        }
                    }
                    if (flag2 && each.getNullAble() != null) {
                        if (!each.getNullAble().equals(definition.getNullAble())) {
                            flag2 = false;
                        }
                    }
                    if (flag2 && each.getNumPrecRadix() != null) {
                        if (!each.getNumPrecRadix().equals(definition.getNumPrecRadix())) {
                            flag2 = false;
                        }
                    }
                    if (flag2 && each.getSize() != null) {
                        if (!each.getSize().equals(definition.getSize())) {
                            flag2 = false;
                        }
                    }
                    if (flag2 && each.getIsAutoincrement() != 0) {
                        if (each.getIsAutoincrement() != definition.getIsAutoincrement()) {
                            flag2 = false;
                        }
                    }
                    if (!flag2) {
                        assertTrue(msg, false);
                    }
                }
            }
            
        }
    }
    
    /**
     * Comparative data set.
     *
     * @param expected expected
     * @param actual   actual
     */
    public static void assertDatas(final DatasetDefinition expected, final DatasetDatabase actual, String msg) {
        Map<String, List<ColumnDefinition>> actualConfigs = actual.getMetadatas();
        Map<String, List<ColumnDefinition>> expectedConfigs = expected.getMetadatas();
        
        for (Map.Entry<String, List<ColumnDefinition>> each : expectedConfigs.entrySet()) {
            List<ColumnDefinition> config = each.getValue();
            List<ColumnDefinition> actualConfig = actualConfigs.get(each.getKey());
            assertTrue(actualConfig != null);
            for (ColumnDefinition eachColumn : config) {
                boolean flag = false;
                for (ColumnDefinition eachActualColumn : actualConfig) {
                    if (eachColumn.getName().equals(eachActualColumn.getName()) && eachColumn.getType().equals(eachActualColumn.getType())) {
                        flag = true;
                    }
                }
                assertTrue(msg, flag);
            }
        }
        
        Map<String, List<Map<String, String>>> actualDatass = actual.getDatas();
        Map<String, List<Map<String, String>>> expectDedatas = expected.getDatas();
        for (Map.Entry<String, List<Map<String, String>>> stringListEntry : expectDedatas.entrySet()) {
            List<Map<String, String>> data = stringListEntry.getValue();
            List<Map<String, String>> actualDatas = actualDatass.get(stringListEntry.getKey());
            
            assertTrue(msg + " result set validation failed , The number of validation data and query data is not equal", data.size() == actualDatas.size());
            
            for (int i = 0; i < data.size(); i++) {
                Map<String, String> expectData = data.get(i);
                Map<String, String> actualData = actualDatas.get(i);
                for (Map.Entry<String, String> stringStringEntry : expectData.entrySet()) {
                    if (!stringStringEntry.getValue().equals(actualData.get(stringStringEntry.getKey()))) {
                        String actualMsg = actualDatas.toString();
                        String expectMsg = data.toString();
                        assertTrue(msg + " result set validation failed . describe : actual = " + actualMsg + " . expect = " + expectMsg, stringStringEntry.getValue().equals(actualData.get(stringStringEntry.getKey())));
                    }
                }
                
            }
        }
    }
    
    /**
     * Use PreparedStatement Test sql select.
     *
     * @param conn  Jdbc connection
     * @param table table
     * @return Query result set
     * @throws SQLException   SQL executes exceptions
     * @throws ParseException ParseException
     */
    public static List<ColumnDefinition> getColumnDefinitions(final Connection conn, final String table) throws SQLException, ParseException {
        DatabaseMetaData stmt = conn.getMetaData();
        try (ResultSet rs = stmt.getColumns(null, null, table, null)) {
            List<ColumnDefinition> cols = new ArrayList<ColumnDefinition>();
            while (rs.next()) {
                ColumnDefinition col = new ColumnDefinition();
                String column = rs.getString("COLUMN_NAME");
                int size = rs.getInt("COLUMN_SIZE");
                String columnType = rs.getString("TYPE_NAME").toLowerCase();
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                int numPrecRadix = rs.getInt("NUM_PREC_RADIX");
                int nullAble = rs.getInt("NULLABLE");
                String isAutoincrement = rs.getString("IS_AUTOINCREMENT");
                
                col.setSize(size);
                col.setDecimalDigits(decimalDigits);
                col.setNumPrecRadix(numPrecRadix);
                col.setNullAble(nullAble);
                if (StringUtils.isNotEmpty(isAutoincrement)) {
                    col.setIsAutoincrement(1);
                }
                col.setName(column);
                col.setType(columnType);
                cols.add(col);
            }
            return cols;
        }
    }
}
