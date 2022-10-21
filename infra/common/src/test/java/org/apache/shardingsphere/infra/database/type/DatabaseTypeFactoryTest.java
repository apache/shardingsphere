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

package org.apache.shardingsphere.infra.database.type;

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.infra.fixture.InfraDatabaseTypeFixture;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DatabaseTypeFactoryTest {
    
    @Test
    public void assertGetInstance() {
        assertThat(DatabaseTypeFactory.getInstance("INFRA.FIXTURE"), instanceOf(InfraDatabaseTypeFixture.class));
        assertThat(DatabaseTypeFactory.getInstance("SQL92"), instanceOf(SQL92DatabaseType.class));
        assertThat(DatabaseTypeFactory.getInstance("MySQL"), instanceOf(MySQLDatabaseType.class));
        assertThat(DatabaseTypeFactory.getInstance("MariaDB"), instanceOf(MariaDBDatabaseType.class));
        assertThat(DatabaseTypeFactory.getInstance("PostgreSQL"), instanceOf(PostgreSQLDatabaseType.class));
        assertThat(DatabaseTypeFactory.getInstance("OpenGauss"), instanceOf(OpenGaussDatabaseType.class));
        assertThat(DatabaseTypeFactory.getInstance("Oracle"), instanceOf(OracleDatabaseType.class));
        assertThat(DatabaseTypeFactory.getInstance("SQLServer"), instanceOf(SQLServerDatabaseType.class));
        assertThat(DatabaseTypeFactory.getInstance("H2"), instanceOf(H2DatabaseType.class));
    }
    
    @Test
    public void assertGetInstances() {
        Collection<DatabaseType> actual = DatabaseTypeFactory.getInstances();
        assertThat(actual.size(), is(9));
        Iterator<DatabaseType> iterator = actual.iterator();
        assertThat(iterator.next(), instanceOf(InfraDatabaseTypeFixture.class));
        assertThat(iterator.next(), instanceOf(SQL92DatabaseType.class));
        assertThat(iterator.next(), instanceOf(MySQLDatabaseType.class));
        assertThat(iterator.next(), instanceOf(MariaDBDatabaseType.class));
        assertThat(iterator.next(), instanceOf(PostgreSQLDatabaseType.class));
        assertThat(iterator.next(), instanceOf(OpenGaussDatabaseType.class));
        assertThat(iterator.next(), instanceOf(OracleDatabaseType.class));
        assertThat(iterator.next(), instanceOf(SQLServerDatabaseType.class));
        assertThat(iterator.next(), instanceOf(H2DatabaseType.class));
    }
}
