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

package org.apache.shardingsphere.distsql.handler.exception.datasource;

import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.DistSQLException;
import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.SQLState;

/**
 * Data source definition violation exception.
 */
public abstract class DataSourceDefinitionViolationException extends DistSQLException {
    
    private static final long serialVersionUID = -3318829232811230364L;
    
    protected DataSourceDefinitionViolationException(final SQLState sqlState, final int errorCode, final String reason, final Object... messageArgs) {
        super(sqlState, errorCode, reason, messageArgs);
    }
}
