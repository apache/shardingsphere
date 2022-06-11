package org.apache.shardingsphere.proxy.database;

import org.junit.Test;

import java.sql.*;

import static org.junit.Assert.*;

public class MysqlTest {

    @Test
    public void test() throws SQLException {

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3307/db", "root", "root")) {
            final Statement stmt = conn.createStatement();

//            stmt.executeUpdate("create table if not exists t1(a int, b varchar(10))");
//
//            stmt.executeUpdate("insert into t1 values(1,'hello'),(2,'jimo')");

            final ResultSet rs = stmt.executeQuery("select started_at,st_aswkt(start_point) from t_test");
            final ResultSetMetaData md = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    System.out.print(rs.getObject(i) + ",");
                }
                System.out.println();
            }
        }
    }
}
