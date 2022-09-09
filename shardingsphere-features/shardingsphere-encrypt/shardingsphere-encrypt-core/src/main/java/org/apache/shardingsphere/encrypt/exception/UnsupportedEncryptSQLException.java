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

package org.apache.shardingsphere.encrypt.exception;

import org.apache.shardingsphere.infra.util.exception.external.sql.type.feature.FeatureSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Unsupported encrypt SQL exception.
 */
public final class UnsupportedEncryptSQLException extends FeatureSQLException {
    
    private static final long serialVersionUID = 8391663664617661217L;
    
    public UnsupportedEncryptSQLException(final String sqlClause) {
        super(XOpenSQLState.SYNTAX_ERROR, 7001, "The SQL clause `%s` is unsupported in encrypt rule", sqlClause);
    }
}
