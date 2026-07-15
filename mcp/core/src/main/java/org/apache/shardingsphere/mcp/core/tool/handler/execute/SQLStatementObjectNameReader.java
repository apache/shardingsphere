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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;

import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SQLStatementObjectNameReader {
    
    private static final List<String> OBJECT_LIST_STOP_KEYWORDS = List.of("JOIN", "WHERE", "ON", "USING", "SET", "VALUES", "RETURNING", "GROUP", "ORDER", "HAVING", "LIMIT", "OFFSET",
            "FETCH", "UNION", "EXCEPT", "INTERSECT", "WHEN", "FROM", "TO");
    
    private final SQLStatementScanner scanner;
    
    SQLStatementObjectName readObjectName(final List<SQLStatementToken> tokens, final int startIndex, final String... optionalKeywords) {
        int currentIndex = startIndex;
        while (currentIndex < tokens.size()) {
            SQLStatementToken token = tokens.get(currentIndex);
            if (scanner.isKeyword(token, optionalKeywords)) {
                currentIndex++;
                continue;
            }
            if ("(".equals(token.text())) {
                return new SQLStatementObjectName("", "", QuoteCharacter.NONE, false, currentIndex);
            }
            return readQualifiedName(tokens, currentIndex);
        }
        return new SQLStatementObjectName("", "", QuoteCharacter.NONE, false, currentIndex);
    }
    
    SQLStatementObjectName readQualifiedName(final List<SQLStatementToken> tokens, final int startIndex) {
        if (startIndex >= tokens.size() || !isObjectNameSegment(tokens.get(startIndex))) {
            return new SQLStatementObjectName("", "", QuoteCharacter.NONE, false, startIndex);
        }
        SQLStatementToken firstToken = tokens.get(startIndex);
        String firstIdentifier = scanner.normalizeIdentifier(firstToken.text());
        StringBuilder result = new StringBuilder(firstIdentifier);
        int currentIndex = startIndex + 1;
        boolean qualified = false;
        while (currentIndex + 1 < tokens.size() && ".".equals(tokens.get(currentIndex).text()) && isObjectNameSegment(tokens.get(currentIndex + 1))) {
            result.append('.').append(scanner.normalizeIdentifier(tokens.get(currentIndex + 1).text()));
            currentIndex += 2;
            qualified = true;
        }
        return new SQLStatementObjectName(result.toString(), firstIdentifier, QuoteCharacter.getQuoteCharacter(firstToken.text()), qualified, currentIndex);
    }
    
    int skipObjectTail(final List<SQLStatementToken> tokens, final int startIndex) {
        int result = startIndex;
        int parenthesesDepth = 0;
        while (result < tokens.size()) {
            SQLStatementToken token = tokens.get(result);
            if ("(".equals(token.text())) {
                parenthesesDepth++;
                result++;
                continue;
            }
            if (")".equals(token.text())) {
                parenthesesDepth--;
                result++;
                continue;
            }
            if (0 == parenthesesDepth && (",".equals(token.text()) || OBJECT_LIST_STOP_KEYWORDS.contains(token.upperText()))) {
                return result;
            }
            result++;
        }
        return result;
    }
    
    private boolean isObjectNameSegment(final SQLStatementToken token) {
        return token.identifier() || "*".equals(token.text());
    }
}
