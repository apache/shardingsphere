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

package org.apache.shardingsphere.infra.distsql.exception.rule;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

import java.util.Collection;

/**
 * Missing required rule exception.
 */
public final class MissingRequiredRuleException extends RuleDefinitionViolationException {
    
    private static final long serialVersionUID = -8464574460917965546L;
    
    public MissingRequiredRuleException(final String ruleType) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 101, String.format("%s rule does not exist", ruleType));
    }
    
    public MissingRequiredRuleException(final String ruleType, final String databaseName) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 101, String.format("%s rule does not exist in database `%s`", ruleType, databaseName));
    }
    
    public MissingRequiredRuleException(final String ruleType, final Collection<String> ruleNames) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 101, String.format("%s rules `%s` do not exist", ruleType, ruleNames));
    }
    
    public MissingRequiredRuleException(final String ruleType, final String databaseName, final String ruleName) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 101, String.format("%s rule `%s` do not exist in database `%s`", ruleType, ruleName, databaseName));
    }
    
    public MissingRequiredRuleException(final String ruleType, final String databaseName, final Collection<String> ruleNames) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 101, String.format("%s rules `%s` do not exist in database `%s`", ruleType, ruleNames, databaseName));
    }
}
