package org.apache.shardingsphere.sharding.distsql.parser.statement;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRulesStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;

public class ShowShardingKeyGeneratorsStatement extends ShowRulesStatement {
    public ShowShardingKeyGeneratorsStatement(SchemaSegment schema) {
        super(schema);
    }
}
