package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import io.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JDBCClassDetermine {

    /*
    for MySQL-connector-java 8.x compatibility
    TODO getXADataSourceClassName
     */
    static String mysqlclass=null;

    private Pattern pattern = Pattern.compile("jdbc:(mysql|postgresql):.*", Pattern.CASE_INSENSITIVE);


    public String getDriverClassName(String url) {

        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            String databaseType = matcher.group(1).toLowerCase();
            switch (databaseType) {
                case "mysql":
                    new MySQLDataSourceMetaData(url);
                    if (mysqlclass==null){
                        try{
                            Class.forName("com.mysql.jdbc.Driver");
                            mysqlclass="com.mysql.jdbc.Driver";
                        }catch (ClassNotFoundException e){
                            try {
                                Class.forName("com.mysql.cj.jdbc.Driver");
                                mysqlclass="com.mysql.cj.jdbc.Driver";
                            }catch (ClassNotFoundException mysql8_e){
                                throw new UnsupportedOperationException(String.format("Cannot support url `%s`,no vailed mysql driver found", url));
                            }
                        }
                    }
                    return  mysqlclass;
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
