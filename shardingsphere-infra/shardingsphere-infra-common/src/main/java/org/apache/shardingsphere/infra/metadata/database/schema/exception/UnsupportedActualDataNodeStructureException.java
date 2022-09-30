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

package org.apache.shardingsphere.infra.metadata.database.schema.exception;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.MetaDataSQLException;
import org.apache.shardingsphere.infra.util.exception.external.sql.sqlstate.XOpenSQLState;

import java.util.Collection;

/**
 * Unsupported actual data node structure exception.
 */
public final class UnsupportedActualDataNodeStructureException extends MetaDataSQLException {
    
    private static final long serialVersionUID = -8921823916974492519L;
    
    public UnsupportedActualDataNodeStructureException(final DataNode dataNode, final Collection<String> jdbcUrlPrefixes) {
        super(XOpenSQLState.SYNTAX_ERROR, 2, "Can not support 3-tier structure for actual data node `%s` with JDBC `%s`.", dataNode.format(), jdbcUrlPrefixes.toString());
    }
}
