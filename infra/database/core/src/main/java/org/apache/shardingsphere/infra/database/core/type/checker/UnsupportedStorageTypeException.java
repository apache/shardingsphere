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

package org.apache.shardingsphere.infra.database.core.type.checker;

import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.kernel.KernelSQLException;

/**
 * Unsupported storage type exception.
 */
public final class UnsupportedStorageTypeException extends KernelSQLException {
    
    private static final long serialVersionUID = 8981789100727786183L;
    
    public UnsupportedStorageTypeException(final String dataSourceName) {
        super(XOpenSQLState.FEATURE_NOT_SUPPORTED, 3, 40, "Unsupported storage type of `%s`.", dataSourceName);
    }
    
    public UnsupportedStorageTypeException(final String databaseName, final String dataSourceName) {
        super(XOpenSQLState.FEATURE_NOT_SUPPORTED, 3, 40, "Unsupported storage type of `%s.%s`.", databaseName, dataSourceName);
    }
}
