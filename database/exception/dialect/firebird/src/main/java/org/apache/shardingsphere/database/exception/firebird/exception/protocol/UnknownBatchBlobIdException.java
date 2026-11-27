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

package org.apache.shardingsphere.database.exception.firebird.exception.protocol;

import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;

/**
 * Unknown batch BLOB ID exception for Firebird.
 */
@Getter
public final class UnknownBatchBlobIdException extends SQLDialectException {
    
    private static final long serialVersionUID = 873093432879341925L;
    
    private final long batchBlobId;
    
    public UnknownBatchBlobIdException(final long batchBlobId) {
        super(String.format("Unknown batch BLOB ID: %d", batchBlobId));
        this.batchBlobId = batchBlobId;
    }
}
