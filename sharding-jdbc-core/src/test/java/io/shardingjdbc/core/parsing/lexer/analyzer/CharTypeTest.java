/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.parsing.lexer.analyzer;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

public final class CharTypeTest {
    
    @Test
    public void assertIsWhitespace() {
        for (int i = 0; i < 256; i++) {
            if (Character.isWhitespace((char) i)) {
                assertThat(CharType.isWhitespace((char) i), is(Character.isWhitespace((char) i)));
            }
        }
    }
    
    @Test
    public void assertIsEndOfInput() {
        assertTrue(CharType.isEndOfInput((char) 0x1A));
    }
    
    @Test
    public void assertIsAlphabet() {
        for (int i = 0; i < 256; i++) {
            if (CharType.isAlphabet((char) i)) {
                assertThat(CharType.isAlphabet((char) i), is(Character.isAlphabetic((char) i)));
            }
        }
    }
    
    @Test
    public void assertIsDigit() {
        for (int i = 0; i < 256; i++) {
            assertThat(CharType.isDigital((char) i), is(Character.isDigit((char) i)));
        }
    }
    
    @Test
    public void assertIsSymbol() {
        assertTrue(CharType.isSymbol('?'));
        assertTrue(CharType.isSymbol('#'));
        assertTrue(CharType.isSymbol('('));
    }
}
