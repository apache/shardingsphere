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

package io.shardingsphere.core.parsing.antlr.extractor.statement.dialect.oracle;

import io.shardingsphere.core.parsing.antlr.extractor.phrase.AddColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.phrase.AddPrimaryKeyExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.phrase.RenameColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.phrase.RuleName;
import io.shardingsphere.core.parsing.antlr.extractor.phrase.dialect.oracle.OracleDropPrimaryKeyExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.phrase.dialect.oracle.OracleModifyColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.AlterTableExtractor;

/**
 * Oracle alter table statement extractor.
 * 
 * @author duhongjun
 */
public final class OracleAlterTableExtractor extends AlterTableExtractor {
    
    public OracleAlterTableExtractor() {
        addPhraseExtractor(new AddColumnExtractor());
        addPhraseExtractor(new OracleModifyColumnExtractor());
        addPhraseExtractor(new RenameColumnExtractor());
        addPhraseExtractor(new AddPrimaryKeyExtractor(RuleName.ADD_CONSTRAINT_CLAUSE));
        addPhraseExtractor(new OracleDropPrimaryKeyExtractor());
    }
}
