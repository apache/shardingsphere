package io.shardingjdbc.dbtest.core.common.util;

import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLPlaceholderUtil {
    
    public static String replaceStatement(final String str, final Object[] args) {
        if (args.length == 0) {
            return str;
        }
        return String.format(str, args).replace("%%", "%");
    }
    
    public static String replacePreparedStatement(final String str) {
        return str.replace("%s", Symbol.QUESTION.getLiterals()).replace("%%", "%");
    }
}