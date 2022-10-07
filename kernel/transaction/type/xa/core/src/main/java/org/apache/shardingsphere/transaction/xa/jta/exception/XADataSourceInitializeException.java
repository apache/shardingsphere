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

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.transaction.exception.TransactionSQLException;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;

/**
 * XA data source initialize exception.
 */
public final class XADataSourceInitializeException extends TransactionSQLException {
    
    private static final long serialVersionUID = -4515239569528215614L;
    
    public XADataSourceInitializeException(final XADataSourceDefinition xaDataSourceDefinition) {
        super(XOpenSQLState.INVALID_TRANSACTION_STATE, 201, "Failed to create `%s` XA data source.", xaDataSourceDefinition.getType());
    }
}
