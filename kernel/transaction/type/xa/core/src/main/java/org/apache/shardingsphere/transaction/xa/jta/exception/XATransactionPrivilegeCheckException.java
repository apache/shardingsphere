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

package org.apache.shardingsphere.transaction.xa.jta.exception;

import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;

import java.sql.SQLException;

/**
 * XA transaction privilege check exception.
 */
public final class XATransactionPrivilegeCheckException extends XATransactionSQLException {
    
    private static final long serialVersionUID = 6073175429050058508L;
    
    public XATransactionPrivilegeCheckException(final String privilege) {
        super(XOpenSQLState.INVALID_TRANSACTION_STATE, 2, "Check XA transaction privileges failed on data source, please grant '%s' to current user.", privilege);
    }
    
    public XATransactionPrivilegeCheckException(final String privilege, final SQLException cause) {
        super(XOpenSQLState.INVALID_TRANSACTION_STATE, 2, String.format("Check XA transaction privileges failed on data source, please grant '%s' to current user.", privilege), cause);
    }
}
