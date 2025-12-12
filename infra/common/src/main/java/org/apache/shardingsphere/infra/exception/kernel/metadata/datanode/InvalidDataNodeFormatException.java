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

/**
 * Invalid data node format exception.
 */
public final class InvalidDataNodeFormatException extends DataNodeDefinitionException {
    
    private static final long serialVersionUID = 192279170808654743L;
    
    public InvalidDataNodeFormatException(final String dataNode) {
        super(XOpenSQLState.GENERAL_ERROR, 0, "Invalid format for actual data node '%s'.", dataNode);
    }
    
    public InvalidDataNodeFormatException(final String dataNode, final String reason) {
        super(XOpenSQLState.GENERAL_ERROR, 0, "Invalid format for data node '%s', reason is: %s.", dataNode, reason);
    }
}
