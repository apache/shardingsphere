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

package org.apache.shardingsphere.encrypt.rewrite.token.pojo;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Function assignment token for encrypt.
 */
public final class EncryptFunctionAssignmentToken extends EncryptAssignmentToken {
    
    private final StringBuilder builder = new StringBuilder();
    
    private final Collection<FunctionAssignment> assignments = new LinkedList<>();
    
    public EncryptFunctionAssignmentToken(final int startIndex, final int stopIndex, final QuoteCharacter quoteCharacter) {
        super(startIndex, stopIndex, quoteCharacter);
    }
    
    /**
     * Add assignment.
     *
     * @param columnName column name
     * @param value assignment value
     */
    public void addAssignment(final String columnName, final Object value) {
        FunctionAssignment functionAssignment = new FunctionAssignment(columnName, value, getQuoteCharacter());
        assignments.add(functionAssignment);
        builder.append(functionAssignment).append(", ");
    }
    
    /**
     * Judge whether assignments is empty or not.
     *
     * @return whether assignments is empty or not
     */
    public boolean isAssignmentsEmpty() {
        return assignments.isEmpty();
    }
    
    @Override
    public String toString() {
        return builder.substring(0, builder.length() - 2);
    }
    
    @RequiredArgsConstructor
    private static final class FunctionAssignment {
        
        private final String columnName;
        
        private final Object value;
        
        private final QuoteCharacter quoteCharacter;
        
        @Override
        public String toString() {
            return quoteCharacter.wrap(columnName) + " = " + value;
        }
    }
}
