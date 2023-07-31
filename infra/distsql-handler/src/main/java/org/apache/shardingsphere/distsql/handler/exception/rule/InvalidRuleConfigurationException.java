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

package org.apache.shardingsphere.distsql.handler.exception.rule;

import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;

import java.util.Collection;

/**
 * Invalid rule configuration exception.
 */
public final class InvalidRuleConfigurationException extends RuleDefinitionViolationException {
    
    private static final long serialVersionUID = 6085010920008859376L;
    
    public InvalidRuleConfigurationException(final String ruleType, final String rule, final String errorMessage) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 100, "Invalid `%s` rule `%s`, error message is: %s", ruleType, rule, errorMessage);
    }
    
    public InvalidRuleConfigurationException(final String ruleType, final Collection<String> rules, final Collection<String> errorMessages) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 100, "Invalid `%s` rules `%s`, error messages are: %s", ruleType, rules, errorMessages);
    }
    
    public InvalidRuleConfigurationException(final String ruleType, final String errorMessage) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 100, "Invalid `%s` rule, error message is: %s", ruleType, errorMessage);
    }
}
