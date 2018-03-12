package io.shardingjdbc.dbtest.common;

import io.shardingjdbc.dbtest.config.bean.ParameterDefinition;
import io.shardingjdbc.dbtest.config.bean.ParametersDefinition;
import io.shardingjdbc.dbtest.data.DatasetDatabase;
import io.shardingjdbc.dbtest.data.DatasetDefinition;
import io.shardingjdbc.dbtest.exception.DbTestException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DatabaseUtils {

    /**
     * Map<列,数据>
     */
    public static String analyzeSql(String table, Map<String, String> config) {


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


    public static boolean insertUsePreparedStatement(Connection conn, String sql, List<Map<String, String>> datas, Map<String, String> config) throws SQLException, ParseException {
        try (PreparedStatement pstmt = conn.prepareStatement(sql);) {
            for (Map<String, String> data : datas) {
                int index = 1;
                for (Map.Entry<String, String> stringStringEntry : data.entrySet()) {
                    String key = stringStringEntry.getKey();
                    String datacol = stringStringEntry.getValue();
                    String type = "String";
                    if(config != null){
                        type = config.get(key);
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

    public static void cleanAllUsePreparedStatement(Connection conn, String table) throws SQLException {
        try (Statement pstmt = conn.createStatement();) {
            pstmt.execute("DELETE from " + table);
        }
    }

    public static boolean isSelect(String sql){
        sql = sql.trim();
        return sql.startsWith("select");
    }

    public static int updateUseStatementToExecuteUpdate(Connection conn, String sql, ParametersDefinition parameters) throws SQLException, ParseException {
        List<ParameterDefinition> parameter = parameters.getParameter();
        int num = 0;
        try (Statement pstmt = conn.createStatement()) {
            sql = sqlStatement(sql, parameter);
            num = pstmt.executeUpdate(sql);
        }
        return num;
    }

    private static String sqlStatement(String sql, List<ParameterDefinition> parameter) {
        for (ParameterDefinition parameterDefinition : parameter) {
            String type = parameterDefinition.getType();
            String datacol = parameterDefinition.getValue();
            switch (type) {
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                    sql = Pattern.compile("%s", Pattern.LITERAL).matcher(
                            sql).replaceFirst((Matcher.quoteReplacement(datacol.toString())));
                    break;
                case "boolean":
                    sql = Pattern.compile("%s", Pattern.LITERAL).matcher(
                            sql).replaceFirst((Matcher.quoteReplacement(Boolean.valueOf(datacol).toString())));
                    break;
                case "Date":
                    throw new DbTestException("暂不支持date类型");
                case "String":
                    sql = Pattern.compile("%s", Pattern.LITERAL).matcher(
                            sql).replaceFirst((Matcher.quoteReplacement("'"+datacol+"'")));
                    break;
                default:
                    sql = Pattern.compile("%s", Pattern.LITERAL).matcher(
                            sql).replaceFirst((Matcher.quoteReplacement("'"+datacol+"'")));
                    break;
            }
        }
        return sql;
    }

    public static boolean updateUseStatementToExecute(Connection conn, String sql, ParametersDefinition parameters) throws SQLException, ParseException {
        List<ParameterDefinition> parameter = parameters.getParameter();
        try (Statement pstmt = conn.createStatement()) {
            sql = sqlStatement(sql, parameter);
           return pstmt.execute(sql);
        }
    }

    public static int updateUsePreparedStatementToExecuteUpdate(Connection conn, String sql, ParametersDefinition parameters) throws SQLException, ParseException {
        List<ParameterDefinition> parameter = parameters.getParameter();
        int num = 0;
        sql = sql.replaceAll("\\%s","?");
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            sqlPreparedStatement(parameter, pstmt);
            num = pstmt.executeUpdate();
        }
        return num;
    }

    public static boolean updateUsePreparedStatementToExecute(Connection conn, String sql, ParametersDefinition parameters) throws SQLException, ParseException {
        List<ParameterDefinition> parameter = parameters.getParameter();
        int num = 0;
        sql = sql.replaceAll("\\%s","?");
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            sqlPreparedStatement(parameter, pstmt);
            return pstmt.execute();
        }
    }

    public static DatasetDatabase selectUsePreparedStatement(Connection conn, String sql, ParametersDefinition parameters) throws SQLException, ParseException {
        List<ParameterDefinition> parameter = parameters.getParameter();
        sql = sql.replaceAll("\\%s","?");
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            sqlPreparedStatement(parameter, pstmt);
            try( ResultSet resultSet = pstmt.executeQuery()){

                ResultSetMetaData rsmd = resultSet.getMetaData();
                int colsint = rsmd.getColumnCount();
                Map<String,String> cols = new LinkedHashMap<>();
                for (int i = 1; i < colsint+1; i++) {
                    String name = rsmd.getColumnName(i);
                    String type = getDataType( rsmd.getColumnType(i),rsmd.getScale(i));
                    cols.put(name,type);
                }

                Map<String,Map<String,String>> configs = new HashMap<>();
                configs.put("data",cols);

                List<Map<String,String>> ls = new ArrayList<>();
                Map<String,List<Map<String,String>>> datas = new HashMap<>();
                datas.put("data",ls);

                while (resultSet.next()) {
                    Map<String,String> data = new HashMap<>();
                    for (Map.Entry<String, String> stringStringEntry : cols.entrySet()) {
                        String name = stringStringEntry.getKey();
                        String type = stringStringEntry.getValue();
                        switch(type){
                            case "int":
                                data.put(name,String.valueOf(resultSet.getInt(name)));
                                break;
                            case "long" :
                                data.put(name,String.valueOf(resultSet.getLong(name)));
                                break;
                            case "float":
                                data.put(name,String.valueOf(resultSet.getFloat(name)));
                                break;
                            case "double":
                                data.put(name,String.valueOf(resultSet.getDouble(name)));
                                break;
                            case "boolean":
                                data.put(name,String.valueOf(resultSet.getBoolean(name)));
                                break;
                            case "char":
                                data.put(name,String.valueOf(resultSet.getString(name)));
                                break;
                            case "String":
                                data.put(name,String.valueOf(resultSet.getString(name)));
                                break;
                            case "Date":
                                data.put(name,DateFormatUtils.format(new java.util.Date(resultSet.getDate(name).getTime()),"yyyy-MM-dd"));
                                break;
                            case "Blob" :
                                data.put(name,String.valueOf(resultSet.getBlob(name)));
                                break;
                            default:
                                data.put(name,resultSet.getString(name));
                                break;
                        }
                    }
                    ls.add(data);
                }
                DatasetDatabase dd = new DatasetDatabase();
                dd.setConfigs(configs);
                dd.setDatas(datas);
                return dd;
            }
        }
    }

    public static DatasetDatabase selectUseStatement(Connection conn, String sql, ParametersDefinition parameters) throws SQLException, ParseException {
        List<ParameterDefinition> parameter = parameters.getParameter();
        try (Statement pstmt = conn.createStatement()) {
            sql = sqlStatement(sql, parameter);
            try( ResultSet resultSet = pstmt.executeQuery(sql)){
                ResultSetMetaData rsmd = resultSet.getMetaData();
                int colsint = rsmd.getColumnCount();
                Map<String,String> cols = new LinkedHashMap<>();
                for (int i = 1; i < colsint+1; i++) {
                    String name = rsmd.getColumnName(i);
                    String type = getDataType( rsmd.getColumnType(i),rsmd.getScale(i));
                    cols.put(name,type);
                }

                Map<String,Map<String,String>> configs = new HashMap<>();
                configs.put("data",cols);

                List<Map<String,String>> ls = new ArrayList<>();
                Map<String,List<Map<String,String>>> datas = new HashMap<>();
                datas.put("data",ls);

                while (resultSet.next()) {
                    Map<String,String> data = new HashMap<>();
                    for (Map.Entry<String, String> stringStringEntry : cols.entrySet()) {
                        String name = stringStringEntry.getKey();
                        String type = stringStringEntry.getValue();
                        switch(type){
                            case "int":
                                data.put(name,String.valueOf(resultSet.getInt(name)));
                                break;
                            case "long" :
                                data.put(name,String.valueOf(resultSet.getLong(name)));
                                break;
                            case "float":
                                data.put(name,String.valueOf(resultSet.getFloat(name)));
                                break;
                            case "double":
                                data.put(name,String.valueOf(resultSet.getDouble(name)));
                                break;
                            case "boolean":
                                data.put(name,String.valueOf(resultSet.getBoolean(name)));
                                break;
                            case "char":
                                data.put(name,String.valueOf(resultSet.getString(name)));
                                break;
                            case "String":
                                data.put(name,String.valueOf(resultSet.getString(name)));
                                break;
                            case "Date":
                                data.put(name,DateFormatUtils.format(new java.util.Date(resultSet.getDate(name).getTime()),"yyyy-MM-dd"));
                                break;
                            case "Blob" :
                                data.put(name,String.valueOf(resultSet.getBlob(name)));
                                break;
                            default:
                                data.put(name,resultSet.getString(name));
                                break;
                        }
                    }
                    ls.add(data);
                }
                DatasetDatabase dd = new DatasetDatabase();
                dd.setConfigs(configs);
                dd.setDatas(datas);
                return dd;
            }
        }
    }

    private static void sqlPreparedStatement(List<ParameterDefinition> parameter, PreparedStatement pstmt) throws SQLException, ParseException {
        int index = 1;
        for (ParameterDefinition parameterDefinition : parameter) {
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

    private static String getDataType(int type,int scale){
        String dataType="";

        switch(type){
            case Types.INTEGER: //-1
                dataType="int";
                break;
            case Types.LONGVARCHAR : //-1
                dataType="long";
                break;
            case Types.BIGINT: //-1
                dataType="long";
                break;
            case Types.FLOAT: //93
                dataType="float";
                break;
            case Types.DOUBLE: //93
                dataType="double";
                break;
            case Types.BOOLEAN: //-1
                dataType="boolean";
                break;
            case Types.CHAR:    //1
                dataType="char";
                break;
            case Types.NUMERIC: //2
                switch(scale){
                    case 0:
                        dataType="double";
                        break;
                    case -127:
                        dataType="float";
                        break;
                    default:
                        dataType="double";
                }
                break;
            case Types.VARCHAR:  //12
                dataType="String";
                break;
            case Types.DATE:  //91
                dataType="Date";
                break;
            case Types.TIMESTAMP: //93
                dataType="Date";
                break;
            case Types.BLOB :
                dataType="Blob";
                break;
            default:
                dataType="String";
        }
        return dataType;
    }

    /**
     * 比较方法
     * @param expected
     * @param actual
     * @param path
     */
    public static void assertDatas(DatasetDefinition expected,DatasetDatabase actual){
        Map<String,Map<String,String>> actualConfigs = actual.getConfigs();
        Map<String,Map<String,String>> expectedConfigs = expected.getConfigs();

        boolean flag = true;
        for (Map.Entry<String, Map<String, String>> stringMapEntry : expectedConfigs.entrySet()) {
            Map<String, String> config = stringMapEntry.getValue();
            Map<String, String> actualConfig = actualConfigs.get(stringMapEntry.getKey());
            assertTrue(actualConfig != null);
            for (Map.Entry<String, String> stringStringEntry : config.entrySet()) {
                assertTrue(stringStringEntry.getValue().equals(actualConfig.get(stringStringEntry.getKey())));
            }
        }

        Map<String,List<Map<String,String>>> actualDatass = actual.getDatas();
        Map<String,List<Map<String,String>>> expectDedatas = expected.getDatas();
        for (Map.Entry<String, List<Map<String, String>>> stringListEntry : expectDedatas.entrySet()) {
            List<Map<String, String>> data = stringListEntry.getValue();
            List<Map<String, String>> actualDatas = actualDatass.get(stringListEntry.getKey());

            for (int i = 0; i < data.size(); i++) {
                Map<String, String> expectData = data.get(i);
                Map<String, String> actualData = actualDatas.get(i);
                for (Map.Entry<String, String> stringStringEntry : expectData.entrySet()) {
                    assertTrue(!stringStringEntry.getValue().equals(actualData.get(stringStringEntry.getKey())));
                }

            }
        }

    }

}
