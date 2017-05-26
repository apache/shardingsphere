/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.spring;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/namespace/withNamespaceGenerateKeyColumns.xml")
public class GenerateKeyDBUnitTest extends AbstractSpringDBUnitTest {
    
    @Test
    public void test() throws SQLException {
        try (Connection connection = getShardingDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO `t_order` (`user_id`, `status`) VALUES (1, 'init')", Statement.RETURN_GENERATED_KEYS);
            assertTrue(statement.getGeneratedKeys().next());
            assertEquals(statement.getGeneratedKeys().getLong(1), 101L);
            statement.execute("INSERT INTO `t_order_item` (`order_id`, `user_id`, `status`) VALUES (101, 1, 'init')", Statement.RETURN_GENERATED_KEYS);
            assertTrue(statement.getGeneratedKeys().next());
            assertEquals(statement.getGeneratedKeys().getLong(1), 99L);
        }
    }
}
