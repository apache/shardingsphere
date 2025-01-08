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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.binder.engine.statement.dcl.DenyUserStatementBinder;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dcl.DenyUserStatement;

/**
 * DCL statement bind engine.
 */
@RequiredArgsConstructor
public final class DCLStatementBindEngine {
    
    private final ShardingSphereMetaData metaData;
    
    private final String currentDatabaseName;
    
    private final HintValueContext hintValueContext;
    
    /**
     * Bind DCL statement.
     *
     * @param statement to be bound DCL statement
     * @return bound DCL statement
     */
    public DCLStatement bind(final DCLStatement statement) {
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, currentDatabaseName, hintValueContext, statement);
        if (statement instanceof DenyUserStatement) {
            return new DenyUserStatementBinder().bind((DenyUserStatement) statement, binderContext);
        }
        return statement;
    }
}
