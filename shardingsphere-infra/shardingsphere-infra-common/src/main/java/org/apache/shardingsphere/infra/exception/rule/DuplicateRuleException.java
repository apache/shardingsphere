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

package org.apache.shardingsphere.infra.exception.rule;

import java.util.Collection;

/**
 * Duplicate rule exception.
 */
public final class DuplicateRuleException extends RuleDefinitionViolationException {
    
    private static final long serialVersionUID = -1738699538105858939L;
    
    public DuplicateRuleException(final String ruleType, final String schemaName, final Collection<String> ruleNames) {
        super(1105, String.format("Duplicate %s rule names `%s` in schema `%s`", ruleType, ruleNames, schemaName));
    }
}
