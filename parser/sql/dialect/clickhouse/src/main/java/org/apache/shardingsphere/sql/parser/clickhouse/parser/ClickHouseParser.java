package org.apache.shardingsphere.sql.parser.clickhouse.parser;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.parser.SQLParser;
import org.apache.shardingsphere.sql.parser.autogen.ClickHouseStatementParser;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;

/**
 * @author zzypersonally@gmail.com
 * @description
 * @since 2024/5/7 15:59
 */
public final class ClickHouseParser extends ClickHouseStatementParser implements SQLParser {

    public ClickHouseParser(final TokenStream input) {
        super(input);
    }

    @Override
    public ASTNode parse() {
        return new ParseASTNode(execute(), (CommonTokenStream) getTokenStream());
    }
}
