package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal;

import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ShowStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public abstract class ShowExtractor extends DALStatementExtractor {

    @Override
    protected SQLStatement createStatement() {
        return new ShowStatement();
    }
}
