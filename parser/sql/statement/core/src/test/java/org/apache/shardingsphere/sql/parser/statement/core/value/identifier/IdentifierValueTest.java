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

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class IdentifierValueTest {
    
    @Test
    void assertGetIdentifierValueWithSingleQuote() {
        String text = "'ms_group_${0..1}'";
        assertThat(new IdentifierValue(text).getValue(), is("ms_group_${0..1}"));
    }
    
    @Test
    void assertGetIdentifierValueWithQuote() {
        String text = "\"ms_group_${0..1}\"";
        assertThat(new IdentifierValue(text).getValue(), is("ms_group_${0..1}"));
    }
    
    @Test
    void assertGetIdentifierValueWithBackQuote() {
        String text = "`t_order`";
        assertThat(new IdentifierValue(text).getValue(), is("t_order"));
    }
    
    @Test
    void assertGetIdentifierValueWithReservedBracket() {
        String text = "ds_${[1,2]}.t_order";
        assertThat(new IdentifierValue(text).getValue(), is("ds_${[1,2]}.t_order"));
    }
    
    @Test
    void assertGetValueWithQuoteCharactersWithNullValue() {
        assertThat(new IdentifierValue(null).getValueWithQuoteCharacters(), is(""));
    }
    
    @Test
    void assertGetValueWithQuoteCharactersWithValue() {
        String text = "[foo]";
        assertThat(new IdentifierValue(text).getValueWithQuoteCharacters(), is("[foo]"));
    }
}
