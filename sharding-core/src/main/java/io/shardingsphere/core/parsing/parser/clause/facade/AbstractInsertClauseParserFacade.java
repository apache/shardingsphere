/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.clause.facade;

import io.shardingsphere.core.parsing.parser.clause.InsertColumnsClauseParser;
import io.shardingsphere.core.parsing.parser.clause.InsertDuplicateKeyUpdateClauseParser;
import io.shardingsphere.core.parsing.parser.clause.InsertIntoClauseParser;
import io.shardingsphere.core.parsing.parser.clause.InsertSetClauseParser;
import io.shardingsphere.core.parsing.parser.clause.InsertValuesClauseParser;
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
    
    private final InsertDuplicateKeyUpdateClauseParser insertDuplicateKeyUpdateClauseParser;
}
