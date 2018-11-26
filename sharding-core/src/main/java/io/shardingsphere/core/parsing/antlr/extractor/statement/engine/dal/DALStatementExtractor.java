package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal;

import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.AbstractSQLStatementExtractor;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;

public abstract class DALStatementExtractor extends AbstractSQLStatementExtractor {
    
    @Override
    protected SQLStatement createStatement() {
        return new DALStatement();
    }
}
