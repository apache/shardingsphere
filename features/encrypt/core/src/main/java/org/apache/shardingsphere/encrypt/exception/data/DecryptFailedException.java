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

package org.apache.shardingsphere.encrypt.exception.data;

import org.apache.shardingsphere.encrypt.exception.EncryptSQLException;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Decrypt failed exception.
 */
public final class DecryptFailedException extends EncryptSQLException {
    
    private static final long serialVersionUID = 1122241238288845667L;
    
    public DecryptFailedException(final String ciphertext, final SQLExceptionIdentifier sqlExceptionIdentifier, final Exception cause) {
        super(XOpenSQLState.DATA_EXCEPTION, 30, cause, "Failed to decrypt the ciphertext '%s' in '%s'.", ciphertext, sqlExceptionIdentifier);
    }
}
