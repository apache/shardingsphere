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

package org.apache.shardingsphere.driver;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShardingSphereDriverTest {
    
    @Test
    void assertConnectWithInvalidURL() {
        assertThrows(SQLException.class, () -> DriverManager.getConnection("jdbc:invalid:xxx"));
    }
    
    @Test
    void assertDriverWorks() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, instanceOf(ShardingSphereConnection.class));
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT)");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 101), (2, 102)");
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) FROM t_order")) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(2));
            }
        }
    }
    
    @Test
    void assertVarbinaryColumnWorks() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/foo-driver-fixture.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, instanceOf(ShardingSphereConnection.class));
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id VARBINARY(64) PRIMARY KEY, user_id INT)");
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO t_order (order_id, user_id) VALUES (?, ?)");
            preparedStatement.setBytes(1, new byte[]{-1, 0, 1});
            preparedStatement.setInt(2, 101);
            int updatedCount = preparedStatement.executeUpdate();
            assertThat(updatedCount, is(1));
            try (ResultSet resultSet = statement.executeQuery("SELECT COUNT(1) FROM t_order")) {
                assertTrue(resultSet.next());
                assertThat(resultSet.getInt(1), is(1));
            }
        }
    }
    
    @Test
    void assertSelectOracle() throws SQLException {
        try (
                Connection connection = DriverManager.getConnection("jdbc:shardingsphere:classpath:config/driver/oracle-driver-fixture.yaml");
                Statement statement = connection.createStatement()) {
            assertThat(connection, instanceOf(ShardingSphereConnection.class));
            
            ResultSet resultSet1 = statement.executeQuery("select *\n" +
                    "from (select tmp_tb.*, ROWNUM row_id\n" +
                    "      from (SELECT bsc.*, ue.USR_TYP_CD, ue.REG_CHL_CD\n" +
                    "            FROM (SELECT distinct u.ID,\n" +
                    "                                  u.USR_NM,\n" +
                    "                                  u.OPEN_ID,\n" +
                    "                                  u.MP,\n" +
                    "                                  u.E_MAIL,\n" +
                    "                                  u.QQ,\n" +
                    "                                  u.IS_SYS_DEF,\n" +
                    "                                  u.IS_VALID,\n" +
                    "                                  u.IS_DEL,\n" +
                    "                                  u.CRT_TM,\n" +
                    "                                  u.CRT_USR_ID,\n" +
                    "                                  u.MDF_TM,\n" +
                    "                                  u.MDF_USR_ID,\n" +
                    "                                  us.IS_SYS_DEF      as REF_IS_SYS_DEF,\n" +
                    "                                  us.RL_NM           as REF_RL_NM,\n" +
                    "                                  us.ID              as REF_ID,\n" +
                    "                                  us.PWD             as REF_PWD,\n" +
                    "                                  us.SYS_CD          as REF_SYS_CD,\n" +
                    "                                  us.REG_TYP_CD      as REF_REG_TYP_CD,\n" +
                    "                                  us.REG_DT          as REF_REG_DT,\n" +
                    "                                  us.IS_VALID        as REF_IS_VALID,\n" +
                    "                                  us.IS_DEL          as REF_IS_DEL,\n" +
                    "                                  us.CERT_TYP_CD     as REF_CERT_TYP_CD,\n" +
                    "                                  us.CERT_NO         as REF_CERT_NO,\n" +
                    "                                  us.REF_MP          as REF_REF_MP,\n" +
                    "                                  us.NICK_NM         as REF_NICK_NM,\n" +
                    "                                  us.LATELY_LOGIN_TM as REF_LATELY_LOGIN_TM,\n" +
                    "                                  us.NO_TALK_TYP     as REF_NO_TALK_TYP,\n" +
                    "                                  us.NO_TALK_TM      as REF_NO_TALK_TM,\n" +
                    "                                  us.REF_USR_ID      as REF_REF_USR_ID,\n" +
                    "                                  us.REF_USR_BUS_TYP as REF_REF_USR_BUS_TYP,\n" +
                    "                                  us.SALT            as REF_SALT,\n" +
                    "                                  us.PWD_CRT_TM      as REF_PWD_CRT_TM,\n" +
                    "                                  us.LOCK_STATE      as REF_LOCK_STATE,\n" +
                    "                                  us.IS_LEAVE        as REF_IS_LEAVE,\n" +
                    "                                  us.PER_TYPE_CD     as REF_PER_TYPE_CD,\n" +
                    "                                  us.MP_STS_CD       as REF_MP_STS_CD\n" +
                    "\n" +
                    "                  FROM SYS_USR u\n" +
                    "                           INNER JOIN SYS_USR_SYS_R us ON u.id = us.usr_id\n" +
                    "                  WHERE u.IS_DEL = 0\n" +
                    "                    AND us.IS_DEL = 0\n" +
                    "                    AND us.SYS_CD = 11000) bsc\n" +
                    "                     left join (select ext.*\n" +
                    "                                from (select USR_ID,\n" +
                    "                                             USR_TYP_CD,\n" +
                    "                                             REG_CHL_CD,\n" +
                    "                                             row_number() over (partition by USR_ID order by CRT_TM) as row_ord\n" +
                    "                                      from SYS_USR_EXT_INFO) ext\n" +
                    "                                where ext.row_ord = 1) ue on bsc.id = ue.usr_id\n" +
                    "            ORDER BY bsc.CRT_TM DESC, bsc.\"ID\") tmp_tb\n" +
                    "      where ROWNUM <= 20)\n" +
                    "where row_id > 0");
            
            statement.executeQuery("SELECT bsc.*,ue.USR_TYP_CD,ue.REG_CHL_CD FROM (\n" +
                    "                SELECT \n" +
                    "\n" +
                    "                u.ID,u.USR_NM,u.OPEN_ID,u.MP,u.E_MAIL,u.QQ,u.IS_SYS_DEF,u.IS_VALID,u.IS_DEL,\n" +
                    "                u.CRT_TM,u.CRT_USR_ID,u.MDF_TM,u.MDF_USR_ID\n" +
                    "\n" +
                    "                FROM SYS_USR u\n" +
                    "                WHERE u.ID = 1 and u.IS_DEL = 0) bsc\n" +
                    "                left join (select ext.* from(select USR_ID,USR_TYP_CD,REG_CHL_CD,\n" +
                    "                                row_number() over(partition by USR_ID order by CRT_TM) as row_ord \n" +
                    "                                from SYS_USR_EXT_INFO \n" +
                    "                                ) ext where ext.row_ord = 1) ue on bsc.id = ue.usr_id");
            
        }
    }
}
