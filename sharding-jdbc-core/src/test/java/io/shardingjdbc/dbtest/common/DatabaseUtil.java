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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.shardingjdbc.dbtest.config.bean.ColumnDefinition;
import io.shardingjdbc.dbtest.config.bean.DatasetDatabase;
import io.shardingjdbc.dbtest.config.bean.DatasetDefinition;
import io.shardingjdbc.dbtest.config.bean.IndexDefinition;
import io.shardingjdbc.dbtest.config.bean.ParameterDefinition;
import io.shardingjdbc.dbtest.config.bean.ParameterValueDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        for (Map.Entry<String, String> entry : config.entrySet()) {
            colsConfigs.add(entry.getKey());
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
            for (Map<String, String> entry : datas) {
                sqlParameterProcessing(config, pstmt, entry);
                pstmt.executeUpdate();
            }
        }
        return true;
    }
    
    private static void sqlParameterProcessing(final List<ColumnDefinition> config, final PreparedStatement pstmt, final Map<String, String> data) throws SQLException, ParseException {
        int index = 1;
        for (Map.Entry<String, String> each : data.entrySet()) {
            String key = each.getKey();
            String datacol = each.getValue();
            String type = "String";
            if (config != null) {
                for (ColumnDefinition eachColumnDefinition : config) {
                    if (key.equals(eachColumnDefinition.getName()) && eachColumnDefinition.getType() != null) {
                        type = eachColumnDefinition.getType();
                    }
                }
            }
            processingParameters(pstmt, index, datacol, type);
            index++;
        }
    }
    
    private static void processingParameters(final PreparedStatement pstmt, final int index, final String datacol, final String type) throws SQLException, ParseException {
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
        try (Statement pstmt = conn.createStatement()) {
            String newSql = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
            newSql = sqlStatement(newSql, parameterDefinition.getValues());
            return pstmt.executeUpdate(newSql);
        }
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
        for (ParameterValueDefinition each : parameter) {
            String type = each.getType();
            String datacol = each.getValue();
            switch (type) {
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement(datacol));
                    break;
                case "boolean":
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result)
                            .replaceFirst(Matcher.quoteReplacement(Boolean.valueOf(datacol).toString()));
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
        for (ParameterValueDefinition each : parameter) {
            String type = each.getType();
            String datacol = each.getValue();
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
        try (Statement pstmt = conn.createStatement()) {
            String newSql = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
            newSql = sqlStatement(newSql, parameterDefinition.getValues());
            if (!pstmt.execute(newSql)) {
                return pstmt.getUpdateCount();
            }
        }
        return 0;
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
        String newSql = sql.replaceAll("\\%s", "?");
        newSql = sqlReplaceStatement(newSql, parameterDefinition.getValueReplaces());
        try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {
            sqlPreparedStatement(parameterDefinition.getValues(), pstmt);
            return pstmt.executeUpdate();
        }
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
        String newSql = sql.replaceAll("\\%s", "?");
        newSql = sqlReplaceStatement(newSql, parameterDefinition.getValueReplaces());
        try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {
            sqlPreparedStatement(parameterDefinition.getValues(), pstmt);
            if (!pstmt.execute()) {
                return pstmt.getUpdateCount();
            }
        }
        return 0;
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
                return useBackResultSet(resultSet);
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
            assertTrue("Not a query statement.", flag);
            try (ResultSet resultSet = pstmt.getResultSet()) {
                return useBackResultSet(resultSet);
            }
        }
    }
    
    private static void handleResultSet(final ResultSet resultSet, final List<ColumnDefinition> cols, final List<Map<String, String>> ls) throws SQLException {
        while (resultSet.next()) {
            Map<String, String> data = new HashMap<>();
            for (ColumnDefinition each : cols) {
                String name = each.getName();
                String type = each.getType();
                switch (type) {
                    case "boolean":
                        data.put(name, String.valueOf(resultSet.getBoolean(name)));
                        break;
                    case "char":
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
                        handleResultSetMore(type, resultSet, data, name);
                        break;
                }
            }
            ls.add(data);
        }
    }
    
    private static void handleResultSetMore(final String type, final ResultSet resultSet, final Map<String, String> data, final String name) throws SQLException {
        switch (type) {
            case "int":
                data.put(name, String.valueOf(resultSet.getInt(name)));
                return;
            case "long":
                data.put(name, String.valueOf(resultSet.getLong(name)));
                break;
            case "float":
                data.put(name, String.valueOf(resultSet.getFloat(name)));
                break;
            case "double":
                data.put(name, String.valueOf(resultSet.getDouble(name)));
                break;
            default:
                data.put(name, resultSet.getString(name));
                break;
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
                return useBackResultSet(resultSet);
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
                return useBackResultSet(resultSet);
            }
        }
    }
    
    private static DatasetDatabase useBackResultSet(final ResultSet resultSet) throws SQLException {
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
        for (ParameterValueDefinition each : parameter) {
            String type = each.getType();
            String datacol = each.getValue();
            processingParameters(pstmt, index, datacol, type);
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
                result = getDataTypeMore(type);
                break;
        }
        return result;
    }
    
    private static String getDataTypeMore(final int type) {
        switch (type) {
            case Types.INTEGER:
                return "int";
            case Types.LONGVARCHAR:
                return "long";
            case Types.BIGINT:
                return "long";
            case Types.FLOAT:
                return "float";
            case Types.DOUBLE:
                return "double";
            default:
                return "String";
        }
    }
    
    /**
     * Comparative data set.
     *
     * @param expected expected
     * @param actual   actual
     * @param table    table
     * @param msg      msg
     */
    public static void assertConfigs(final DatasetDefinition expected, final List<ColumnDefinition> actual, final String table, final String msg) {
        Map<String, List<ColumnDefinition>> configs = expected.getMetadatas();
        List<ColumnDefinition> columnDefinitions = configs.get(table);
        for (ColumnDefinition each : columnDefinitions) {
            checkActual(actual, msg, each);
        }
    }
    
    private static void checkActual(final List<ColumnDefinition> actual, final String msg, final ColumnDefinition expect) {
        for (ColumnDefinition each : actual) {
            if (expect.getName().equals(each.getName())) {
                if (StringUtils.isNotEmpty(expect.getType())) {
                    assertEquals(msg, expect.getType(), each.getType());
                }
                checkDatabaseColumn(msg, expect.getDecimalDigits(), each.getDecimalDigits());
                checkDatabaseColumn(msg, expect.getNullAble(), each.getNullAble());
                checkDatabaseColumn(msg, expect.getNumPrecRadix(), each.getNumPrecRadix());
                checkDatabaseColumn(msg, expect.getSize(), each.getSize());
                if (expect.getIsAutoincrement() != 0 && expect.getIsAutoincrement() != each.getIsAutoincrement()) {
                    fail(msg);
                }
                List<IndexDefinition> indexs = expect.getIndexs();
                if (indexs != null && !indexs.isEmpty()) {
                    checkIndex(msg, each, indexs);
                }
            }
        }
    }
    
    private static void checkDatabaseColumn(final String msg, final Integer expectData, final Integer actualData) {
        if (expectData != null && !expectData.equals(actualData)) {
            fail(msg);
        }
    }
    
    private static void checkIndex(final String msg, final ColumnDefinition columnDefinition, final List<IndexDefinition> indexs) {
        for (IndexDefinition each : indexs) {
            for (IndexDefinition actualIndex : columnDefinition.getIndexs()) {
                if (each.getName().equals(actualIndex.getName())) {
                    if (each.getType() != null && !each.getType().equals(actualIndex.getType())) {
                        fail(msg);
                    }
                    if (each.isUnique() != actualIndex.isUnique()) {
                        fail(msg);
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
     * @param msg      error msg
     */
    public static void assertDatas(final DatasetDefinition expected, final DatasetDatabase actual, final String msg) {
        Map<String, List<ColumnDefinition>> actualConfigs = actual.getMetadatas();
        Map<String, List<ColumnDefinition>> expectedConfigs = expected.getMetadatas();
        for (Map.Entry<String, List<ColumnDefinition>> entry : expectedConfigs.entrySet()) {
            List<ColumnDefinition> expectedConfig = entry.getValue();
            List<ColumnDefinition> actualConfig = actualConfigs.get(entry.getKey());
            assertNotNull(msg, actualConfig);
            checkConfig(msg, expectedConfig, actualConfig);
        }
        Map<String, List<Map<String, String>>> actualDatass = actual.getDatas();
        Map<String, List<Map<String, String>>> expectDedatas = expected.getDatas();
        for (Map.Entry<String, List<Map<String, String>>> entry : expectDedatas.entrySet()) {
            List<Map<String, String>> data = entry.getValue();
            List<Map<String, String>> actualDatas = actualDatass.get(entry.getKey());
            assertEquals(msg + " result set validation failed , The number of validation data and query data is not equal", actualDatas.size(), data.size());
            checkData(msg, data, actualDatas);
        }
    }
    
    private static void checkData(final String msg, final List<Map<String, String>> data, final List<Map<String, String>> actualDatas) {
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> expectData = data.get(i);
            Map<String, String> actualData = actualDatas.get(i);
            for (Map.Entry<String, String> entry : expectData.entrySet()) {
                if (!entry.getValue().equals(actualData.get(entry.getKey()))) {
                    String actualMsg = actualDatas.toString();
                    String expectMsg = data.toString();
                    fail(msg + " result set validation failed . describe : actual = " + actualMsg + " . expect = " + expectMsg);
                }
            }
        }
    }
    
    private static void checkConfig(final String msg, final List<ColumnDefinition> expectedConfig, final List<ColumnDefinition> actualConfig) {
        for (ColumnDefinition eachColumn : expectedConfig) {
            boolean flag = false;
            for (ColumnDefinition each : actualConfig) {
                if (eachColumn.getName().equals(each.getName()) && eachColumn.getType().equals(each.getType())) {
                    flag = true;
                }
            }
            assertTrue(msg, flag);
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
                col.setName(column);
                int size = rs.getInt("COLUMN_SIZE");
                col.setSize(size);
                String columnType = rs.getString("TYPE_NAME").toLowerCase();
                col.setType(columnType);
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                col.setDecimalDigits(decimalDigits);
                int numPrecRadix = rs.getInt("NUM_PREC_RADIX");
                col.setNumPrecRadix(numPrecRadix);
                int nullAble = rs.getInt("NULLABLE");
                col.setNullAble(nullAble);
                String isAutoincrement = rs.getString("IS_AUTOINCREMENT");
                if (StringUtils.isNotEmpty(isAutoincrement)) {
                    col.setIsAutoincrement(1);
                }
                cols.add(col);
            }
            geIndexDefinitions(stmt, cols, table);
            return cols;
        }
    }
    
    private static List<ColumnDefinition> geIndexDefinitions(final DatabaseMetaData stmt, final List<ColumnDefinition> cols, final String table) throws SQLException, ParseException {
        try (ResultSet rs = stmt.getIndexInfo(null, null, table, false, false)) {
            while (rs.next()) {
                IndexDefinition index = new IndexDefinition();
                String name = rs.getString("COLUMN_NAME");
                String nameIndex = rs.getString("INDEX_NAME");
                if (StringUtils.isNotEmpty(nameIndex)) {
                    index.setName(nameIndex);
                }
                String typeIndex = rs.getString("TYPE");
                if (StringUtils.isNotEmpty(typeIndex)) {
                    index.setType(typeIndex);
                }
                String uniqueIndex = rs.getString("NON_UNIQUE");
                if (StringUtils.isNotEmpty(uniqueIndex)) {
                    index.setUnique(!"TRUE".equalsIgnoreCase(uniqueIndex));
                }
                for (ColumnDefinition col : cols) {
                    if (name.equals(col.getName())) {
                        col.getIndexs().add(index);
                        break;
                    }
                }
            }
            return cols;
        }
    }
}
