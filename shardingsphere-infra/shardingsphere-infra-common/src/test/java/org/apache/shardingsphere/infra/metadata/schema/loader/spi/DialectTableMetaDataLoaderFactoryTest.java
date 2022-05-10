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
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.H2SchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.MySQLSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.OpenGaussSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.OracleSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.PostgreSQLSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.loader.dialect.SQLServerSchemaMetaDataLoader;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertTrue;

public final class DialectTableMetaDataLoaderFactoryTest {
    
    @Test
    public void assertNewInstanceWithH2TableMetaDataLoader() {
        H2DatabaseType h2DatabaseType = new H2DatabaseType();
        Optional<DialectSchemaMetaDataLoader> h2TableMetaDataLoader = DialectTableMetaDataLoaderFactory.findInstance(h2DatabaseType);
        assertTrue(h2TableMetaDataLoader.isPresent());
        assertTrue(h2TableMetaDataLoader.get() instanceof H2SchemaMetaDataLoader);
    }
    
    @Test
    public void assertNewInstanceWithOracleTableMetaDataLoader() {
        OracleDatabaseType oracleDatabaseType = new OracleDatabaseType();
        Optional<DialectSchemaMetaDataLoader> oracleTableMetaDataLoader = DialectTableMetaDataLoaderFactory.findInstance(oracleDatabaseType);
        assertTrue(oracleTableMetaDataLoader.isPresent());
        assertTrue(oracleTableMetaDataLoader.get() instanceof OracleSchemaMetaDataLoader);
    }
    
    @Test
    public void assertNewInstanceWithSQLServerTableMetaDataLoader() {
        SQLServerDatabaseType sqlServerDatabaseType = new SQLServerDatabaseType();
        Optional<DialectSchemaMetaDataLoader> sQLServerTableMetaDataLoader = DialectTableMetaDataLoaderFactory.findInstance(sqlServerDatabaseType);
        assertTrue(sQLServerTableMetaDataLoader.isPresent());
        assertTrue(sQLServerTableMetaDataLoader.get() instanceof SQLServerSchemaMetaDataLoader);
    }
    
    @Test
    public void assertNewInstanceWithOpenGaussDatabaseType() {
        OpenGaussDatabaseType openGaussDatabaseType = new OpenGaussDatabaseType();
        Optional<DialectSchemaMetaDataLoader> openGaussTableMetaDataLoader = DialectTableMetaDataLoaderFactory.findInstance(openGaussDatabaseType);
        assertTrue(openGaussTableMetaDataLoader.isPresent());
        assertTrue(openGaussTableMetaDataLoader.get() instanceof OpenGaussSchemaMetaDataLoader);
    }
    
    @Test
    public void assertNewInstanceWithMySQLTableMetaDataLoader() {
        MySQLDatabaseType mySQLDatabaseType = new MySQLDatabaseType();
        Optional<DialectSchemaMetaDataLoader> mySQLTableMetaDataLoader = DialectTableMetaDataLoaderFactory.findInstance(mySQLDatabaseType);
        assertTrue(mySQLTableMetaDataLoader.isPresent());
        assertTrue(mySQLTableMetaDataLoader.get() instanceof MySQLSchemaMetaDataLoader);
    }
    
    @Test
    public void assertNewInstanceWithPostgreSQLTableMetaDataLoader() {
        PostgreSQLDatabaseType postgreSQLDatabaseType = new PostgreSQLDatabaseType();
        Optional<DialectSchemaMetaDataLoader> postgreSQLTableMetaDataLoader = DialectTableMetaDataLoaderFactory.findInstance(postgreSQLDatabaseType);
        assertTrue(postgreSQLTableMetaDataLoader.isPresent());
        assertTrue(postgreSQLTableMetaDataLoader.get() instanceof PostgreSQLSchemaMetaDataLoader);
    }
}
