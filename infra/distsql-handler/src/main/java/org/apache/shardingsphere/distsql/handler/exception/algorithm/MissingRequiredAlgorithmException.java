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

package org.apache.shardingsphere.distsql.handler.exception.algorithm;

import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;

import java.util.Collection;

/**
 * Missing required algorithm exception.
 */
public final class MissingRequiredAlgorithmException extends AlgorithmDefinitionViolationException {
    
    private static final long serialVersionUID = 4591071898233749618L;
    
    public MissingRequiredAlgorithmException() {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 151, "Algorithm does not exist.");
    }
    
    public MissingRequiredAlgorithmException(final String type, final String databaseName) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 151, String.format("%s algorithm does not exist in database `%s`.", type, databaseName));
    }
    
    public MissingRequiredAlgorithmException(final String type, final Collection<String> algorithmNames) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 151, String.format("%s algorithms `%s` do not exist.", type, algorithmNames));
    }
    
    public MissingRequiredAlgorithmException(final String type, final String databaseName, final Collection<String> algorithmNames) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 151, String.format("%s algorithms `%s` do not exist in database `%s`.", type, algorithmNames, databaseName));
    }
}
