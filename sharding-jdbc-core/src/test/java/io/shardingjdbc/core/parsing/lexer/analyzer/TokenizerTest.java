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

import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Literals;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.lexer.token.Token;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import org.junit.Test;

import org.apache.commons.lang3.builder.EqualsBuilder;

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
    public void assertSkipHint() {
        String comment = "/*--xyz \n WHERE XX=1 //xyz*/";
        String sql = "SELECT * FROM XXX_TABLE " + comment + "WHERE YY>2";
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("/"));
        int expected = sql.indexOf("/") + comment.length();
        assertThat(tokenizer.skipHint(), is(expected));
    }
    
    @Test(expected = UnterminatedCharException.class)
    public void assertSkipHintUnterminatedCharException() {
        String comment = "/*--xyz \n WHERE XX=1 //xyz";
        String sql = "SELECT * FROM XXX_TABLE " + comment;
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("/"));
        tokenizer.skipHint();
    }
    
    @Test
    public void assertScanVariable() {
        String sql = "SELECT * FROM XXX_TABLE %s WHERE YY>2";
        assertScanVariable(sql, "@var");
        assertScanVariable(sql, "@@var");
    }

    private void assertScanVariable(final String sql, final String literals) {
        String formatSql = String.format(sql, literals);
        Tokenizer tokenizer = new Tokenizer(formatSql, dictionary, formatSql.indexOf("@"));
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanVariable(), new Token(Literals.VARIABLE, literals, formatSql.indexOf("WHERE") - 1)));
    }
    
    @Test
    public void assertScanNumber() {
        String sql = "SELECT * FROM XXX_TABLE WHERE XX=%s";
        assertScanNumber(sql, "123", Literals.INT);
        assertScanNumber(sql, "-123", Literals.INT);
        assertScanNumber(sql, "123.0", Literals.FLOAT);
        assertScanNumber(sql, "123e4", Literals.FLOAT);
        assertScanNumber(sql, "123E4", Literals.FLOAT);
        assertScanNumber(sql, "123e+4", Literals.FLOAT);
        assertScanNumber(sql, "123E+4", Literals.FLOAT);
        assertScanNumber(sql, "123e-4", Literals.FLOAT);
        assertScanNumber(sql, "123E-4", Literals.FLOAT);
        assertScanNumber(sql, ".5", Literals.FLOAT);
        assertScanNumber(sql, "123f", Literals.FLOAT);
        assertScanNumber(sql, "123F", Literals.FLOAT);
        assertScanNumber(sql, ".5f", Literals.FLOAT);
        assertScanNumber(sql, ".5F", Literals.FLOAT);
        assertScanNumber(sql, "123d", Literals.FLOAT);
        assertScanNumber(sql, "123D", Literals.FLOAT);
        assertScanHexDecimal(sql, "0x1e", Literals.HEX);
        assertScanHexDecimal(sql, "0x-1e", Literals.HEX);
    }
    
    private void assertScanNumber(final String sql, final String literals, final TokenType type) {
        String formatSql = String.format(sql, literals);
        Tokenizer tokenizer = new Tokenizer(formatSql, dictionary, sql.indexOf("=") + 1);
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanNumber(), new Token(type, literals, formatSql.length())));
    }
    
    private void assertScanHexDecimal(final String sql, final String literals, final TokenType type) {
        String formatSql = String.format(sql, literals);
        Tokenizer tokenizer = new Tokenizer(formatSql, dictionary, sql.indexOf("=") + 1);
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanHexDecimal(), new Token(type, literals, formatSql.length())));
    }
    
    @Test
    public void assertScanNChars() {
        String sql = "SELECT * FROM ORDER, XX_TABLE AS `table` WHERE YY=N'xx' And group =-1 GROUP BY YY";
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("ORDER"));
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanIdentifier(), new Token(Literals.IDENTIFIER, "ORDER", sql.indexOf(","))));
        tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("GROUP"));
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanIdentifier(), new Token(DefaultKeyword.GROUP, "GROUP", sql.indexOf("BY") - 1)));
        tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("`"));
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanIdentifier(), new Token(Literals.IDENTIFIER, "`table`", sql.indexOf("WHERE") - 1)));
        tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("YY"));
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanIdentifier(), new Token(Literals.IDENTIFIER, "YY", sql.indexOf("="))));
        tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("=-"));
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanSymbol(), new Token(Symbol.EQ, "=", sql.indexOf("=-") + 1)));
        tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("'"));
        assertTrue(EqualsBuilder.reflectionEquals(tokenizer.scanChars(), new Token(Literals.CHARS, "xx", sql.indexOf("And") - 1)));
    }
    
    @Test(expected = UnterminatedCharException.class)
    public void assertScanChars() {
        String sql = "SELECT * FROM XXX_TABLE AS `TEST";
        Tokenizer tokenizer = new Tokenizer(sql, dictionary, sql.indexOf("`"));
        tokenizer.scanChars();
    }
}
