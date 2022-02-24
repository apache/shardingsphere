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

package org.apache.shardingsphere.infra.datasource.pool.destroyer;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

public final class DataSourcePoolDestroyerFactoryTest {
    
    @Test(timeout = 60000L)
    public void assertDestroyForHikari() throws InterruptedException, SQLException {
        HikariDataSource dataSource = new HikariDataSource();
        DataSourcePoolDestroyerFactory.destroy(dataSource);
        while (!dataSource.isClosed()) {
            Thread.sleep(10L);
        }
    }

    @Test
    public void assertDestroyForDefault() throws InterruptedException, SQLException {
        BasicDataSource dataSource = new BasicDataSource();
        DataSourcePoolDestroyerFactory.destroy(dataSource);
        assertTrue(dataSource.isClosed());
    }
}
