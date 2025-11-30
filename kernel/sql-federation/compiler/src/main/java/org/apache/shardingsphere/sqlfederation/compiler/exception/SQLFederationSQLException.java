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

package org.apache.shardingsphere.sqlfederation.compiler.exception;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.exception.external.sql.type.feature.FeatureSQLException;

/**
 * SQL federation SQL exception.
 */
public abstract class SQLFederationSQLException extends FeatureSQLException {
    
    private static final long serialVersionUID = 4689889693356895996L;
    
    private static final int FEATURE_CODE = 1;
    
    protected SQLFederationSQLException(final SQLState sqlState, final int errorCode, final String reason, final Object... messageArgs) {
        super(sqlState, FEATURE_CODE, errorCode, reason, messageArgs);
    }
    
    protected SQLFederationSQLException(final SQLState sqlState, final int errorCode, final Exception cause, final String reason, final Object... messageArgs) {
        super(sqlState, FEATURE_CODE, errorCode, cause, reason, messageArgs);
    }
}
