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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedSchemaMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.fixture.loader.CommonFixtureSchemaMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RuleBasedSchemaMetaDataBuilderFactoryTest {
    
    @SuppressWarnings("rawtypes")
    @Test
    public void assertGetInstances() {
        CommonFixtureRule rule = new CommonFixtureRule();
        Map<ShardingSphereRule, RuleBasedSchemaMetaDataBuilder> actual = RuleBasedSchemaMetaDataBuilderFactory.getInstances(Collections.singleton(rule));
        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertTrue(actual.containsKey(rule));
        assertThat(actual.get(rule), instanceOf(CommonFixtureSchemaMetaDataBuilder.class));
    }
}
