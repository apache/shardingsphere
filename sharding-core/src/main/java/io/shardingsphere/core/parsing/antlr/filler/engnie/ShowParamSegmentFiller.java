package io.shardingsphere.core.parsing.antlr.filler.engnie;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLSegmentFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.ShowParamSegment;
import io.shardingsphere.core.parsing.parser.dialect.postgresql.statement.ShowStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;

public class ShowParamSegmentFiller implements SQLSegmentFiller {
    
    @Override
    public void fill(SQLSegment sqlSegment, SQLStatement sqlStatement, ShardingRule shardingRule,
            ShardingTableMetaData shardingTableMetaData) {
        ShowParamSegment segment = (ShowParamSegment) sqlSegment;
        ShowStatement statement = (ShowStatement) sqlStatement;
        statement.setName(segment.getName());
    }
}
