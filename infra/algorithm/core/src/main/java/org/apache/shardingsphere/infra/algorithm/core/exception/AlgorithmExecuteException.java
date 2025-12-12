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

import org.apache.shardingsphere.infra.algorithm.core.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Algorithm execute exception.
 */
// TODO It is runnable exception, consider about move out from AlgorithmDefinitionException
public final class AlgorithmExecuteException extends AlgorithmDefinitionException {
    
    private static final long serialVersionUID = -9099514178650043282L;
    
    public AlgorithmExecuteException(final ShardingSphereAlgorithm algorithm, final String reason, final Object... args) {
        super(XOpenSQLState.GENERAL_ERROR, 40, "Algorithm '%s' execute failed, reason is: %s.",
                algorithm.getClass().getSimpleName(), String.format(reason, args));
    }
}
