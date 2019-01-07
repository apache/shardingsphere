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

package io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.column.dialect.mysql;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.impl.ddl.column.ModifyColumnDefinitionExtractor;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.alter.ModifyColumnDefinitionSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.definition.column.position.ColumnPositionSegment;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Modify column definition extractor for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLModifyColumnDefinitionExtractor extends ModifyColumnDefinitionExtractor {
    
    @Override
    protected void postExtractColumnDefinition(final ParserRuleContext modifyColumnNode, final ModifyColumnDefinitionSegment modifyColumnDefinitionSegment) {
        Optional<ColumnPositionSegment> columnPositionSegment = new MySQLColumnPositionExtractor(modifyColumnDefinitionSegment.getColumnDefinition().getColumnName()).extract(modifyColumnNode);
        if (columnPositionSegment.isPresent()) {
            modifyColumnDefinitionSegment.setColumnPosition(columnPositionSegment.get());
        }
    }
}
