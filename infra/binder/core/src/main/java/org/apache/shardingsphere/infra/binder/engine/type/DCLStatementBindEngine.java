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

package org.apache.shardingsphere.infra.binder.engine.type;

import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dcl.RevokeStatementBinder;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;

/**
 * DCL statement bind engine.
 */
public final class DCLStatementBindEngine {
    
    /**
     * Bind DCL statement.
     *
     * @param statement to be bound DCL statement
     * @param binderContext binder context
     * @return bound DCL statement
     */
    public DCLStatement bind(final DCLStatement statement, final SQLStatementBinderContext binderContext) {
        if (statement instanceof RevokeStatement) {
            return new RevokeStatementBinder().bind((RevokeStatement) statement, binderContext);
        }
        return statement;
    }
}
