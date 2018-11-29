package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal.dialect.postgresql;

import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal.DALStatementExtractor;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ResetParamStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public class PostgreSQLResetParamExtractor extends DALStatementExtractor {

    @Override
    protected SQLStatement createStatement() {
        return new ResetParamStatement();
    }
}
