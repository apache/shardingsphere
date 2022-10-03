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

package org.apache.shardingsphere.transaction.rule.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

import java.util.Properties;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.constant.TransactionOrder;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;

public final class DefaultTransactionRuleConfigurationBuilderTest {
    
    @Test
    public void assertBuild() {
        TransactionRuleConfiguration actual = new DefaultTransactionRuleConfigurationBuilder().build();
        assertThat(actual.getDefaultType(), is(TransactionType.LOCAL.name()));
        assertNull(actual.getProviderType());
        assertThat(actual.getProps(), is(new Properties()));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(new DefaultTransactionRuleConfigurationBuilder().getOrder(), is(TransactionOrder.ORDER));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(new DefaultTransactionRuleConfigurationBuilder().getTypeClass().toString(), is(TransactionRuleBuilder.class.toString()));
    }
}
