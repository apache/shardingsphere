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

package org.apache.shardingsphere.shardingjdbc.spring.boot.type;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.EncryptDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootEncryptTest.class)
@SpringBootApplication
@ActiveProfiles("encrypt")
public class SpringBootEncryptTest {
    
    @Resource
    private DataSource dataSource;
    
    @Test
    public void assertSqlShow() {
        assertTrue(((EncryptDataSource) dataSource).getShardingProperties().<Boolean>getValue(ShardingPropertiesConstant.SQL_SHOW));
    }
    
    @Test
    public void assertWithEncryptDataSource() {
        assertTrue(dataSource instanceof EncryptDataSource);
        BasicDataSource basicDataSource = (BasicDataSource) ((EncryptDataSource) dataSource).getDataSource();
        assertThat(basicDataSource.getMaxTotal(), is(100));
    }
    
    @Test
    public void assertWithEncryptRule() {
        EncryptRule encryptRule = ((EncryptDataSource) dataSource).getEncryptRule();
        assertThat(encryptRule.getEncryptTableNames().size(), is(1));
        assertTrue(encryptRule.getEncryptEngine().getShardingEncryptor("t_order", "user_id").isPresent());
    }
}
