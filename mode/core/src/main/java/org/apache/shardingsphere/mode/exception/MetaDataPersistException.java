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

package org.apache.shardingsphere.mode.exception;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.category.MetaDataSQLException;

/**
 * Meta data persist exception.
 */
public abstract class MetaDataPersistException extends MetaDataSQLException {
    
    private static final long serialVersionUID = 8997644844343785667L;
    
    protected MetaDataPersistException(final SQLState sqlState, final int errorCode, final Exception cause, final String reason, final Object... messageArgs) {
        super(sqlState, errorCode, cause, reason, messageArgs);
    }
}
