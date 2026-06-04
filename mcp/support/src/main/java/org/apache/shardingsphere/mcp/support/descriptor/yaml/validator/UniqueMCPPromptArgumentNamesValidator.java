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

import org.apache.shardingsphere.mcp.support.descriptor.yaml.YamlMCPPromptArgumentDescriptor;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Unique MCP prompt argument names validator.
 */
public final class UniqueMCPPromptArgumentNamesValidator implements ConstraintValidator<UniqueMCPPromptArgumentNames, Collection<YamlMCPPromptArgumentDescriptor>> {
    
    @Override
    public boolean isValid(final Collection<YamlMCPPromptArgumentDescriptor> value, final ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }
        Set<String> names = new HashSet<>(value.size(), 1F);
        for (YamlMCPPromptArgumentDescriptor each : value) {
            if (null != each && null != each.getName() && !each.getName().isBlank() && !names.add(each.getName())) {
                return false;
            }
        }
        return true;
    }
}
