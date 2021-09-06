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

package org.apache.shardingsphere.transaction.xa.jta.datasource.properties;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.H2XADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.MariaDBXADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.MySQLXADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.OracleXADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.PostgreSQLXADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.SQLServerXADataSourceDefinition;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class XADataSourceDefinitionFactoryTest {
    
    @Test
    public void assertCreateXAPropertiesForH2() {
        assertThat(XADataSourceDefinitionFactory.getXADataSourceDefinition(DatabaseTypeRegistry.getActualDatabaseType("H2")), instanceOf(H2XADataSourceDefinition.class));
    }
    
    @Test
    public void assertCreateXAPropertiesForMySQL() {
        assertThat(XADataSourceDefinitionFactory.getXADataSourceDefinition(DatabaseTypeRegistry.getActualDatabaseType("MySQL")), instanceOf(MySQLXADataSourceDefinition.class));
    }

    @Test
    public void assertCreateXAPropertiesForMariaDB() {
        assertThat(XADataSourceDefinitionFactory.getXADataSourceDefinition(DatabaseTypeRegistry.getActualDatabaseType("MariaDB")), instanceOf(MariaDBXADataSourceDefinition.class));
    }
    
    @Test
    public void assertCreateXAPropertiesForPostgreSQL() {
        assertThat(XADataSourceDefinitionFactory.getXADataSourceDefinition(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL")), instanceOf(PostgreSQLXADataSourceDefinition.class));
    }
    
    @Test
    public void assertCreateXAPropertiesForOracle() {
        assertThat(XADataSourceDefinitionFactory.getXADataSourceDefinition(DatabaseTypeRegistry.getActualDatabaseType("Oracle")), instanceOf(OracleXADataSourceDefinition.class));
    }
    
    @Test
    public void assertCreateXAPropertiesForSQLServer() {
        assertThat(XADataSourceDefinitionFactory.getXADataSourceDefinition(DatabaseTypeRegistry.getActualDatabaseType("SQLServer")), instanceOf(SQLServerXADataSourceDefinition.class));
    }
}
