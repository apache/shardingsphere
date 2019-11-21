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

package io.shardingsphere.core.parsing.antlr.extractor;

import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.Collection;

/**
 * Collection SQL segment extractor.
 * 
 * @author zhangliang
 */
public interface CollectionSQLSegmentExtractor extends SQLSegmentExtractor {
    
    /**
     * Extract SQL segments from SQL AST.
     *  
     * @param ancestorNode ancestor node of AST
     * @return SQL segments
     */
    Collection<? extends SQLSegment> extract(ParserRuleContext ancestorNode);
}
