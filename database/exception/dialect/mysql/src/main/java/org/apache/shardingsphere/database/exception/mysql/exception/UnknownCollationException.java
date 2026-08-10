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

package org.apache.shardingsphere.database.exception.mysql.exception;

import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;

/**
 * Unknown collation exception.
 */
@Getter
public final class UnknownCollationException extends SQLDialectException {
    
    private static final long serialVersionUID = 6920150607711135228L;
    
    private final int collationId;
    
    private final String collation;
    
    /**
     * Construct an unknown collation exception by collation ID.
     *
     * @param collationId collation ID
     */
    public UnknownCollationException(final int collationId) {
        this.collationId = collationId;
        collation = String.valueOf(collationId);
    }
    
    /**
     * Construct an unknown collation exception by collation name.
     *
     * @param collation collation name
     */
    public UnknownCollationException(final String collation) {
        collationId = -1;
        this.collation = collation;
    }
}
