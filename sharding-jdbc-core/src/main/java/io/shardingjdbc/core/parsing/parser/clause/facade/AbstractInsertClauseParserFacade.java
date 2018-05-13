package io.shardingjdbc.core.parsing.parser.clause.facade;

import io.shardingjdbc.core.parsing.parser.clause.InsertColumnsClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.InsertIntoClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.InsertSetClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.InsertValuesClauseParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Insert clause parser facade.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractInsertClauseParserFacade {
    
    private final InsertIntoClauseParser insertIntoClauseParser;
    
    private final InsertColumnsClauseParser insertColumnsClauseParser;
    
    private final InsertValuesClauseParser insertValuesClauseParser;
    
    private final InsertSetClauseParser insertSetClauseParser;
}
