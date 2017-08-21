package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Literals;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;

/**
 * 别名解析器.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public class AliasSQLParser implements SQLParser {
    
    private final CommonParser commonParser;
    
    /**
     * 解析别名.
     *
     * @return 别名
     */
    public Optional<String> parseAlias() {
        if (commonParser.skipIfEqual(DefaultKeyword.AS)) {
            if (commonParser.equalAny(Symbol.values())) {
                return Optional.absent();
            }
            String result = SQLUtil.getExactlyValue(commonParser.getLexer().getCurrentToken().getLiterals());
            commonParser.getLexer().nextToken();
            return Optional.of(result);
        }
        // TODO 增加哪些数据库识别哪些关键字作为别名的配置
        if (commonParser.equalAny(
                Literals.IDENTIFIER, Literals.CHARS, DefaultKeyword.USER, DefaultKeyword.END, DefaultKeyword.CASE, DefaultKeyword.KEY, DefaultKeyword.INTERVAL, DefaultKeyword.CONSTRAINT)) {
            String result = SQLUtil.getExactlyValue(commonParser.getLexer().getCurrentToken().getLiterals());
            commonParser.getLexer().nextToken();
            return Optional.of(result);
        }
        return Optional.absent();
    }
}
