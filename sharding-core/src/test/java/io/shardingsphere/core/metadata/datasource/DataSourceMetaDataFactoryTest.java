/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.metadata.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.metadata.datasource.dialect.H2DataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.MySQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.OracleDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.PostgreSQLDataSourceMetaData;
import io.shardingsphere.core.metadata.datasource.dialect.SQLServerDataSourceMetaData;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DataSourceMetaDataFactoryTest {
    
    @Test
    public void assertAllNewInstanceForH2() {
        assertTrue(DataSourceMetaDataFactory.newInstance(DatabaseType.H2, "jdbc:h2:mem:ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL") instanceof H2DataSourceMetaData);
    }
    
    @Test
    public void assertAllNewInstanceForMySQL() {
        assertTrue(DataSourceMetaDataFactory.newInstance(DatabaseType.MySQL, "jdbc:mysql://127.0.0.1:3306/db_0") instanceof MySQLDataSourceMetaData);
    }
    
    @Test
    public void assertAllNewInstanceForOracle() {
        assertTrue(DataSourceMetaDataFactory.newInstance(DatabaseType.Oracle, "jdbc:oracle:thin:@//127.0.0.1:3306/ds_0") instanceof OracleDataSourceMetaData);
    }
    
    @Test
    public void assertAllNewInstanceForPostgreSQL() {
        assertTrue(DataSourceMetaDataFactory.newInstance(DatabaseType.PostgreSQL, "jdbc:postgresql://127.0.0.1:3306/ds_0") instanceof PostgreSQLDataSourceMetaData);
    }
    
    @Test
    public void assertAllNewInstanceForSQLServer() {
        assertTrue(DataSourceMetaDataFactory.newInstance(DatabaseType.SQLServer, "jdbc:microsoft:sqlserver://127.0.0.1:3306;DatabaseName=ds_0") instanceof SQLServerDataSourceMetaData);
    }
}
