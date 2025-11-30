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

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.StringJoiner;

/**
 * Parameter assignment token for encrypt.
 */
public final class EncryptParameterAssignmentToken extends EncryptAssignmentToken {
    
    private final Collection<String> columnNames = new LinkedList<>();
    
    public EncryptParameterAssignmentToken(final int startIndex, final int stopIndex, final QuoteCharacter quoteCharacter) {
        super(startIndex, stopIndex, quoteCharacter);
    }
    
    /**
     * Add column name.
     *
     * @param columnName column name
     */
    public void addColumnName(final String columnName) {
        columnNames.add(columnName);
    }
    
    @Override
    public String toString() {
        StringJoiner result = new StringJoiner(", ");
        for (String each : columnNames) {
            result.add(getQuoteCharacter().wrap(each) + " = ?");
        }
        return result.toString();
    }
}
