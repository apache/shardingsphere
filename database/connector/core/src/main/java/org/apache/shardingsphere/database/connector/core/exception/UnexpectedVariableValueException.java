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

package org.apache.shardingsphere.database.connector.core.exception;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.category.MetaDataSQLException;

/**
 * Unexpected variable value exception.
 */
public final class UnexpectedVariableValueException extends MetaDataSQLException {
    
    private static final long serialVersionUID = -8481467708967951766L;
    
    public UnexpectedVariableValueException(final String variableName, final String expectedValue, final String actualValue) {
        super(XOpenSQLState.GENERAL_ERROR, 8, "Unexpected variable value of '%s', required '%s', now is '%s'.", variableName, expectedValue, actualValue);
    }
}
