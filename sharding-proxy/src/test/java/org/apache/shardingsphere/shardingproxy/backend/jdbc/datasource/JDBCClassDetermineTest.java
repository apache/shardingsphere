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

package org.apache.shardingsphere.shardingproxy.backend.jdbc.datasource;

import lombok.SneakyThrows;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JDBCClassDetermineTest {
    
    private JDBCClassDetermine jdbcClassDetermine = new JDBCClassDetermine();
    
    @Test
    public void assertMySQLUrl() {
        assertThat(jdbcClassDetermine.getDriverClassName("jdbc:mysql://localhost:3306/demo_ds_master"), is("com.mysql.jdbc.Driver"));
    }
    
    @Test
    public void assertPostgreSQLUrl() {
        assertThat(jdbcClassDetermine.getDriverClassName("jdbc:postgresql://db.psql:5432/postgres"), is("org.postgresql.Driver"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @SneakyThrows
    public void assertFailedUrl() {
        assertThat(jdbcClassDetermine.getDriverClassName("jdbc:oracle://db.psql:5432/postgres"), is("com.mysql.jdbc.Driver"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @SneakyThrows
    public void assertFailedUrl2() {
        assertThat(jdbcClassDetermine.getDriverClassName("xxxx"), is("com.mysql.jdbc.Driver"));
    }
}
