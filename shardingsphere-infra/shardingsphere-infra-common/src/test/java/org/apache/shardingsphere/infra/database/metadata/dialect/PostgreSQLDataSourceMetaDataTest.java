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

package org.apache.shardingsphere.infra.database.metadata.dialect;

import org.apache.shardingsphere.infra.database.metadata.UnrecognizedDatabaseURLException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class PostgreSQLDataSourceMetaDataTest {
    
    @Test
    public void assertNewConstructorWithPort() {
        PostgreSQLDataSourceMetaData actual = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1:9999/ds_0");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(9999));
        assertThat(actual.getCatalog(), is("ds_0"));
        assertNull(actual.getSchema());
    }
    
    @Test
    public void assertNewConstructorWithDefaultPort() {
        PostgreSQLDataSourceMetaData actual = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1/ds_0");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(5432));
        assertThat(actual.getCatalog(), is("ds_0"));
        assertNull(actual.getSchema());
    }

    @Test
    public void assertMultipleDatabases() {
        PostgreSQLDataSourceMetaData actual = new PostgreSQLDataSourceMetaData("jdbc:postgresql://127.0.0.1:5432,127.0.0.1:5433,127.0.0.1:5434/ds_0?targetServerType=master");
        assertThat(actual.getHostName(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(5432));
        assertThat(actual.getCatalog(), is("ds_0"));
        assertNull(actual.getSchema());
    }
    
    @Test(expected = UnrecognizedDatabaseURLException.class)
    public void assertNewConstructorFailure() {
        new PostgreSQLDataSourceMetaData("jdbc:postgresql:xxxxxxxx");
    }
}
