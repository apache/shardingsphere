package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertColumnsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertIntoClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.InsertValuesClauseParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * INSERT从句解析器门面类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractInsertClauseParserFacade {
    
    private final InsertIntoClauseParser insertIntoClauseParser;
    
    private final InsertColumnsClauseParser insertColumnsClauseParser;
    
    private final InsertValuesClauseParser insertValuesClauseParser;
}
