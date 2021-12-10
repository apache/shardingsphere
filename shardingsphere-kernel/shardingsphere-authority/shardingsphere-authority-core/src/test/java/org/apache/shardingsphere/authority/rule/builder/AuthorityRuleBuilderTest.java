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

package org.apache.shardingsphere.authority.rule.builder;

import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AuthorityRuleBuilderTest {
    
    private final AuthorityRuleBuilder ruleBuilder = new AuthorityRuleBuilder();
    
    @Test
    public void getOrderTest() {
        int order = ruleBuilder.getOrder();
        assertThat(order, is(500));
    }
    
    @Test
    public void getTypeClassTest() {
        assertThat(AuthorityRuleConfiguration.class, equalTo(ruleBuilder.getTypeClass()));
    }
}
