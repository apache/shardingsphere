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

package org.apache.shardingsphere.infra.executor.check;

import org.apache.shardingsphere.infra.executor.check.fixture.SQLCheckerFixture;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.test.fixture.rule.MockedRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class SQLCheckerFactoryTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstance() {
        MockedRule rule = mock(MockedRule.class);
        Map<ShardingSphereRule, SQLChecker> actual = SQLCheckerFactory.getInstance(Collections.singleton(rule));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(rule), instanceOf(SQLCheckerFixture.class));
    }
}
