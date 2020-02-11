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

package org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect;

import org.apache.shardingsphere.underlying.common.config.DatabaseAccessConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class PostgreSQLXADataSourceDefinitionTest {
    
    @Test
    public void assertGetXADriverClassName() {
        assertThat(new PostgreSQLXADataSourceDefinition().getXADriverClassName(), CoreMatchers.<Collection<String>>is(Collections.singletonList("org.postgresql.xa.PGXADataSource")));
    }
    
    @Test
    public void assertGetXAProperties() {
        Properties actual = new PostgreSQLXADataSourceDefinition().getXAProperties(new DatabaseAccessConfiguration("jdbc:postgresql://db.psql:5432/test_db", "root", "root"));
        assertThat(actual.getProperty("user"), is("root"));
        assertThat(actual.getProperty("password"), is("root"));
        assertThat(actual.getProperty("serverName"), is("db.psql"));
        assertThat(actual.getProperty("portNumber"), is("5432"));
        assertThat(actual.getProperty("databaseName"), is("test_db"));
    }
}
