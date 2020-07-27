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

import com.google.common.base.Joiner;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Literal assignment token for encrypt.
 */
public final class EncryptLiteralAssignmentToken extends EncryptAssignmentToken {
    
    private final Collection<LiteralAssignment> assignments = new LinkedList<>();
    
    public EncryptLiteralAssignmentToken(final int startIndex, final int stopIndex) {
        super(startIndex, stopIndex);
    }
    
    /**
     * Add assignment.
     *
     * @param columnName column name
     * @param value assignment value
     */
    public void addAssignment(final String columnName, final Object value) {
        assignments.add(new LiteralAssignment(columnName, value));
    }
    
    @Override
    public String toString() {
        return Joiner.on(", ").join(assignments);
    }
    
    @RequiredArgsConstructor
    private static final class LiteralAssignment {
        
        private final String columnName;
        
        private final Object value;
        
        @Override
        public String toString() {
            return String.format("%s = %s", columnName, toString(value));
        }
    
        private String toString(final Object value) {
            return String.class == value.getClass() ? String.format("'%s'", value) : value.toString();
        }
    }
}
