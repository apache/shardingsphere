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

import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.DropPrimaryKeyExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.PrimaryKeyForAlterTableExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.RenameIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLAddColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLAddIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLChangeColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLDropIndexExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.segment.impl.dialect.mysql.MySQLModifyColumnExtractor;
import io.shardingsphere.core.parsing.antlr.extractor.statement.impl.ddl.AlterTableExtractor;

/**
 * Alter table statement extractor for MySQL.
 * 
 * @author duhongjun
 */
public final class MySQLAlterTableExtractor extends AlterTableExtractor {
    
    public MySQLAlterTableExtractor() {
        addSQLSegmentExtractor(new MySQLAddColumnExtractor());
        addSQLSegmentExtractor(new MySQLAddIndexExtractor());
        addSQLSegmentExtractor(new MySQLDropIndexExtractor());
        addSQLSegmentExtractor(new RenameIndexExtractor());
        addSQLSegmentExtractor(new PrimaryKeyForAlterTableExtractor());
        addSQLSegmentExtractor(new DropPrimaryKeyExtractor());
        addSQLSegmentExtractor(new MySQLChangeColumnExtractor());
        addSQLSegmentExtractor(new MySQLModifyColumnExtractor());
    }
}
