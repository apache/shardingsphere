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

package org.apache.shardingsphere.data.pipeline.core.exception.data;

import org.apache.shardingsphere.infra.exception.core.external.sql.type.kernel.category.PipelineSQLException;
import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;

import javax.annotation.Nullable;

/**
 * Pipeline table data consistency check loading failed exception.
 */
public final class PipelineTableDataConsistencyCheckLoadingFailedException extends PipelineSQLException {
    
    private static final long serialVersionUID = 8965231249677009738L;
    
    public PipelineTableDataConsistencyCheckLoadingFailedException(@Nullable final String schemaName, final String tableName) {
        this(schemaName, tableName, null);
    }
    
    public PipelineTableDataConsistencyCheckLoadingFailedException(@Nullable final String schemaName, final String tableName, final Exception cause) {
        super(XOpenSQLState.CONNECTION_EXCEPTION, 51, String.format("Data check table `%s` failed.", null != schemaName ? schemaName + "." + tableName : tableName), cause);
    }
}
