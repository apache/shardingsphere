package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.facade;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableReferencesClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Delete clause parser facade.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractDeleteClauseParserFacade {
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    private final WhereClauseParser whereClauseParser;
}
