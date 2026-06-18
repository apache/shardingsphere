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

package org.apache.shardingsphere.infra.exception.kernel.metadata.rule;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

import java.util.Collection;

/**
 * Duplicate rule exception.
 */
public final class DuplicateRuleException extends RuleDefinitionException {
    
    private static final long serialVersionUID = -1738699538105858939L;
    
    public DuplicateRuleException(final String ruleType, final String databaseName, final Collection<String> ruleNames) {
        super(XOpenSQLState.DUPLICATE, 4, "Duplicate %s rule names '%s' in database '%s'.", ruleType, ruleNames, databaseName);
    }
    
    public DuplicateRuleException(final String ruleType, final Collection<String> ruleNames) {
        super(XOpenSQLState.DUPLICATE, 4, "Duplicate %s rule names '%s'.", ruleType, ruleNames);
    }
    
    public DuplicateRuleException(final String type, final String databaseName) {
        super(XOpenSQLState.DUPLICATE, 4, "Duplicate '%s' in database '%s'.", type, databaseName);
    }
}
