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

package org.apache.shardingsphere.infra.database.metadata;

import org.apache.shardingsphere.infra.database.metadata.dialect.H2DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.MariaDBDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.MySQLDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.OracleDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.PostgreSQLDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.SQL92DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.dialect.SQLServerDataSourceMetaData;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public final class DataSourceMetaDataTest {
    
    @Test
    public void assertIsInSameDatabaseInstanceWithH2() {
        H2DataSourceMetaData actual1 = new H2DataSourceMetaData("jdbc:h2:tcp://localhost:8082/~/test1/test2;DB_CLOSE_DELAY=-1");
        H2DataSourceMetaData actual2 = new H2DataSourceMetaData("jdbc:h2:tcp://localhost:8082/~/test1/test2;DB_CLOSE_DELAY=-1");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithMysql() {
        MySQLDataSourceMetaData actual1 = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9999/ds_1?serverTimezone=UTC&useSSL=false");
        MySQLDataSourceMetaData actual2 = new MySQLDataSourceMetaData("jdbc:mysql://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithOracle() {
        OracleDataSourceMetaData actual1 = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9999/ds_0", "test");
        OracleDataSourceMetaData actual2 = new OracleDataSourceMetaData("jdbc:oracle:thin:@//127.0.0.1:9999/ds_1", "test");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithMariaDB() {
        MariaDBDataSourceMetaData actual1 = new MariaDBDataSourceMetaData("jdbc:mariadb://127.0.0.1:9999/ds_0?serverTimezone=UTC&useSSL=false");
        MariaDBDataSourceMetaData actual2 = new MariaDBDataSourceMetaData("jdbc:mariadb://127.0.0.1:9999/ds_1?serverTimezone=UTC&useSSL=false");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithPostgreSQL() {
        PostgreSQLDataSourceMetaData actual1 = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        PostgreSQLDataSourceMetaData actual2 = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_1");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithSQL92() {
        SQL92DataSourceMetaData actual1 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_0");
        SQL92DataSourceMetaData actual2 = new SQL92DataSourceMetaData("jdbc:sql92_db:ds_1");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
    
    @Test
    public void assertIsInSameDatabaseInstanceWithSQLServer() {
        SQLServerDataSourceMetaData actual1 = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_0");
        SQLServerDataSourceMetaData actual2 = new SQLServerDataSourceMetaData("jdbc:microsoft:sqlserver://127.0.0.1:9999;DatabaseName=ds_1");
        assertTrue(actual1.isInSameDatabaseInstance(actual2));
    }
}
