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

package org.apache.shardingsphere.encrypt.exception.metadata;

import org.apache.shardingsphere.encrypt.exception.EncryptSQLException;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Mismatched encrypt algorithm type exception.
 */
public final class MismatchedEncryptAlgorithmTypeException extends EncryptSQLException {
    
    private static final long serialVersionUID = 4258928279099223870L;
    
    public MismatchedEncryptAlgorithmTypeException(final String databaseName, final String columnType, final String encryptorName, final String algorithmFeature) {
        super(XOpenSQLState.GENERAL_ERROR, 4, "'%s' column's encrypt algorithm '%s' should support %s in database '%s'.", columnType, encryptorName, algorithmFeature, databaseName);
    }
}
