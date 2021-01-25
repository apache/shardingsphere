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

package org.apache.shardingsphere.sql.parser.sql.common.constant;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class QuoteCharacterTest {
    
    @Test
    public void assertGetQuoteCharacterWithNullValue() {
        assertThat(QuoteCharacter.getQuoteCharacter(null), is(QuoteCharacter.NONE));
    }
    
    @Test
    public void assertGetQuoteCharacterWithEmptyValue() {
        assertThat(QuoteCharacter.getQuoteCharacter(""), is(QuoteCharacter.NONE));
    }
    
    @Test
    public void assertGetQuoteCharacterWithNone() {
        assertThat(QuoteCharacter.getQuoteCharacter("tbl"), is(QuoteCharacter.NONE));
    }
    
    @Test
    public void assertGetQuoteCharacterWithBackQuote() {
        assertThat(QuoteCharacter.getQuoteCharacter("`tbl`"), is(QuoteCharacter.BACK_QUOTE));
    }
    
    @Test
    public void assertGetQuoteCharacterWithSingleQuote() {
        assertThat(QuoteCharacter.getQuoteCharacter("'tbl'"), is(QuoteCharacter.SINGLE_QUOTE));
    }
    
    @Test
    public void assertGetQuoteCharacterWithQuote() {
        assertThat(QuoteCharacter.getQuoteCharacter("\"tbl\""), is(QuoteCharacter.QUOTE));
    }
    
    @Test
    public void assertGetQuoteCharacterWithBrackets() {
        assertThat(QuoteCharacter.getQuoteCharacter("[tbl]"), is(QuoteCharacter.BRACKETS));
    }
    
    @Test
    public void assertWarp() {
        assertThat(QuoteCharacter.BACK_QUOTE.wrap("test"), is("`test`"));
    }
}
