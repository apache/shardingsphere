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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.Map;

/**
 * MCP prompt guidance metadata validator.
 */
public final class MCPPromptGuidanceMetaValidator implements ConstraintValidator<MCPPromptGuidanceMeta, Map<String, Object>> {
    
    @Override
    public boolean isValid(final Map<String, Object> value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        if (!isNonEmptyCollection(value.get(MCPShardingSphereMetadataKeys.STOP_CONDITIONS))) {
            addViolation(context, MCPShardingSphereMetadataKeys.STOP_CONDITIONS);
            return false;
        }
        if (!isNonEmptyCollection(value.get(MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS))) {
            addViolation(context, MCPShardingSphereMetadataKeys.ASK_USER_CONDITIONS);
            return false;
        }
        return true;
    }
    
    private void addViolation(final ConstraintValidatorContext context, final String key) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("must declare " + key).addConstraintViolation();
    }
    
    private boolean isNonEmptyCollection(final Object value) {
        return value instanceof Collection && !((Collection<?>) value).isEmpty();
    }
}
