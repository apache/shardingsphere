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

package org.apache.shardingsphere.mode.event;

import org.apache.shardingsphere.infra.rule.event.rule.RuleItemChangedEvent;
import org.apache.shardingsphere.infra.rule.event.rule.alter.AlterUniqueRuleItemEvent;
import org.apache.shardingsphere.infra.rule.event.rule.drop.DropUniqueRuleItemEvent;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class UniqueRuleItemChangedEventCreatorTest {
    
    private final UniqueRuleItemChangedEventCreator eventCreator = new UniqueRuleItemChangedEventCreator();
    
    @Test
    void assertCreateWithEventTypeAdded() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("test_key", "test_value", DataChangedEvent.Type.ADDED);
        RuleItemChangedEvent ruleItemChangedEvent = eventCreator.create("test_db", dataChangedEvent, "test_type_added");
        assertThat(ruleItemChangedEvent, instanceOf(AlterUniqueRuleItemEvent.class));
        assertThat(ruleItemChangedEvent.getType(), is("test_type_added"));
    }
    
    @Test
    void assertCreateWithEventTypeDeleted() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("test_key", "test_value", DataChangedEvent.Type.DELETED);
        RuleItemChangedEvent ruleItemChangedEvent = eventCreator.create("test_db", dataChangedEvent, "test_type_deleted");
        assertThat(ruleItemChangedEvent, instanceOf(DropUniqueRuleItemEvent.class));
        assertThat(ruleItemChangedEvent.getType(), is("test_type_deleted"));
    }
}
