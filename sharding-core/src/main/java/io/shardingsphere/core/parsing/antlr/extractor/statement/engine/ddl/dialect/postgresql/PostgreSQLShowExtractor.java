package io.shardingsphere.core.parsing.antlr.extractor.statement.engine.ddl.dialect.postgresql;

import io.shardingsphere.core.parsing.antlr.extractor.segment.engine.dialect.postgresql.ShowParamExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.engine.dal.ShowExtractor;

public final class PostgreSQLShowExtractor extends ShowExtractor {
    
    public PostgreSQLShowExtractor() {
        addSQLSegmentExtractor(new ShowParamExtractor());
    }
}
