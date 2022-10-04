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

package org.apache.shardingsphere.shadow.spring.boot.condition;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShadowSpringBootConditionTest {
    
    @Test
    public void assertNotMatch() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.rules.encrypt.encryptors.aes_encryptor.type", "AES");
        ConditionContext context = mock(ConditionContext.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(context.getEnvironment()).thenReturn(mockEnvironment);
        ShadowSpringBootCondition condition = new ShadowSpringBootCondition();
        ConditionOutcome matchOutcome = condition.getMatchOutcome(context, metadata);
        assertFalse(matchOutcome.isMatch());
    }
    
    @Test
    public void assertMatch() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.rules.shadow.column", "user_id");
        ConditionContext context = mock(ConditionContext.class);
        AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);
        when(context.getEnvironment()).thenReturn(mockEnvironment);
        ShadowSpringBootCondition condition = new ShadowSpringBootCondition();
        ConditionOutcome matchOutcome = condition.getMatchOutcome(context, metadata);
        assertTrue(matchOutcome.isMatch());
    }
}
