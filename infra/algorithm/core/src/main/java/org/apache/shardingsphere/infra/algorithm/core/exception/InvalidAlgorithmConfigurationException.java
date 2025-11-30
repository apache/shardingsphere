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

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;

/**
 * Invalid algorithm configuration exception.
 */
// TODO consider about merge with AlgorithmInitializationException
public final class InvalidAlgorithmConfigurationException extends AlgorithmDefinitionException {
    
    private static final long serialVersionUID = 1352014079406440573L;
    
    public InvalidAlgorithmConfigurationException(final String algorithmType, final String algorithm) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 4, "Invalid %s algorithm configuration '%s'.", algorithmType, algorithm);
    }
    
    public InvalidAlgorithmConfigurationException(final String algorithmType) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 4, "Invalid %s algorithm configuration.", algorithmType);
    }
    
    public InvalidAlgorithmConfigurationException(final String algorithmType, final String algorithm, final String message) {
        super(XOpenSQLState.CHECK_OPTION_VIOLATION, 4, "Invalid %s algorithm configuration '%s'. %s.", algorithmType, algorithm, message);
    }
}
