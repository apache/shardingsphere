package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.UpdateSetItemsClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableReferenceClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * UPDATE从句解析器门面类.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractUpdateClauseParserFacade {
    
    private final TableReferenceClauseParser tableReferenceClauseParser;
    
    private final UpdateSetItemsClauseParser updateSetItemsClauseParser;
    
    private final WhereClauseParser whereClauseParser;
}
