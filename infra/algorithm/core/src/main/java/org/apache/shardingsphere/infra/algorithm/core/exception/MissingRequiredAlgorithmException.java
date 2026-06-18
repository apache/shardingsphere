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

package org.apache.shardingsphere.infra.algorithm.core.exception;

import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Missing required algorithm exception.
 */
public final class MissingRequiredAlgorithmException extends AlgorithmDefinitionException {
    
    private static final long serialVersionUID = 8128067899672436211L;
    
    public MissingRequiredAlgorithmException(final String algorithmType, final SQLExceptionIdentifier sqlExceptionIdentifier) {
        super(XOpenSQLState.NOT_FOUND, 1, "'%s' algorithm on %s is required.", algorithmType, sqlExceptionIdentifier);
    }
}
