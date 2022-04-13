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

package org.apache.shardingsphere.infra.metadata.schema.loader.spi;

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.H2TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.MySQLTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.OpenGaussTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.OracleTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.PostgreSQLTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.SQLServerTableMetaDataLoader;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public final class DialectTableMetaDataLoaderFactoryTest {

    @Test
    public void assertNewInstanceWithH2TableMetaDataLoader() {
        H2DatabaseType h2DatabaseType = new H2DatabaseType();
        Optional<DialectTableMetaDataLoader> h2TableMetaDataLoader = DialectTableMetaDataLoaderFactory.newInstance(h2DatabaseType);
        assertTrue(h2TableMetaDataLoader.isPresent());
        assertTrue(h2TableMetaDataLoader.get() instanceof H2TableMetaDataLoader);
    }

    @Test
    public void assertNewInstanceWithOracleTableMetaDataLoader() {
        OracleDatabaseType oracleDatabaseType = new OracleDatabaseType();
        Optional<DialectTableMetaDataLoader> oracleTableMetaDataLoader = DialectTableMetaDataLoaderFactory.newInstance(oracleDatabaseType);
        assertTrue(oracleTableMetaDataLoader.isPresent());
        assertTrue(oracleTableMetaDataLoader.get() instanceof OracleTableMetaDataLoader);
    }

    @Test
    public void assertNewInstanceWithSQLServerTableMetaDataLoader() {
        SQLServerDatabaseType sqlServerDatabaseType = new SQLServerDatabaseType();
        Optional<DialectTableMetaDataLoader> sQLServerTableMetaDataLoader = DialectTableMetaDataLoaderFactory.newInstance(sqlServerDatabaseType);
        assertTrue(sQLServerTableMetaDataLoader.isPresent());
        assertTrue(sQLServerTableMetaDataLoader.get() instanceof SQLServerTableMetaDataLoader);
    }

    @Test
    public void assertNewInstanceWithOpenGaussDatabaseType() {
        OpenGaussDatabaseType openGaussDatabaseType = new OpenGaussDatabaseType();
        Optional<DialectTableMetaDataLoader> openGaussTableMetaDataLoader = DialectTableMetaDataLoaderFactory.newInstance(openGaussDatabaseType);
        assertTrue(openGaussTableMetaDataLoader.isPresent());
        assertTrue(openGaussTableMetaDataLoader.get() instanceof OpenGaussTableMetaDataLoader);
    }

    @Test
    public void assertNewInstanceWithMySQLTableMetaDataLoader() {
        MySQLDatabaseType mySQLDatabaseType = new MySQLDatabaseType();
        Optional<DialectTableMetaDataLoader> mySQLTableMetaDataLoader = DialectTableMetaDataLoaderFactory.newInstance(mySQLDatabaseType);
        assertTrue(mySQLTableMetaDataLoader.isPresent());
        assertTrue(mySQLTableMetaDataLoader.get() instanceof MySQLTableMetaDataLoader);
    }

    @Test
    public void assertNewInstanceWithPostgreSQLTableMetaDataLoader() {
        PostgreSQLDatabaseType postgreSQLDatabaseType = new PostgreSQLDatabaseType();
        Optional<DialectTableMetaDataLoader> postgreSQLTableMetaDataLoader = DialectTableMetaDataLoaderFactory.newInstance(postgreSQLDatabaseType);
        assertTrue(postgreSQLTableMetaDataLoader.isPresent());
        assertTrue(postgreSQLTableMetaDataLoader.get() instanceof PostgreSQLTableMetaDataLoader);
    }
}
