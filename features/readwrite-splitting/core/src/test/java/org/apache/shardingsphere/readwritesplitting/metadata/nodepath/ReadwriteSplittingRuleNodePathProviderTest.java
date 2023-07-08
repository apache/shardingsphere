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

package org.apache.shardingsphere.readwritesplitting.metadata.nodepath;

import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RuleNodePathProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReadwriteSplittingRuleNodePathProviderTest {
    
    @Test
    void assertReadwriteSplittingRuleNodePath() {
        RuleNodePathProvider ruleNodePathProvider = new ReadwriteSplittingRuleNodePathProvider();
        RuleNodePath actualRuleNodePath = ruleNodePathProvider.getRuleNodePath();
        assertEquals(2, actualRuleNodePath.getNamedItems().size());
        assertTrue(actualRuleNodePath.getNamedItems().containsKey(ReadwriteSplittingRuleNodePathProvider.DATA_SOURCES));
        assertTrue(actualRuleNodePath.getNamedItems().containsKey(ReadwriteSplittingRuleNodePathProvider.LOAD_BALANCERS));
        assertTrue(actualRuleNodePath.getUniqueItems().isEmpty());
        assertNotNull(actualRuleNodePath.getRoot());
        assertEquals(ReadwriteSplittingRuleNodePathProvider.RULE_TYPE, actualRuleNodePath.getRoot().getRuleType());
    }
}
