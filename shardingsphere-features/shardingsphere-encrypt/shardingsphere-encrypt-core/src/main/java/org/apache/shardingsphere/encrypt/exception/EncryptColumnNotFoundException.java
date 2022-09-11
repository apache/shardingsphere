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

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Encrypt column not found exception.
 */
public final class EncryptColumnNotFoundException extends EncryptSQLException {
    
    private static final long serialVersionUID = -8481487069938556675L;
    
    public EncryptColumnNotFoundException(final String tableName, final String columnName) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 1, "Fail to find encrypt column `%s` from table `%s`", columnName, tableName);
    }
}
