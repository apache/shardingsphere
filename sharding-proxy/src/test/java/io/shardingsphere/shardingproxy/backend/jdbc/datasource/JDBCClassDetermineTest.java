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

package io.shardingsphere.shardingproxy.backend.jdbc.datasource;

import lombok.SneakyThrows;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JDBCClassDetermineTest {
    
    private JDBCClassDetermine jdbcClassDetermine = new JDBCClassDetermine();
    
    @Test
    public void assertMySQLUrl() {
        assertEquals("com.mysql.jdbc.Driver", jdbcClassDetermine.getDriverClassName("jdbc:mysql://localhost:3306/demo_ds_master"));
    }
    
    @Test
    public void assertPostgreSQLUrl() {
        assertEquals("org.postgresql.Driver", jdbcClassDetermine.getDriverClassName("jdbc:postgresql://db.psql:5432/postgres"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @SneakyThrows
    public void assertFailedUrl() {
        assertEquals("com.mysql.jdbc.Driver", jdbcClassDetermine.getDriverClassName("jdbc:oracle://db.psql:5432/postgres"));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    @SneakyThrows
    public void assertFailedUrl2() {
        assertEquals("com.mysql.jdbc.Driver", jdbcClassDetermine.getDriverClassName("xxxx"));
    }
}
