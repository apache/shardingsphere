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

package org.apache.shardingsphere.test.e2e;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Test {
    
    public static void main(String[] args) {
        
        // 第7步把局部变量变成全局变量
        Connection con = null;
        Statement stat = null;
        // 结果集也是一个对象，后面需要关闭它
        ResultSet rs = null;
        
        try {
            // 1.注册驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 2.定义sql脚本
            String sql = "show full tables";
            // 3.获取Connection Connection 在try外面定义后进行赋值
            con = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3307/proxy_db?userSSL=false", "root", "root");
            // 4.获取执行sql对象 Statement Statement stat 在try外面进行定义后赋值
            stat = con.createStatement();
            // 5.执行sql语句
            rs = stat.executeQuery(sql); // 影响行数
            // 6.处理结果
            ResultSetPrinter.printResultSet(rs);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        } finally {
            // 7.释放资源
            // 避免空指针异常 stat.close() ;
            // rs是最后使用的要先关闭,stat 使用con对象，所以应该先释放stat对象，释放后面的对象再释放前面的
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            if (stat != null) {
                try {
                    stat.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    stat.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        
    }
}
