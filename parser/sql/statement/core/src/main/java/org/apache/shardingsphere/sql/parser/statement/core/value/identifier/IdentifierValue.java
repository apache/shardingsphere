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

package org.apache.shardingsphere.sql.parser.statement.core.value.identifier;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;
import org.apache.shardingsphere.sql.parser.statement.core.value.ValueASTNode;

/**
 * Identifier value.
 */
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public final class IdentifierValue implements ValueASTNode<String> {
    
    private final String value;
    
    private final QuoteCharacter quoteCharacter;
    
    public IdentifierValue(final String text) {
        quoteCharacter = QuoteCharacter.getQuoteCharacter(text);
        if (QuoteCharacter.BACK_QUOTE == quoteCharacter) {
            value = SQLUtils.getRealContentInBackticks(text);
        } else {
            value = null == text ? null : quoteCharacter.unwrap(text);
        }
    }
    
    /**
     * Get value with quote characters, i.e. `table1` or `field1`
     *
     * @return value with quote characters
     */
    public String getValueWithQuoteCharacters() {
        return null == value ? "" : quoteCharacter.wrap(value);
    }
}
