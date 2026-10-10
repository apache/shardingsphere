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
 * Batch BLOB segment too big exception for Firebird.
 */
@Getter
public final class BatchSegmentTooBigException extends SQLDialectException {
    
    private static final long serialVersionUID = 84415505619003752L;
    
    private final int segmentLength;
    
    private final int availableLength;
    
    public BatchSegmentTooBigException(final int segmentLength, final int availableLength) {
        super(String.format("Batch BLOB segment length %d exceeds available data length %d", segmentLength, availableLength));
        this.segmentLength = segmentLength;
        this.availableLength = availableLength;
    }
}
