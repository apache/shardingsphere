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

package org.apache.shardingsphere.mask.rule.changed;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropNamedRuleItemEvent;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.metadata.nodepath.MaskRuleNodePathProvider;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MaskTableChangedProcessorTest {


    @Test
    void assertSwapRuleItemConfiguration() {
        AlterRuleItemEvent event = mock(AlterRuleItemEvent.class);
        MaskTableChangedProcessor processor = new MaskTableChangedProcessor();
        MaskTableRuleConfiguration maskTableRuleConfiguration = processor.swapRuleItemConfiguration(event, "name: test_table");
        assertThat(maskTableRuleConfiguration.getName(), is("test_table"));
    }

    @Test
    void assertFindRuleConfiguration() {
        MaskTableChangedProcessor processor = new MaskTableChangedProcessor();
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap());
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mock(ShardingSphereRule.class)));
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, ruleMetaData, Collections.emptyMap());
        MaskRuleConfiguration maskRuleConfiguration = processor.findRuleConfiguration(database);
        assertThat(maskRuleConfiguration.getTables().size(), is(0));
    }


    @Test
    void assertDropRuleItemConfiguration() {
        MaskTableChangedProcessor processor = new MaskTableChangedProcessor();
        DropNamedRuleItemEvent event = mock(DropNamedRuleItemEvent.class);
        MaskRuleConfiguration currentRuleConfig = new MaskRuleConfiguration(Collections.emptyList(), Collections.singletonMap("name: test_table", mock(AlgorithmConfiguration.class)));
        processor.dropRuleItemConfiguration(event, currentRuleConfig);
    }

    @Test
    void assertGetType() {
        MaskTableChangedProcessor processor = new MaskTableChangedProcessor();
        String result = processor.getType();
        assertEquals(MaskRuleNodePathProvider.RULE_TYPE + "." + MaskRuleNodePathProvider.TABLES, result);
    }
}
