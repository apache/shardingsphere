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

package org.apache.shardingsphere.rdl.parser.binder.util;

import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.rdl.parser.statement.rdl.DataSourceConnectionSegment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceConnectionUrlUtilTest {

    @Test
    public void assertMySQLGetUrl() {
        DataSourceConnectionSegment segment = new DataSourceConnectionSegment();
        segment.setHostName("127.0.0.1");
        segment.setDb("test");
        segment.setUser("root");
        segment.setPort("3306");
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        String actual = DataSourceConnectionUrlUtil.getUrl(segment, databaseType);
        String expected = String.format("%s//%s:%s/%s", "jdbc:mysql:",
                segment.getHostName(), segment.getPort(), segment.getDb());
        assertThat(actual, is(expected));
    }

    @Test
    public void assertPostgreSQLGetUrl() {
        DataSourceConnectionSegment segment = new DataSourceConnectionSegment();
        segment.setHostName("127.0.0.1");
        segment.setDb("test");
        segment.setUser("root");
        segment.setPort("3306");

        PostgreSQLDatabaseType databaseType = new PostgreSQLDatabaseType();
        String actual = DataSourceConnectionUrlUtil.getUrl(segment, databaseType);
        String expected = String.format("%s//%s:%s/%s", "jdbc:postgresql:",
                segment.getHostName(), segment.getPort(), segment.getDb());
        assertThat(actual, is(expected));
    }
}
