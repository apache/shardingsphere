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

package org.apache.shardingsphere.infra.algorithm.core.exception;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Unsupported algorithm on database type exception.
 */
public final class UnsupportedAlgorithmOnDatabaseTypeException extends AlgorithmDefinitionException {
    
    private static final long serialVersionUID = 9046024072116200648L;
    
    public UnsupportedAlgorithmOnDatabaseTypeException(final String algorithmType, final String algorithmDetailType, final DatabaseType databaseType) {
        super(XOpenSQLState.FEATURE_NOT_SUPPORTED, 10, "Unsupported %s.%s with database type '%s'.", algorithmType, algorithmDetailType, databaseType.getType());
    }
}
