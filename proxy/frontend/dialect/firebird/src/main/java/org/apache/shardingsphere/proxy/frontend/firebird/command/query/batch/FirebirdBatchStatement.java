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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.FirebirdBlrRowMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Firebird batch statement metadata.
 */
@Getter
public final class FirebirdBatchStatement {
    
    private final int statementHandle;
    
    @Setter
    private long batchMessageCount;
    
    private final long batchMessageLength;
    
    @Setter
    private byte[] batchData;
    
    @Setter
    private List<List<Object>> parameterValues = new ArrayList<>();
    
    private final FirebirdBlrRowMetadata batchBlr;
    
    public FirebirdBatchStatement(final int statementHandle, final long batchMessageLength, final FirebirdBlrRowMetadata batchBlr) {
        this.statementHandle = statementHandle;
        this.batchMessageLength = batchMessageLength;
        this.batchBlr = batchBlr;
    }
    
    /**
     * Get parameter values for batch entry.
     *
     * @param index batch entry index
     * @return parameter values for entry
     */
    public List<Object> getParameterValues(final int index) {
        return index < parameterValues.size() ? parameterValues.get(index) : Collections.emptyList();
    }
}
