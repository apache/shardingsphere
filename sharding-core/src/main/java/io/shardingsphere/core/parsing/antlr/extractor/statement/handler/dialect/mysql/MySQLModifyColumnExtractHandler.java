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

package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.dialect.mysql;

import org.antlr.v4.runtime.ParserRuleContext;

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.ModifyColumnExtractHandler;
import io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result.ColumnDefinitionExtractResult;
import io.shardingsphere.core.parsing.antlr.extractor.statement.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnPosition;

/**
 * Modify column extract handler for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLModifyColumnExtractHandler extends ModifyColumnExtractHandler {
    
    @Override
    protected void postExtractColumnDefinition(final ParserRuleContext ancestorNode, final ColumnDefinitionExtractResult columnDefinition) {
        Optional<ColumnPosition> columnPosition = ExtractorUtils.extractFirstOrAfterColumn((ParserRuleContext) ancestorNode, columnDefinition.getName());
        if (columnPosition.isPresent()) {
            columnDefinition.setPosition(columnPosition.get());
        }
    }
}
