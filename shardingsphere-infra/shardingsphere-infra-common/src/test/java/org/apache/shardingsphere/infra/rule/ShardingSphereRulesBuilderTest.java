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

package org.apache.shardingsphere.infra.rule;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.builder.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.infra.rule.fixture.TestRuleConfiguration;
import org.apache.shardingsphere.infra.rule.fixture.TestShardingSphereRule;
import org.apache.shardingsphere.infra.rule.single.SingleTableRule;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingSphereRulesBuilderTest {
    
    @Test
    public void assertBuild() {
        Collection<ShardingSphereRule> shardingSphereRules = ShardingSphereRulesBuilder.buildSchemaRules(
                "schema_name", Lists.newArrayList(new TestRuleConfiguration()), mock(DatabaseType.class), Collections.emptyMap());
        assertThat(shardingSphereRules.size(), is(2));
        Iterator<ShardingSphereRule> iterator = shardingSphereRules.iterator();
        assertThat(iterator.next(), instanceOf(TestShardingSphereRule.class));
        assertThat(iterator.next(), instanceOf(SingleTableRule.class));
    }
}
