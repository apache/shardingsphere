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

package org.apache.shardingsphere.route.time.mysql;

import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

public final class MySQLTimeServiceTest {

    private final File file = new File(MySQLTimeServiceTest.class.getResource("/").getPath() + "mysql-time-service.properties");

    @Test
    public void assertInitDataSource() throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        Properties properties = new Properties();
        properties.put("dataSourceType", "com.zaxxer.hikari.HikariDataSource");
        properties.put("jdbcUrl", "jdbc:mysql://localhost:3306/test");
        properties.put("username", "root");
        properties.put("password", "root");
        properties.put("driverClassName", "com.mysql.jdbc.Driver");
        properties.store(stream, null);
        stream.close();
        MySQLTimeService service = new MySQLTimeService();
        Assert.assertNotNull(getDataSource(service));
        Assert.assertTrue(file.delete());
    }

    @SneakyThrows
    private DataSource getDataSource(final MySQLTimeService service) {
        Field field = service.getClass().getDeclaredField("dataSource");
        field.setAccessible(true);
        return (DataSource) field.get(null);
    }
}
