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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

//
//
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.postgresql.Driver;
//import org.postgresql.core.BaseConnection;
//import org.postgresql.jdbc.PgArray;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.utility.DockerImageName;
//
//import java.nio.charset.StandardCharsets;
//import java.sql.*;
//
//import java.util.Properties;
//import java.util.TimeZone;
//
//import static org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLArrayBinaryProtocolValue.instance;
//import static org.junit.jupiter.api.Assertions.*;
//
public class PostgreSQLArrayBinaryProtocolValueTest {
    // private static PostgreSQLContainer pgsqlContainer;
    // private static Connection connection;
    //
    // @BeforeAll
    // public static void setup() throws SQLException {
    // pgsqlContainer = new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
    // Driver driver = new Driver();
    //
    // Properties info = new Properties();
    // info.put("user", pgsqlContainer.getUsername());
    // info.put("password", pgsqlContainer.getPassword());
    // pgsqlContainer.start();
    // connection = driver.connect(pgsqlContainer.getJdbcUrl(), info);
    //
    // }
    //
    //
    // @AfterAll
    // public static void close() throws SQLException {
    // if (connection != null) {
    // connection.close();
    // }
    // pgsqlContainer.close();
    // }
    //
    // @Test
    // public void testArrayBinaryProtocolValue() throws SQLException {
    // testReadWrite("varchar", new String[][]{
    // {"a", "b"}, {"c", "d"}
    // });
    // testReadWrite("text", new String[][]{
    // {"a", "b"}, {"c", "d"}
    // });
    // testReadWrite("bool", new Boolean[][]{
    // {true, false}, {false, true}, {true, true}
    // });
    //
    // testReadWrite("int4", new Integer[][]{
    // {1, 2}, {3, 4}
    // });
    // testReadWrite("float4", new Float[][]{
    // {1.1f, 2.2f}, {3.3f, 4.4f}
    // });
    // testReadWrite("float8", new Double[][]{
    // {1.1, 2.2}, {3.3, 4.4}
    // });
    // testReadWrite("int8", new Long[][]{
    // {100L, 200L}, {300L, 400L}
    // });
    //
    // testReadWrite("date", new java.sql.Date[]{
    // new Date(-TimeZone.getDefault().getRawOffset()),
    // new Date(-TimeZone.getDefault().getRawOffset())
    // });
    // testReadWrite("timestamp", new java.sql.Timestamp[]{
    // java.sql.Timestamp.valueOf("2022-01-01 10:10:10"),
    // java.sql.Timestamp.valueOf("2022-02-01 20:20:20")
    // });
    // testReadWrite("time", new java.sql.Time[]{
    // new java.sql.Time(0),
    // new java.sql.Time(0)
    // });
    //
    // testReadWrite("numeric", new Number[]{
    // new java.math.BigDecimal("123.45"),
    // new java.math.BigDecimal("678.90"),
    // Double.NaN,
    //
    // });
    //
    //
    // }
    //
    //
    // private PgArray createBinaryPgArray(String baseTypeName, Object[] array) throws SQLException {
    // return (PgArray) connection.createArrayOf(baseTypeName, array);
    // }
    //
    // private PgArray createTextPgArray(PgArray pgArray) throws SQLException {
    // BaseConnection baseConnection = (BaseConnection) connection;
    //
    // int pgArrayType = baseConnection.getTypeInfo().getPGArrayType(pgArray.getBaseTypeName());
    // return new PgArray(baseConnection, pgArrayType, pgArray.toString());
    // }
    //
    // private void testReadWrite(String baseTypeName, Object[] array) throws SQLException {
    // PgArray binaryPgArray = createBinaryPgArray(baseTypeName, array);
    // PgArray textPgArray = createTextPgArray(binaryPgArray);
    // ByteBuf buffer = Unpooled.buffer();
    // PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(buffer, StandardCharsets.UTF_8);
    // instance.write(payload, textPgArray);
    // Object read = instance.read(payload, payload.getByteBuf().readInt());
    // }
    //
    //
}
