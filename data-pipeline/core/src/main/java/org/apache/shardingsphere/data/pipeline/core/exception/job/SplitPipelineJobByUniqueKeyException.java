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

package org.apache.shardingsphere.data.pipeline.core.exception.job;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;

import java.sql.SQLException;

/**
 * Split pipeline job by unique key exception.
 */
public final class SplitPipelineJobByUniqueKeyException extends PipelineJobException {
    
    private static final long serialVersionUID = -7804078676439253443L;
    
    public SplitPipelineJobByUniqueKeyException(final QualifiedTable qualifiedTable, final String uniqueKey, final SQLException cause) {
        super(XOpenSQLState.GENERAL_ERROR, 4, String.format("Can not split by unique key '%s' for table '%s'.", uniqueKey, qualifiedTable.format()), cause);
    }
}
