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

package io.shardingsphere.core.parsing.antlr.extractor.phrase;

import com.google.common.base.Optional;
import io.shardingsphere.core.parsing.antlr.extractor.util.ASTUtils;
import io.shardingsphere.core.parsing.antlr.extractor.util.ExtractorUtils;
import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Extract modify column phrase.
 * 
 * @author duhongjun
 */
public class ModifyColumnExtractor implements PhraseExtractor {
    
    @Override
    public final void extract(final ParserRuleContext ancestorNode, final SQLStatement statement) {
        AlterTableStatement alterStatement = (AlterTableStatement) statement;
        for (ParserRuleContext each : ASTUtils.getAllDescendantNodes(ancestorNode, RuleName.MODIFY_COLUMN)) {
            // it`s not column definition, but can call this method
            Optional<ColumnDefinition> column = ExtractorUtils.extractColumnDefinition(each);
            if (column.isPresent()) {
                alterStatement.getUpdateColumns().put(column.get().getName(), column.get());
                postVisitColumnDefinition(each, statement, column.get().getName());
            }
        }
    }
    
    protected void postVisitColumnDefinition(final ParseTree ancestorNode, final SQLStatement statement, final String columnName) {
    }
}
