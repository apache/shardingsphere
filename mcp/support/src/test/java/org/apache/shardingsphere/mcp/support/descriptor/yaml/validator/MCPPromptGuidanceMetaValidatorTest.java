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

package org.apache.shardingsphere.mcp.support.descriptor.yaml.validator;

import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPPromptGuidanceMetaValidatorTest {
    
    @Test
    void assertIsValidWithoutMetadata() {
        assertTrue(new MCPPromptGuidanceMetaValidator().isValid(null, mock(ConstraintValidatorContext.class)));
    }
    
    @Test
    void assertIsValidWithGuidance() {
        assertTrue(new MCPPromptGuidanceMetaValidator().isValid(Map.of(MCPShardingSphereMetadataKeys.STOP_CONDITIONS, List.of("stop"),
                MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS, List.of("ask")), mock(ConstraintValidatorContext.class)));
    }
    
    @Test
    void assertIsValidWithoutStopConditions() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate("must declare " + MCPShardingSphereMetadataKeys.STOP_CONDITIONS)).thenReturn(builder);
        assertFalse(new MCPPromptGuidanceMetaValidator().isValid(Map.of(MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS, List.of("ask")), context));
        verify(builder).addConstraintViolation();
    }
    
    @Test
    void assertIsValidWithEmptyAskUserConditions() {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate("must declare " + MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS)).thenReturn(builder);
        assertFalse(new MCPPromptGuidanceMetaValidator().isValid(Map.of(MCPShardingSphereMetadataKeys.STOP_CONDITIONS, List.of("stop"),
                MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS, List.of()), context));
        verify(context).disableDefaultConstraintViolation();
        verify(builder).addConstraintViolation();
    }
}
