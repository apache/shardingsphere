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

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.external.sql.type.kernel.category.ClusterSQLException;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * ShardingSphere state exception.
 */
public final class ShardingSphereStateException extends ClusterSQLException {
    
    private static final long serialVersionUID = 3834132923835083492L;
    
    public ShardingSphereStateException(final ShardingSphereState state, final SQLStatement sqlStatement) {
        super(XOpenSQLState.GENERAL_ERROR, 20, "The cluster state is %s, can not support SQL statement '%s'.", state, sqlStatement.getClass().getSimpleName());
    }
}
