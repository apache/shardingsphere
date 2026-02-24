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

package org.apache.shardingsphere.sql.parser.engine.core;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.sql.parser.api.ASTNode;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Parse AST node.
 */
@RequiredArgsConstructor
public final class ParseASTNode implements ASTNode {
    
    private final ParseTree parseTree;
    
    private final CommonTokenStream tokenStream;
    
    /**
     * Get root node.
     *
     * @return root node
     */
    public ParseTree getRootNode() {
        return parseTree.getChild(0);
    }
    
    /**
     * Get hidden tokens.
     *
     * @return hidden tokens
     */
    public Collection<Token> getHiddenTokens() {
        Collection<Token> result = new LinkedList<>();
        List<Token> allTokens = tokenStream.getTokens();
        int i = 0;
        while (i < allTokens.size()) {
            Token each = allTokens.get(i);
            if (Token.HIDDEN_CHANNEL != each.getChannel()) {
                i++;
                continue;
            }
            if (isExecutableCommentStart(each)) {
                int endIndex = findExecutableCommentEnd(allTokens, i);
                if (endIndex > i) {
                    result.add(buildMergedExecutableCommentToken(each, allTokens.get(endIndex)));
                    i = endIndex + 1;
                    continue;
                }
            }
            result.add(each);
            i++;
        }
        return result;
    }
    
    /**
     * Check if token is executable comment start.
     *
     * @param token token to check
     * @return true if executable comment start
     */
    private boolean isExecutableCommentStart(final Token token) {
        return token.getText().startsWith("/*!");
    }
    
    /**
     * Find executable comment end index.
     *
     * @param tokens all tokens
     * @param startIndex start index
     * @return end index or -1 if not found
     */
    private int findExecutableCommentEnd(final List<Token> tokens, final int startIndex) {
        for (int i = startIndex + 1; i < tokens.size(); i++) {
            Token each = tokens.get(i);
            if (Token.HIDDEN_CHANNEL == each.getChannel() && "*/".equals(each.getText())) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Build merged executable comment token.
     *
     * @param startToken start token
     * @param endToken end token
     * @return merged token
     */
    private Token buildMergedExecutableCommentToken(final Token startToken, final Token endToken) {
        CharStream charStream = tokenStream.getTokenSource().getInputStream();
        String fullText = charStream.getText(Interval.of(startToken.getStartIndex(), endToken.getStopIndex()));
        CommonToken merged = new CommonToken(startToken.getType());
        merged.setText(fullText);
        merged.setStartIndex(startToken.getStartIndex());
        merged.setStopIndex(endToken.getStopIndex());
        merged.setChannel(Token.HIDDEN_CHANNEL);
        return merged;
    }
}
