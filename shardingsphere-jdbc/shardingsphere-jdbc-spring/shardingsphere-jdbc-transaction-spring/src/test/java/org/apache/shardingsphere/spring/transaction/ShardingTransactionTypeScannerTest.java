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

package org.apache.shardingsphere.spring.transaction;

import org.apache.shardingsphere.spring.transaction.fixture.FixtureSpringConfiguration;
import org.apache.shardingsphere.spring.transaction.fixture.MockService;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = FixtureSpringConfiguration.class)
public class ShardingTransactionTypeScannerTest {
    
    @Autowired
    private MockService mockService;
    
    @Test
    public void assertShardingTransaction() {
        assertThat(mockService.executeLocal(), is(TransactionType.LOCAL));
        assertThat(mockService.executeBase(), is(TransactionType.BASE));
        assertThat(mockService.execute(), is(TransactionType.XA));
    }
    
    @Test
    public void assertShardingTransactionType() {
        TransactionType preTransactionType = TransactionTypeHolder.get();
        mockService.executeLocal();
        assertThat(TransactionTypeHolder.get(), is(preTransactionType));
        mockService.executeBase();
        assertThat(TransactionTypeHolder.get(), is(preTransactionType));
        mockService.execute();
        assertThat(TransactionTypeHolder.get(), is(preTransactionType));
    }
}
