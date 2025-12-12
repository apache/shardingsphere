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

package org.apache.shardingsphere.sharding.exception.metadata;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.sharding.exception.ShardingSQLException;

/**
 * Duplicate sharding actual data node exception.
 */
public final class DuplicateShardingActualDataNodeException extends ShardingSQLException {
    
    private static final long serialVersionUID = 3503761639898230998L;
    
    public DuplicateShardingActualDataNodeException(final String logicalTableName, final String dataSourceName, final String tableName) {
        super(XOpenSQLState.DUPLICATE, 12,
                "Same actual data node cannot be configured in multiple logic tables in same database, logical table '%s', actual data node '%s.%s'.", logicalTableName, dataSourceName, tableName);
    }
}
