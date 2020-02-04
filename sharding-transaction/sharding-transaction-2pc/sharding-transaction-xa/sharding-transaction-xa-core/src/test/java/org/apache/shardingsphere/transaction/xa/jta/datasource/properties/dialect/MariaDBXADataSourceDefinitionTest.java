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

import org.apache.shardingsphere.core.config.DatabaseAccessConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MariaDBXADataSourceDefinitionTest {
    
    @Test
    public void assertGetXADriverClassName() {
        assertThat(new MariaDBXADataSourceDefinition().getXADriverClassName(),
                CoreMatchers.<Collection<String>>is(Arrays.asList(org.mariadb.jdbc.MariaDbDataSource.class.getName())));
    }
    
    @Test
    public void assertGetXAProperties() {
        Properties actual = new MariaDBXADataSourceDefinition().getXAProperties(new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/demo", "root", "root"));
        assertThat(actual.getProperty("user"), is("root"));
        assertThat(actual.getProperty("password"), is("root"));
        assertThat(actual.getProperty("url"), is("jdbc:mysql://127.0.0.1:3306/demo"));
        assertThat(actual.getProperty("ServerName"), is("127.0.0.1"));
        assertThat(actual.getProperty("port"), is("3306"));
        assertThat(actual.getProperty("DatabaseName"), is("demo"));
    }
}
