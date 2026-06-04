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

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * MCP description validator.
 */
public final class MCPDescriptionValidator implements ConstraintValidator<MCPDescription, String> {
    
    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        if (null == value || value.isBlank()) {
            return false;
        }
        if (value.startsWith("ShardingSphere MCP resource:") || value.startsWith("ShardingSphere MCP resource template:")) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("must not be a placeholder description").addConstraintViolation();
            return false;
        }
        return true;
    }
}
