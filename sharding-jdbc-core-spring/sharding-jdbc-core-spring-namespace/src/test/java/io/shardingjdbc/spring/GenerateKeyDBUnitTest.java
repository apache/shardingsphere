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

package io.shardingjdbc.spring;

import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.TableRule;
import io.shardingjdbc.spring.fixture.DecrementKeyGenerator;
import io.shardingjdbc.spring.fixture.IncrementKeyGenerator;
import io.shardingjdbc.spring.util.FieldValueUtil;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/withNamespaceGenerateKeyColumns.xml")
public class GenerateKeyDBUnitTest extends AbstractSpringDBUnitTest {
    
    @Resource
    private ShardingDataSource shardingDataSource;
    
    @Test
    public void assertGenerateKey() throws SQLException {
        try (Connection connection = getShardingDataSource().getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO `t_order` (`user_id`, `status`) VALUES (1, 'init')", Statement.RETURN_GENERATED_KEYS);
            ResultSet generateKeyResultSet = statement.getGeneratedKeys();
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(101L));
            statement.execute("INSERT INTO `t_order_item` (`order_id`, `user_id`, `status`) VALUES (101, 1, 'init')", Statement.RETURN_GENERATED_KEYS);
            generateKeyResultSet = statement.getGeneratedKeys();
            assertTrue(generateKeyResultSet.next());
            assertThat(generateKeyResultSet.getLong(1), is(99L));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateKeyColumn() {
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        assertNotNull(shardingContext);
        Object shardingRule = FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
        assertNotNull(shardingRule);
        Object defaultKeyGenerator = FieldValueUtil.getFieldValue(shardingRule, "defaultKeyGenerator");
        assertNotNull(defaultKeyGenerator);
        assertTrue(defaultKeyGenerator instanceof IncrementKeyGenerator);
        Object tableRules = FieldValueUtil.getFieldValue(shardingRule, "tableRules");
        assertNotNull(tableRules);
        assertThat(((Collection<TableRule>) tableRules).size(), is(2));
        Iterator<TableRule> tableRuleIterator = ((Collection<TableRule>) tableRules).iterator();
        TableRule orderRule = tableRuleIterator.next();
        assertThat(orderRule.getGenerateKeyColumn(), is("order_id"));
        TableRule orderItemRule = tableRuleIterator.next();
        assertThat(orderItemRule.getGenerateKeyColumn(), is("order_item_id"));
        assertTrue(orderItemRule.getKeyGenerator() instanceof DecrementKeyGenerator);
    }
}
