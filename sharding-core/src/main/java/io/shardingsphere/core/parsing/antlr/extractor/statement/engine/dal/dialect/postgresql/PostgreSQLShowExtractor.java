package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal.dialect.postgresql;

import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.dialect.postgresql.ShowParamExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal.DALStatementExtractor;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ShowStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public final class PostgreSQLShowExtractor extends DALStatementExtractor {
    
    public PostgreSQLShowExtractor() {
        addSQLSegmentExtractor(new ShowParamExtractor());
    }

    @Override
    protected SQLStatement createStatement() {
        return new ShowStatement();
    }
}
