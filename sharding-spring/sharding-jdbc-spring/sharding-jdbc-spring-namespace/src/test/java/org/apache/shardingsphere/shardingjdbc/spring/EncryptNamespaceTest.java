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

package org.apache.shardingsphere.shardingjdbc.spring;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.impl.AESShardingEncryptor;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/encryptNamespace.xml")
public class EncryptNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertEncryptDataSource() {
        EncryptRule encryptRule = getEncryptRuleRule();
        assertTrue(encryptRule.getShardingEncryptor("t_order", "user_id").isPresent());
        assertThat(encryptRule.getCipherColumn("t_order", "user_id"), is("user_encrypt"));
        assertTrue(encryptRule.getShardingEncryptor("t_order", "user_id").get() instanceof AESShardingEncryptor);
        assertThat(encryptRule.getPlainColumn("t_order", "order_id"), is(Optional.of("order_decrypt")));
        assertTrue(getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW));
        assertTrue(getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.QUERY_WITH_CIPHER_COLUMN));
    }
    
    private EncryptRule getEncryptRuleRule() {
        EncryptDataSource encryptDataSource = applicationContext.getBean("encryptDataSource", EncryptDataSource.class);
        return encryptDataSource.getRuntimeContext().getRule();
    }
    
    private ShardingProperties getShardingProperties() {
        EncryptDataSource encryptDataSource = applicationContext.getBean("encryptDataSource", EncryptDataSource.class);
        return encryptDataSource.getRuntimeContext().getProps();
    }
}
