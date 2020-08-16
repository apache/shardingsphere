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

package org.apache.shardingsphere.spring;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.spring.fixture.IncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.spring.util.FieldValueUtil;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/withNamespaceGenerateKeyColumns.xml")
public class GenerateKeyJUnitTest extends AbstractSpringJUnitTest {
    
    @Resource
    private ShardingSphereDataSource shardingSphereDataSource;
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertGenerateKeyColumn() {
        SchemaContexts schemaContexts = shardingSphereDataSource.getSchemaContexts();
        assertNotNull(schemaContexts);
        ShardingRule shardingRule = (ShardingRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
        assertNotNull(shardingRule);
        KeyGenerateAlgorithm defaultKeyGenerateAlgorithm = shardingRule.getDefaultKeyGenerateAlgorithm();
        assertNotNull(defaultKeyGenerateAlgorithm);
        assertTrue(defaultKeyGenerateAlgorithm instanceof IncrementKeyGenerateAlgorithm);
        Object tableRules = FieldValueUtil.getFieldValue(shardingRule, "tableRules");
        assertNotNull(tableRules);
        assertThat(((Collection<TableRule>) tableRules).size(), is(2));
        Iterator<TableRule> tableRuleIterator = ((Collection<TableRule>) tableRules).iterator();
        TableRule orderRule = tableRuleIterator.next();
        assertTrue(orderRule.getGenerateKeyColumn().isPresent());
        assertThat(orderRule.getGenerateKeyColumn().get(), is("order_id"));
        TableRule orderItemRule = tableRuleIterator.next();
        assertTrue(orderItemRule.getGenerateKeyColumn().isPresent());
        assertThat(orderItemRule.getGenerateKeyColumn().get(), is("order_item_id"));
        assertThat(orderItemRule.getKeyGeneratorName(), is("decrementAlgorithm"));
    }
}
