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

package org.apache.shardingsphere.database.connector.core.metadata.database.enums;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuoteCharacterTest {
    
    @Test
    void assertGetQuoteCharacterWithNullValue() {
        assertThat(QuoteCharacter.getQuoteCharacter(null), is(QuoteCharacter.NONE));
    }
    
    @Test
    void assertGetQuoteCharacterWithEmptyValue() {
        assertThat(QuoteCharacter.getQuoteCharacter(""), is(QuoteCharacter.NONE));
    }
    
    @Test
    void assertGetQuoteCharacterWithNone() {
        assertThat(QuoteCharacter.getQuoteCharacter("test"), is(QuoteCharacter.NONE));
    }
    
    @Test
    void assertGetQuoteCharacterWithBackQuote() {
        assertThat(QuoteCharacter.getQuoteCharacter("`test`"), is(QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    void assertGetQuoteCharacterWithSingleQuote() {
        assertThat(QuoteCharacter.getQuoteCharacter("'test'"), is(QuoteCharacter.SINGLE_QUOTE));
    }
    
    @Test
    void assertGetQuoteCharacterWithQuote() {
        assertThat(QuoteCharacter.getQuoteCharacter("\"test\""), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    void assertGetQuoteCharacterWithBrackets() {
        assertThat(QuoteCharacter.getQuoteCharacter("[test]"), is(QuoteCharacter.BRACKETS));
    }
    
    @Test
    void assertWarp() {
        assertThat(QuoteCharacter.BACK_QUOTE.wrap("test"), is("`test`"));
    }
    
    @Test
    void assertUnwrapWithWrappedValue() {
        assertThat(QuoteCharacter.BACK_QUOTE.unwrap("`test`"), is("test"));
    }
    
    @Test
    void assertUnwrapWithoutWrappedValue() {
        assertThat(QuoteCharacter.BACK_QUOTE.unwrap("[test]"), is("[test]"));
    }
    
    @Test
    void assertIsWrapped() {
        assertTrue(QuoteCharacter.SINGLE_QUOTE.isWrapped("'test'"));
    }
    
    @Test
    void assertIsNotWrapped() {
        assertFalse(QuoteCharacter.SINGLE_QUOTE.isWrapped("'test\""));
    }
    
    @Test
    void assertUnwrapText() {
        assertThat(QuoteCharacter.unwrapText("test"), is("test"));
        assertThat(QuoteCharacter.unwrapText("`test`"), is("test"));
        assertThat(QuoteCharacter.unwrapText("'test'"), is("test"));
        assertThat(QuoteCharacter.unwrapText("\"test\""), is("test"));
        assertThat(QuoteCharacter.unwrapText("[test]"), is("test"));
        assertThat(QuoteCharacter.unwrapText("(test)"), is("test"));
        assertThat(QuoteCharacter.unwrapText("{test}"), is("{test}"));
    }
}
