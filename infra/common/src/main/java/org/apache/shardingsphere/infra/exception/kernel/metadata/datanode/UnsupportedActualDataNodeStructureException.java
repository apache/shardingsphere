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

package org.apache.shardingsphere.infra.exception.kernel.metadata.datanode;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

import java.util.Collection;

/**
 * Unsupported actual data node structure exception.
 */
public final class UnsupportedActualDataNodeStructureException extends DataNodeDefinitionException {
    
    private static final long serialVersionUID = -8921823916974492519L;
    
    public UnsupportedActualDataNodeStructureException(final String dataSourceName, final String tableName, final Collection<String> jdbcUrlPrefixes) {
        super(XOpenSQLState.FEATURE_NOT_SUPPORTED, 1, "Can not support 3-tier structure for actual data node '%s.%s' with JDBC '%s'.", dataSourceName, tableName, jdbcUrlPrefixes);
    }
}
