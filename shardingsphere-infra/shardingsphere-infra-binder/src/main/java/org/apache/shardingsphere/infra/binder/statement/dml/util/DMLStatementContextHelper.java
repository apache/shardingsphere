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

package org.apache.shardingsphere.infra.binder.statement.dml.util;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.CallStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.DeleteStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.UpdateStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;

/**
 * DML statement context helper.
 */
public final class DMLStatementContextHelper {
    
    /**
     * Get schema name from DML statement context.
     * 
     * @param sqlStatementContext SQLStatementContext
     * @return schema name.
     */
    public static String getSchemaName(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof CallStatementContext) {
            return ((CallStatementContext) sqlStatementContext).getSchemaName();
        }
        if (sqlStatementContext instanceof DeleteStatementContext) {
            return ((DeleteStatementContext) sqlStatementContext).getSchemaName();
        }
        if (sqlStatementContext instanceof InsertStatementContext) {
            return ((InsertStatementContext) sqlStatementContext).getSchemaName();
        }
        if (sqlStatementContext instanceof SelectStatementContext) {
            return ((SelectStatementContext) sqlStatementContext).getSchemaName();
        }
        if (sqlStatementContext instanceof UpdateStatementContext) {
            return ((UpdateStatementContext) sqlStatementContext).getSchemaName();
        }
        return DefaultSchema.LOGIC_NAME;
    }
}
