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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParseASTNodeTest {
    
    @Test
    void assertGetRootNode() {
        ParseTree parseTree = mock(ParseTree.class);
        when(parseTree.getChild(0)).thenReturn(parseTree);
        assertThat(new ParseASTNode(parseTree, mock(CommonTokenStream.class)).getRootNode(), is(parseTree));
    }
    
    @Test
    void assertGetHiddenTokensWithEmptyTokenList() {
        CommonTokenStream tokenStream = mock(CommonTokenStream.class);
        when(tokenStream.getTokens()).thenReturn(Collections.emptyList());
        assertTrue(new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens().isEmpty());
    }
    
    @Test
    void assertGetHiddenTokensWithNonHiddenTokens() {
        CommonTokenStream tokenStream = mock(CommonTokenStream.class);
        when(tokenStream.getTokens()).thenReturn(Collections.singletonList(createToken("SELECT", Token.DEFAULT_CHANNEL, 0, 5)));
        assertTrue(new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens().isEmpty());
    }
    
    @Test
    void assertGetHiddenTokensWithNonExecutableHiddenToken() {
        CommonTokenStream tokenStream = mock(CommonTokenStream.class);
        when(tokenStream.getTokens()).thenReturn(Collections.singletonList(createToken("/* normal comment */", Token.HIDDEN_CHANNEL, 0, 18)));
        Collection<Token> actual = new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getText(), is("/* normal comment */"));
    }
    
    @Test
    void assertGetHiddenTokensWithUnclosedExecutableComment() {
        CommonTokenStream tokenStream = mock(CommonTokenStream.class);
        when(tokenStream.getTokens()).thenReturn(Collections.singletonList(createToken("/*! SET x=1", Token.HIDDEN_CHANNEL, 0, 10)));
        Collection<Token> actual = new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getText(), is("/*! SET x=1"));
    }
    
    @Test
    void assertGetHiddenTokensWithEndCommentOnNonHiddenChannel() {
        CommonTokenStream tokenStream = mock(CommonTokenStream.class);
        when(tokenStream.getTokens()).thenReturn(Arrays.asList(
                createToken("/*!", Token.HIDDEN_CHANNEL, 0, 2),
                createToken("*/", Token.DEFAULT_CHANNEL, 10, 11)));
        Collection<Token> actual = new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getText(), is("/*!"));
    }
    
    @Test
    void assertGetHiddenTokensWithMergedExecutableComment() {
        String fullText = "/*! SET GLOBAL max_connections=123 */";
        CommonTokenStream tokenStream = createMergeCapableTokenStream(
                Arrays.asList(createToken("/*!", Token.HIDDEN_CHANNEL, 0, 2),
                        createToken("*/", Token.HIDDEN_CHANNEL, 34, 35)),
                0, 35, fullText);
        Collection<Token> actual = new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens();
        assertThat(actual.size(), is(1));
        Token merged = actual.iterator().next();
        assertThat(merged.getText(), is(fullText));
        assertThat(merged.getStartIndex(), is(0));
        assertThat(merged.getStopIndex(), is(35));
        assertThat(merged.getChannel(), is(Token.HIDDEN_CHANNEL));
    }
    
    @Test
    void assertGetHiddenTokensWithIntermediateNonHiddenTokensBetweenExecutableComment() {
        String fullText = "/*! SET max_connections=123 */";
        CommonTokenStream tokenStream = createMergeCapableTokenStream(
                Arrays.asList(createToken("/*!", Token.HIDDEN_CHANNEL, 0, 2),
                        createToken("SET", Token.DEFAULT_CHANNEL, 4, 6),
                        createToken("max_connections=123", Token.DEFAULT_CHANNEL, 8, 26),
                        createToken("*/", Token.HIDDEN_CHANNEL, 27, 28)),
                0, 28, fullText);
        Collection<Token> actual = new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getText(), is(fullText));
    }
    
    @Test
    void assertGetHiddenTokensWithMixedTokenTypes() {
        String execFullText = "/*! SET x=1 */";
        CommonTokenStream tokenStream = createMergeCapableTokenStream(
                Arrays.asList(createToken("SELECT", Token.DEFAULT_CHANNEL, 0, 5),
                        createToken(" ", Token.HIDDEN_CHANNEL, 6, 6),
                        createToken("/*!", Token.HIDDEN_CHANNEL, 7, 9),
                        createToken("*/", Token.HIDDEN_CHANNEL, 18, 19),
                        createToken("FROM", Token.DEFAULT_CHANNEL, 21, 24)),
                7, 19, execFullText);
        Collection<Token> actual = new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens();
        assertThat(actual.size(), is(2));
        Iterator<Token> iterator = actual.iterator();
        assertThat(iterator.next().getText(), is(" "));
        assertThat(iterator.next().getText(), is(execFullText));
    }
    
    @Test
    void assertGetHiddenTokensWithTwoExecutableComments() {
        String firstComment = "/*! SET x=1 */";
        String secondComment = "/*!80029 SET y=2 */";
        List<Token> tokens = Arrays.asList(
                createToken("/*!", Token.HIDDEN_CHANNEL, 0, 2),
                createToken("*/", Token.HIDDEN_CHANNEL, 13, 14),
                createToken("SELECT", Token.DEFAULT_CHANNEL, 16, 21),
                createToken("/*!", Token.HIDDEN_CHANNEL, 23, 28),
                createToken("*/", Token.HIDDEN_CHANNEL, 44, 45));
        CommonTokenStream tokenStream = mock(CommonTokenStream.class);
        when(tokenStream.getTokens()).thenReturn(tokens);
        TokenSource tokenSource = mock(TokenSource.class);
        when(tokenStream.getTokenSource()).thenReturn(tokenSource);
        CharStream charStream = mock(CharStream.class);
        when(tokenSource.getInputStream()).thenReturn(charStream);
        when(charStream.getText(any(Interval.class))).thenAnswer(invocation -> {
            Interval interval = invocation.getArgument(0);
            if (interval.a == 0 && interval.b == 14) {
                return firstComment;
            }
            if (interval.a == 23 && interval.b == 45) {
                return secondComment;
            }
            return "";
        });
        Collection<Token> actual = new ParseASTNode(mock(ParseTree.class), tokenStream).getHiddenTokens();
        assertThat(actual.size(), is(2));
        Iterator<Token> iterator = actual.iterator();
        Token first = iterator.next();
        assertThat(first.getText(), is(firstComment));
        assertThat(first.getStartIndex(), is(0));
        assertThat(first.getStopIndex(), is(14));
        assertThat(first.getChannel(), is(Token.HIDDEN_CHANNEL));
        Token second = iterator.next();
        assertThat(second.getText(), is(secondComment));
        assertThat(second.getStartIndex(), is(23));
        assertThat(second.getStopIndex(), is(45));
        assertThat(second.getChannel(), is(Token.HIDDEN_CHANNEL));
    }
    
    private static CommonToken createToken(final String text, final int channel, final int startIndex, final int stopIndex) {
        CommonToken result = new CommonToken(1, text);
        result.setChannel(channel);
        result.setStartIndex(startIndex);
        result.setStopIndex(stopIndex);
        return result;
    }
    
    private static CommonTokenStream createMergeCapableTokenStream(final java.util.List<Token> tokens, final int intervalStart, final int intervalStop, final String mergedText) {
        CommonTokenStream result = mock(CommonTokenStream.class);
        when(result.getTokens()).thenReturn(tokens);
        TokenSource tokenSource = mock(TokenSource.class);
        when(result.getTokenSource()).thenReturn(tokenSource);
        CharStream charStream = mock(CharStream.class);
        when(tokenSource.getInputStream()).thenReturn(charStream);
        when(charStream.getText(Interval.of(intervalStart, intervalStop))).thenReturn(mergedText);
        return result;
    }
}
