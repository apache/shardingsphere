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

package org.apache.shardingsphere.agent.core.builder;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentJunctionTest {
    
    @Test
    void assertMatchesWithExistingTarget() {
        TypeDescription matched = mock(TypeDescription.class);
        when(matched.getTypeName()).thenReturn("matched");
        assertTrue(new AgentJunction(Collections.singletonMap("matched", mock(AdvisorConfiguration.class))).matches(matched));
    }
    
    @Test
    void assertMatchesWithoutTarget() {
        TypeDescription unmatched = mock(TypeDescription.class);
        when(unmatched.getTypeName()).thenReturn("unmatched");
        assertFalse(new AgentJunction(Collections.emptyMap()).matches(unmatched));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertAndAndOrReturnNull() {
        AgentJunction junction = new AgentJunction(Collections.emptyMap());
        ElementMatcher<TypeDescription> matcher = mock(ElementMatcher.class);
        assertNull(junction.and(matcher));
        assertNull(junction.or(matcher));
    }
}
