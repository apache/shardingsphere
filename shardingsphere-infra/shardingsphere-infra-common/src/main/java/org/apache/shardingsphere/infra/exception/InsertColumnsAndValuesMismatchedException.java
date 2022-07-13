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

package org.apache.shardingsphere.infra.exception;

import lombok.Getter;

/**
 * Insert columns and values mismatched exception.
 */
@Getter
public final class InsertColumnsAndValuesMismatchedException extends ShardingSphereException {
    
    private static final long serialVersionUID = 5676889868213244575L;
    
    private static final String ERROR_MESSAGE = "The count columns and values are mismatched in INSERT statement.";
    
    private final int mismatchedRowNumber;
    
    public InsertColumnsAndValuesMismatchedException(final int mismatchedRowNumber) {
        super(ERROR_MESSAGE);
        this.mismatchedRowNumber = mismatchedRowNumber;
    }
}
