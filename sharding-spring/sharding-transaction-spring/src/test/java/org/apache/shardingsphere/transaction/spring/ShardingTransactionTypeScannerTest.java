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

package org.apache.shardingsphere.transaction.spring;

import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spring.fixture.MockService;
import org.apache.shardingsphere.transaction.spring.fixture.FixtureSpringConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = FixtureSpringConfiguration.class)
public class ShardingTransactionTypeScannerTest {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private MockService mockService;
    
    @Before
    public void setUp() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS t_order");
        jdbcTemplate.execute("CREATE TABLE t_order (order_id BIGINT AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))");
    }
    
    @Test
    public void assertShardingTransaction() {
        assertThat(mockService.executeXA(), is(TransactionType.XA));
        assertThat(mockService.executeLocal(), is(TransactionType.LOCAL));
        assertThat(mockService.execute(), is(TransactionType.LOCAL));
    }
}
