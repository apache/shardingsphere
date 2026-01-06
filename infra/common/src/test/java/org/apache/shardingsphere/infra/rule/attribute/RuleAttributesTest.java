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

package org.apache.shardingsphere.infra.rule.attribute;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RuleAttributesTest {
    
    @Test
    void assertFindsAttribute() {
        RuleAttribute attribute = mock(RuleAttribute.class);
        RuleAttributes attributes = new RuleAttributes(attribute);
        assertTrue(attributes.findAttribute(RuleAttribute.class).isPresent());
    }
    
    @Test
    void assertEmptyWhenAttributeMissing() {
        RuleAttributes attributes = new RuleAttributes();
        Optional<RuleAttribute> actual = attributes.findAttribute(RuleAttribute.class);
        assertFalse(actual.isPresent());
    }
    
    @Test
    void assertGetsAttribute() {
        RuleAttribute attribute = mock(RuleAttribute.class);
        RuleAttributes attributes = new RuleAttributes(attribute);
        RuleAttribute actual = attributes.getAttribute(RuleAttribute.class);
        assertThat(actual, instanceOf(RuleAttribute.class));
    }
    
    @Test
    void assertThrowsWhenAttributeMissing() {
        RuleAttributes attributes = new RuleAttributes();
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> attributes.getAttribute(RuleAttribute.class));
        assertThat(actual.getMessage(), is("Can not find rule attribute: interface org.apache.shardingsphere.infra.rule.attribute.RuleAttribute"));
    }
}
