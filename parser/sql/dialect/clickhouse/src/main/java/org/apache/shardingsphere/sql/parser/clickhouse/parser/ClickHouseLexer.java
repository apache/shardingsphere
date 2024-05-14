package org.apache.shardingsphere.sql.parser.clickhouse.parser;

import org.antlr.v4.runtime.CharStream;
import org.apache.shardingsphere.sql.parser.api.parser.SQLLexer;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementLexer;

/**
 * @author zzypersonally@gmail.com
 * @description
 * @since 2024/5/7 15:56
 */
public  final class ClickHouseLexer extends ClickHouseStatementLexer implements SQLLexer {
    public ClickHouseLexer(final CharStream input) {
        super(input);
    }

}
