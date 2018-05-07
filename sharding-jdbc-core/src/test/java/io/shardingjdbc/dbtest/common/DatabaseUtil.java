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

import io.shardingjdbc.dbtest.config.bean.ColumnDefinition;
import io.shardingjdbc.dbtest.config.bean.DatasetDatabase;
import io.shardingjdbc.dbtest.config.bean.DatasetDefinition;
import io.shardingjdbc.dbtest.config.bean.IndexDefinition;
import io.shardingjdbc.dbtest.config.bean.ParameterDefinition;
import io.shardingjdbc.dbtest.config.bean.ParameterValueDefinition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Database utility.
 * 
 * <p>
 * Database related operations a series of methods,
 * including pre-compiled sql processing,
 * non-precompiled sql processing and include result set transformation,
 * get table structure, etc.
 * </p>
 * 
 * @author liu ze jian
 */
public class DatabaseUtil {
    
    /**
     * Generating sql.
     *
     * @param table  table
     * @param config Map column, data
     * @return sql
     */
    public static String analyzeSQL(final String table, final Map<String, String> config) {
        List<String> colsConfigs = new ArrayList<>();
        List<String> valueConfigs = new ArrayList<>();
        for (Map.Entry<String, String> entry : config.entrySet()) {
            colsConfigs.add(entry.getKey());
            valueConfigs.add("?");
        }
        StringBuilder result = new StringBuilder("insert into ");
        result.append(table);
        result.append(" ( ");
        result.append(StringUtils.join(colsConfigs, ","));
        result.append(" )");
        result.append(" values ");
        result.append(" ( ");
        result.append(StringUtils.join(valueConfigs, ","));
        result.append(" )");
        return result.toString();
    }
    
    /**
     * Insert initialization data.
     *
     * @param connection connection
     * @param sql SQL
     * @param datas init data
     * @param config table field type
     * @return Success or failure
     * @throws SQLException   SQL exception
     * @throws ParseException Precompiled anomaly
     */
    public static boolean insertUsePreparedStatement(final Connection connection, final String sql, final List<Map<String, String>> datas, final List<ColumnDefinition> config)
            throws SQLException, ParseException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            for (Map<String, String> entry : datas) {
                sqlParameterProcessing(config, preparedStatement, entry);
                preparedStatement.executeUpdate();
            }
        }
        return true;
    }
    
    private static void sqlParameterProcessing(final List<ColumnDefinition> config, final PreparedStatement preparedStatement, final Map<String, String> data) throws SQLException, ParseException {
        int index = 1;
        for (Map.Entry<String, String> each : data.entrySet()) {
            String key = each.getKey();
            String dataColumn = each.getValue();
            String type = "String";
            if (config != null) {
                for (ColumnDefinition eachColumnDefinition : config) {
                    if (key.equals(eachColumnDefinition.getName()) && eachColumnDefinition.getType() != null) {
                        type = eachColumnDefinition.getType();
                    }
                }
            }
            processingParameters(preparedStatement, index, dataColumn, type);
            index++;
        }
    }
    
    private static void processingParameters(final PreparedStatement preparedStatement, final int index, final String dataColumn, final String type) throws SQLException, ParseException {
        switch (type) {
            case "byte":
                preparedStatement.setByte(index, Byte.valueOf(dataColumn));
                break;
            case "short":
                preparedStatement.setShort(index, Short.valueOf(dataColumn));
                break;
            case "int":
                preparedStatement.setInt(index, Integer.valueOf(dataColumn));
                break;
            case "long":
                preparedStatement.setLong(index, Long.valueOf(dataColumn));
                break;
            case "float":
                preparedStatement.setFloat(index, Float.valueOf(dataColumn));
                break;
            case "double":
                preparedStatement.setDouble(index, Double.valueOf(dataColumn));
                break;
            case "boolean":
                preparedStatement.setBoolean(index, Boolean.valueOf(dataColumn));
                break;
            case "Date":
                FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd");
                preparedStatement.setDate(index, new Date(fdf.parse(dataColumn).getTime()));
                break;
            case "String":
                preparedStatement.setString(index, dataColumn);
                break;
            default:
                preparedStatement.setString(index, dataColumn);
                break;
        }
    }
    
    /**
     * clear table.
     *
     * @param connection  Jdbc connection
     * @param table table
     * @throws SQLException SQL executes exceptions
     */
    public static void cleanAllUsePreparedStatement(final Connection connection, final String table) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM " + table);
        }
    }
    
    /**
     * Use Statement Test data update.
     *
     * @param connection connection
     * @param sql SQL
     * @param parameterDefinition parameter
     * @return Number of rows as a result of execution
     * @throws SQLException SQL exception
     */
    public static int updateUseStatementToExecuteUpdate(final Connection connection, final String sql, final ParameterDefinition parameterDefinition) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String newSql = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
            newSql = sqlStatement(newSql, parameterDefinition.getValues());
            return statement.executeUpdate(newSql);
        }
    }
    
    private static String sqlStatement(final String sql, final List<ParameterValueDefinition> parameter) {
        if (null == parameter) {
            return sql;
        }
        String result = sql;
        for (ParameterValueDefinition each : parameter) {
            String type = each.getType();
            String dataColumn = each.getValue();
            switch (type) {
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result).replaceFirst(Matcher.quoteReplacement(dataColumn));
                    break;
                case "boolean":
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result).replaceFirst(Matcher.quoteReplacement(Boolean.valueOf(dataColumn).toString()));
                    break;
                default:
                    result = Pattern.compile("%s", Pattern.LITERAL).matcher(result).replaceFirst(Matcher.quoteReplacement("'" + dataColumn + "'"));
                    break;
            }
        }
        return result;
    }
    
    private static String sqlReplaceStatement(final String sql, final List<ParameterValueDefinition> parameter) {
        if (null == parameter) {
            return sql;
        }
        String result = sql;
        for (ParameterValueDefinition each : parameter) {
            String type = each.getType();
            String dataColumn = each.getValue();
            switch (type) {
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                    result = Pattern.compile("#s", Pattern.LITERAL).matcher(result).replaceFirst(Matcher.quoteReplacement(dataColumn.toString()));
                    break;
                case "boolean":
                    result = Pattern.compile("#s", Pattern.LITERAL).matcher(result).replaceFirst(Matcher.quoteReplacement(Boolean.valueOf(dataColumn).toString()));
                    break;
                default:
                    result = Pattern.compile("#s", Pattern.LITERAL).matcher(result).replaceFirst(Matcher.quoteReplacement("'" + dataColumn + "'"));
                    break;
            }
        }
        return result;
    }
    
    /**
     * Use Statement Test data update.
     *
     * @param connection connection
     * @param sql SQL
     * @param parameterDefinition parameter definition
     * @return implementation results
     * @throws SQLException SQL exception
     */
    public static int updateUseStatementToExecute(final Connection connection, final String sql, final ParameterDefinition parameterDefinition) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String newSQL = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
            newSQL = sqlStatement(newSQL, parameterDefinition.getValues());
            if (!statement.execute(newSQL)) {
                return statement.getUpdateCount();
            }
        }
        return 0;
    }
    
    /**
     * Use PreparedStatement test data update.
     *
     * @param connection connection
     * @param sql SQL
     * @param parameterDefinition parameter
     * @return Number of rows as a result of execution
     * @throws SQLException SQL exception
     * @throws ParseException parse exception
     */
    public static int updateUsePreparedStatementToExecuteUpdate(final Connection connection, final String sql, final ParameterDefinition parameterDefinition) throws SQLException, ParseException {
        String newSQL = sql.replaceAll("\\%s", "?");
        newSQL = sqlReplaceStatement(newSQL, parameterDefinition.getValueReplaces());
        try (PreparedStatement preparedStatement = connection.prepareStatement(newSQL)) {
            sqlPreparedStatement(parameterDefinition.getValues(), preparedStatement);
            return preparedStatement.executeUpdate();
        }
    }
    
    /**
     * Use PreparedStatement test data update.
     *
     * @param connection connection
     * @param sql SQL
     * @param parameterDefinition parameter definition
     * @return implementation results
     * @throws SQLException   SQL exception
     * @throws ParseException parse exception
     */
    public static int updateUsePreparedStatementToExecute(final Connection connection, final String sql, final ParameterDefinition parameterDefinition) throws SQLException, ParseException {
        String newSQL = sql.replaceAll("\\%s", "?");
        newSQL = sqlReplaceStatement(newSQL, parameterDefinition.getValueReplaces());
        try (PreparedStatement preparedStatement = connection.prepareStatement(newSQL)) {
            sqlPreparedStatement(parameterDefinition.getValues(), preparedStatement);
            if (!preparedStatement.execute()) {
                return preparedStatement.getUpdateCount();
            }
        }
        return 0;
    }
    
    /**
     * Use PreparedStatement test SQL select.
     *
     * @param conn connection
     * @param sql SQL
     * @param parameterDefinition parameter definition 
     * @return query result set
     * @throws SQLException   SQL exception
     * @throws ParseException parse exception
     */
    public static DatasetDatabase selectUsePreparedStatement(final Connection conn, final String sql, final ParameterDefinition parameterDefinition) throws SQLException, ParseException {
        List<ParameterValueDefinition> parameters = parameterDefinition.getValues();
        String newSQL = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
        newSQL = newSQL.replaceAll("\\%s", "?");
        try (PreparedStatement preparedStatement = conn.prepareStatement(newSQL)) {
            sqlPreparedStatement(parameters, preparedStatement);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return useBackResultSet(resultSet);
            }
        }
    }
    
    /**
     * Use PreparedStatement test SQL select.
     *
     * @param connection connection
     * @param sql SQL
     * @param parameterDefinition parameter definition
     * @return query result set
     * @throws SQLException   SQL exception
     * @throws ParseException parse exception
     */
    public static DatasetDatabase selectUsePreparedStatementToExecuteSelect(final Connection connection, final String sql, final ParameterDefinition parameterDefinition) throws SQLException, ParseException {
        List<ParameterValueDefinition> parameter = parameterDefinition.getValues();
        String newSQL = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
        newSQL = newSQL.replaceAll("\\%s", "?");
        try (PreparedStatement preparedStatement = connection.prepareStatement(newSQL)) {
            sqlPreparedStatement(parameter, preparedStatement);
            boolean flag = preparedStatement.execute();
            assertTrue("Not a query statement.", flag);
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                return useBackResultSet(resultSet);
            }
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
     * Use Statement test SQL select.
     *
     * @param connection connection
     * @param sql SQL
     * @param parameterDefinition parameter definition
     * @return query result set
     * @throws SQLException SQL exception
     */
    public static DatasetDatabase selectUseStatement(final Connection connection, final String sql, final ParameterDefinition parameterDefinition) throws SQLException {
        List<ParameterValueDefinition> parameter = parameterDefinition.getValues();
        String newSQL = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
        try (Statement statement = connection.createStatement()) {
            newSQL = sqlStatement(newSQL, parameter);
            try (ResultSet resultSet = statement.executeQuery(newSQL)) {
                return useBackResultSet(resultSet);
            }
        }
    }
    
    /**
     * Use Statement test SQL select.
     *
     * @param connection connection
     * @param sql SQL
     * @param parameterDefinition parameter definition
     * @return query result set
     * @throws SQLException SQL exception
     */
    public static DatasetDatabase selectUseStatementToExecuteSelect(final Connection connection, final String sql, final ParameterDefinition parameterDefinition) throws SQLException {
        List<ParameterValueDefinition> parameter = parameterDefinition.getValues();
        String newSQL = sqlReplaceStatement(sql, parameterDefinition.getValueReplaces());
        try (Statement statement = connection.createStatement()) {
            newSQL = sqlStatement(newSQL, parameter);
            try (ResultSet resultSet = statement.executeQuery(newSQL)) {
                return useBackResultSet(resultSet);
            }
        }
    }
    
    private static DatasetDatabase useBackResultSet(final ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<ColumnDefinition> cols = new ArrayList<>();
        for (int i = 1; i < columnCount + 1; i++) {
            String name = metaData.getColumnName(i);
            String type = getDataType(metaData.getColumnType(i), metaData.getScale(i));
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
    
    private static void handleResultSet(final ResultSet resultSet, final List<ColumnDefinition> columnDefinitions, final List<Map<String, String>> ls) throws SQLException {
        while (resultSet.next()) {
            Map<String, String> data = new HashMap<>();
            for (ColumnDefinition each : columnDefinitions) {
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
    
    private static void sqlPreparedStatement(final List<ParameterValueDefinition> parameterValueDefinitions, final PreparedStatement preparedStatement) throws SQLException, ParseException {
        if (null == parameterValueDefinitions) {
            return;
        }
        int index = 1;
        for (ParameterValueDefinition each : parameterValueDefinitions) {
            processingParameters(preparedStatement, index, each.getValue(), each.getType());
            index++;
        }
    }
    
    private static String getDataType(final int type, final int scale) {
        String result;
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
     * @param actual actual
     * @param table table
     */
    public static void assertConfigs(final DatasetDefinition expected, final List<ColumnDefinition> actual, final String table) {
        Map<String, List<ColumnDefinition>> configs = expected.getMetadatas();
        List<ColumnDefinition> columnDefinitions = configs.get(table);
        for (ColumnDefinition each : columnDefinitions) {
            checkActual(actual, each);
        }
    }
    
    private static void checkActual(final List<ColumnDefinition> actual, final ColumnDefinition expect) {
        for (ColumnDefinition each : actual) {
            if (expect.getName().equals(each.getName())) {
                if (StringUtils.isNotEmpty(expect.getType())) {
                    assertEquals(expect.getType(), each.getType());
                }
                checkDatabaseColumn(expect.getDecimalDigits(), each.getDecimalDigits());
                checkDatabaseColumn(expect.getNullAble(), each.getNullAble());
                checkDatabaseColumn(expect.getNumPrecRadix(), each.getNumPrecRadix());
                checkDatabaseColumn(expect.getSize(), each.getSize());
                if (expect.getIsAutoincrement() != 0 && expect.getIsAutoincrement() != each.getIsAutoincrement()) {
                    fail();
                }
                List<IndexDefinition> indexs = expect.getIndexs();
                if (indexs != null && !indexs.isEmpty()) {
                    checkIndex(each, indexs);
                }
            }
        }
    }
    
    private static void checkDatabaseColumn(final Integer expectData, final Integer actualData) {
        if (expectData != null && !expectData.equals(actualData)) {
            fail();
        }
    }
    
    private static void checkIndex(final ColumnDefinition columnDefinition, final List<IndexDefinition> indexs) {
        for (IndexDefinition each : indexs) {
            for (IndexDefinition actualIndex : columnDefinition.getIndexs()) {
                if (each.getName().equals(actualIndex.getName())) {
                    if (each.getType() != null && !each.getType().equals(actualIndex.getType())) {
                        fail();
                    }
                    if (each.isUnique() != actualIndex.isUnique()) {
                        fail();
                    }
                }
            }
        }
    }
    
    /**
     * Comparative data set.
     *
     * @param expected expected
     * @param actual actual
     */
    public static void assertDatas(final DatasetDefinition expected, final DatasetDatabase actual) {
        Map<String, List<ColumnDefinition>> actualConfigs = actual.getMetadatas();
        Map<String, List<ColumnDefinition>> expectedConfigs = expected.getMetadatas();
        for (Map.Entry<String, List<ColumnDefinition>> entry : expectedConfigs.entrySet()) {
            List<ColumnDefinition> expectedConfig = entry.getValue();
            List<ColumnDefinition> actualConfig = actualConfigs.get(entry.getKey());
            assertNotNull(actualConfig);
            checkConfig(expectedConfig, actualConfig);
        }
        Map<String, List<Map<String, String>>> actualDatass = actual.getDatas();
        Map<String, List<Map<String, String>>> expectDedatas = expected.getDatas();
        for (Map.Entry<String, List<Map<String, String>>> entry : expectDedatas.entrySet()) {
            List<Map<String, String>> data = entry.getValue();
            List<Map<String, String>> actualDatas = actualDatass.get(entry.getKey());
            assertEquals(actualDatas.size(), data.size());
            checkData(data, actualDatas);
        }
    }
    
    private static void checkData(final List<Map<String, String>> data, final List<Map<String, String>> actualDatas) {
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> expectData = data.get(i);
            Map<String, String> actualData = actualDatas.get(i);
            for (Map.Entry<String, String> entry : expectData.entrySet()) {
                if (!entry.getValue().equals(actualData.get(entry.getKey()))) {
                    String actualMsg = actualDatas.toString();
                    String expectMsg = data.toString();
                    fail("result set validation failed . describe : actual = " + actualMsg + " . expect = " + expectMsg);
                }
            }
        }
    }
    
    private static void checkConfig(final List<ColumnDefinition> expectedConfig, final List<ColumnDefinition> actualConfig) {
        for (ColumnDefinition eachColumn : expectedConfig) {
            boolean flag = false;
            for (ColumnDefinition each : actualConfig) {
                if (eachColumn.getName().equals(each.getName()) && eachColumn.getType().equals(each.getType())) {
                    flag = true;
                }
            }
            assertTrue(flag);
        }
    }
    
    /**
     * Use PreparedStatement test SQL select.
     *
     * @param connection connection
     * @param table table
     * @return query result set
     * @throws SQLException SQL exception
     */
    public static List<ColumnDefinition> getColumnDefinitions(final Connection connection, final String table) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(null, null, table, null)) {
            List<ColumnDefinition> result = new ArrayList<>();
            while (resultSet.next()) {
                ColumnDefinition columnDefinition = new ColumnDefinition();
                String column = resultSet.getString("COLUMN_NAME");
                columnDefinition.setName(column);
                int size = resultSet.getInt("COLUMN_SIZE");
                columnDefinition.setSize(size);
                String columnType = resultSet.getString("TYPE_NAME").toLowerCase();
                columnDefinition.setType(columnType);
                int decimalDigits = resultSet.getInt("DECIMAL_DIGITS");
                columnDefinition.setDecimalDigits(decimalDigits);
                int numPrecRadix = resultSet.getInt("NUM_PREC_RADIX");
                columnDefinition.setNumPrecRadix(numPrecRadix);
                int nullAble = resultSet.getInt("NULLABLE");
                columnDefinition.setNullAble(nullAble);
                String isAutoincrement = resultSet.getString("IS_AUTOINCREMENT");
                if (StringUtils.isNotEmpty(isAutoincrement)) {
                    columnDefinition.setIsAutoincrement(1);
                }
                result.add(columnDefinition);
            }
            geIndexDefinitions(metaData, result, table);
            return result;
        }
    }
    
    private static List<ColumnDefinition> geIndexDefinitions(final DatabaseMetaData databaseMetaData, final List<ColumnDefinition> columnDefinitions, final String table) throws SQLException {
        try (ResultSet resultSet = databaseMetaData.getIndexInfo(null, null, table, false, false)) {
            while (resultSet.next()) {
                IndexDefinition index = new IndexDefinition();
                String name = resultSet.getString("COLUMN_NAME");
                String nameIndex = resultSet.getString("INDEX_NAME");
                if (StringUtils.isNotEmpty(nameIndex)) {
                    index.setName(nameIndex);
                }
                String typeIndex = resultSet.getString("TYPE");
                if (StringUtils.isNotEmpty(typeIndex)) {
                    index.setType(typeIndex);
                }
                String uniqueIndex = resultSet.getString("NON_UNIQUE");
                if (StringUtils.isNotEmpty(uniqueIndex)) {
                    index.setUnique(!"TRUE".equalsIgnoreCase(uniqueIndex));
                }
                for (ColumnDefinition col : columnDefinitions) {
                    if (name.equals(col.getName())) {
                        col.getIndexs().add(index);
                        break;
                    }
                }
            }
            return columnDefinitions;
        }
    }
}
