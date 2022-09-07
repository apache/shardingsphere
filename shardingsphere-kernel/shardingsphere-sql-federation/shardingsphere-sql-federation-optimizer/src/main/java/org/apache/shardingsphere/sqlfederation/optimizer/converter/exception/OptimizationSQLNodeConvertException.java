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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.exception;

import org.apache.shardingsphere.infra.util.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * Optimization SQL node convert exception.
 */
public final class OptimizationSQLNodeConvertException extends ShardingSphereSQLException {
    
    private static final long serialVersionUID = -5486229929620713984L;
    
    public OptimizationSQLNodeConvertException(final SQLStatement statement) {
        super(XOpenSQLState.SYNTAX_ERROR, 10003, "Unsupported SQL node conversion for SQL statement `%s`", statement.toString());
    }
}
