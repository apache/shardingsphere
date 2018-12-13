package io.shardingsphere.shardingjdbc.yaml;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class userTest {
    
    public static void main(String[] args) throws ClassNotFoundException, SQLException  {
//        String URL="jdbc:mysql://127.0.0.1:3306/demo_ds_0?useUnicode=true&amp;characterEncoding=utf-8";
//        String USER="root";
//        String PASSWORD="";
//        Class.forName("com.mysql.jdbc.Driver");
    
        String URL="jdbc:postgresql://db.psql:5432/master";
        String USER="postgres";
        String PASSWORD="";
        Class.forName("org.postgresql.Driver");
        
        //2.获得数据库链接
        Connection conn=DriverManager.getConnection(URL, USER, PASSWORD);
        //3.通过数据库的连接操作数据库，实现增删改查（使用Statement类）
        Statement st1=conn.createStatement();
        st1.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs1=st1.executeQuery("select * from t1");
    
        Statement st2=conn.createStatement();
        st2.setFetchSize(Integer.MIN_VALUE);
        ResultSet rs2=st2.executeQuery("select * from t2");
        
        //4.处理数据库的返回结果(使用ResultSet类)
        while(rs1.next()){
            System.out.println("t_order_0");
            System.out.println(rs1.getInt(1));
            while (rs2.next()) {
                System.out.println("t_order_1");
                System.out.println(rs2.getInt(1));
            }
        }
        
        //关闭资源
        rs1.close();
        st1.close();
        rs2.close();
        st2.close();
        conn.close();
    }
}
