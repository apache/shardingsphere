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

import java.util.Collection;

/**
 * Auditor in used exception.
 */
public final class AuditorInUsedException extends RuleDefinitionViolationException {
    
    private static final long serialVersionUID = 367366381929736622L;
    
    public AuditorInUsedException(final String ruleType, final String databaseName, final Collection<String> auditorNames) {
        super(1128, String.format("%s auditor `%s` in database `%s` are still in used.", ruleType, auditorNames, databaseName));
    }
}
