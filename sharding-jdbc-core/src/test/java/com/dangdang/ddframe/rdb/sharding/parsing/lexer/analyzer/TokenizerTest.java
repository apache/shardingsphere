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

package com.dangdang.ddframe.rdb.sharding.parsing.lexer.analyzer;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Token;
import org.junit.Test;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;

public final class TokenizerTest {

    private final Dictionary dictionary = new Dictionary();

    @Test
    public void assertSkipWhitespace() {
        String sql = "SELECT *\tFROM\rTABLE_XXX\n";
        for (int i = 0; i < sql.length(); i++) {
            Tokenizer tokenizer = new Tokenizer(sql, dictionary, i);
            int expected = i;
            if (CharType.isWhitespace(sql.charAt(i))) {
                expected += 1;
            }
            assertThat(tokenizer.skipWhitespace(), is(expected));
        }
    }

    @Test
    public void assertSkipCommentWithoutComment() {
        String sql = "SELECT * FROM XXX_TABLE";
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("_"));
        assertThat(tokenizer.skipComment(), is(sql.indexOf("_")));
    }

    @Test
    public void assertSkipSingleLineComment() {
        String singleLineCommentWithHyphen = "--x\"y`z\n";
        String singleLineCommentWithSlash = "//x\\\"y'z\n";
        String sql = "SELECT * FROM XXX_TABLE " + singleLineCommentWithHyphen + "WHERE XX" + singleLineCommentWithSlash + "=1 ";
        Tokenizer hyphenTokenizer = new Tokenizer(sql, dictionary, sql.indexOf("-"));
        int expected = sql.indexOf("-") + singleLineCommentWithHyphen.length();
        assertThat(hyphenTokenizer.skipComment(), is(expected));
        Tokenizer slashTokenizer = new Tokenizer(sql, dictionary, sql.indexOf("/"));
        expected = sql.indexOf("/") + singleLineCommentWithSlash.length();
        assertThat(slashTokenizer.skipComment(), is(expected));
    }

    @Test
    public void assertSkipSingleLineMySQLComment() {
        String comment = "#x\"y`z\n";
        String sql = "SELECT * FROM XXX_TABLE " + comment + "WHERE XX=1";
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("#"));
        int expected = sql.indexOf("#") + comment.length();
        assertThat(tokenizer.skipComment(), is(expected));
    }

    @Test
    public void assertSkipMultipleLineComment() {
        String comment = "/*--xyz \n WHERE XX=1 //xyz*/";
        String sql = "SELECT * FROM XXX_TABLE " + comment + "WHERE YY>2";
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("/"));
        int expected = sql.indexOf("/") + comment.length();
        assertThat(tokenizer.skipComment(), is(expected));
    }

    @Test(expected = UnterminatedCharException.class)
    public void assertSkipMultipleLineCommentUnterminatedCharException() {
        String comment = "/*--xyz \n WHERE XX=1 //xyz";
        String sql = "SELECT * FROM XXX_TABLE " + comment;
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("/"));
        tokenizer.skipComment();
    }

    @Test
    public void assertScanVariable() {
        String sql = "SELECT * FROM XXX_TABLE @var WHERE YY>2";
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("@"));
        assertTrue(new ReflectionEquals(tokenizer.scanVariable()).matches(new Token(Literals.VARIABLE, "@var", 28)));
    }
}
