package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal.dialect.postgresql;

import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal.DALStatementExtractor;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.SetParamStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PostgreSQLSetParamExtractor extends DALStatementExtractor {

    @Override
    protected SQLStatement createStatement() {
        return new SetParamStatement();
    }
}
