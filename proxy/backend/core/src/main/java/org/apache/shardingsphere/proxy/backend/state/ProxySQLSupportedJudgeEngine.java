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

package org.apache.shardingsphere.proxy.backend.state;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Proxy SQL supported judge engine.
 */
@RequiredArgsConstructor
public final class ProxySQLSupportedJudgeEngine {
    
    private final Collection<Class<? extends SQLStatement>> supportedStandardSQLStatementTypes;
    
    private final Collection<Class<? extends SQLStatement>> supportedDialectSQLStatementTypes;
    
    private final Collection<Class<? extends SQLStatement>> unsupportedStandardSQLStatementTypes;
    
    private final Collection<Class<? extends SQLStatement>> unsupportedDialectSQLStatementTypes;
    
    /**
     * Judge whether to support SQL.
     *
     * @param sqlStatement SQL statement to be judged
     * @return supported or not
     */
    public boolean isSupported(final SQLStatement sqlStatement) {
        for (Class<? extends SQLStatement> each : combineSQLStatementTypes(supportedStandardSQLStatementTypes, supportedDialectSQLStatementTypes)) {
            if (each.isAssignableFrom(sqlStatement.getClass())) {
                return true;
            }
        }
        for (Class<? extends SQLStatement> each : combineSQLStatementTypes(unsupportedStandardSQLStatementTypes, unsupportedDialectSQLStatementTypes)) {
            if (each.isAssignableFrom(sqlStatement.getClass())) {
                return false;
            }
        }
        return true;
    }
    
    private Collection<Class<? extends SQLStatement>> combineSQLStatementTypes(final Collection<Class<? extends SQLStatement>> standardSQLStatementTypes,
                                                                               final Collection<Class<? extends SQLStatement>> dialectSQLStatementTypes) {
        if (dialectSQLStatementTypes.isEmpty()) {
            return standardSQLStatementTypes;
        }
        Collection<Class<? extends SQLStatement>> result = new LinkedList<>(standardSQLStatementTypes);
        result.addAll(dialectSQLStatementTypes);
        return result;
    }
}
