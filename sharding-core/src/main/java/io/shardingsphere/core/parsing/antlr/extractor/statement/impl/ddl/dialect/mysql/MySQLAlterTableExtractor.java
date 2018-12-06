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

package io.shardingsphere.core.parsing.antlr.extractor.statement.impl.ddl.dialect.mysql;

import io.shardingsphere.core.parsing.antlr.extractor.segment.SQLSegmentExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.DropColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.DropPrimaryKeyExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.PrimaryKeyForAlterTableExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.RenameIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.RenameTableExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.TableNamesExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLAddColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLAddIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLChangeColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLDropIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLModifyColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.SQLStatementExtractor;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Alter table extractor for MySQL.
 * 
 * @author duhongjun
 * @author zhangliang
 */
public final class MySQLAlterTableExtractor implements SQLStatementExtractor {
    
    private static final Collection<SQLSegmentExtractor> EXTRACTORS = new LinkedList<>();
    
    static {
        EXTRACTORS.add(new TableNamesExtractor());
        EXTRACTORS.add(new RenameTableExtractor());
        EXTRACTORS.add(new DropColumnExtractor());
        EXTRACTORS.add(new MySQLAddColumnExtractor());
        EXTRACTORS.add(new MySQLAddIndexExtractor());
        EXTRACTORS.add(new MySQLDropIndexExtractor());
        EXTRACTORS.add(new RenameIndexExtractor());
        EXTRACTORS.add(new PrimaryKeyForAlterTableExtractor());
        EXTRACTORS.add(new DropPrimaryKeyExtractor());
        EXTRACTORS.add(new MySQLChangeColumnExtractor());
        EXTRACTORS.add(new MySQLModifyColumnExtractor());
    }
    
    @Override
    public Collection<SQLSegmentExtractor> getExtractors() {
        return EXTRACTORS;
    }
}
