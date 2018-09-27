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

package io.shardingsphere.core.parsing.antler.statement.visitor;

import io.shardingsphere.core.parsing.antler.phrase.visitor.AddColumnVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.DropColumnVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.RenameTableVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.TableNameVisitor;
import io.shardingsphere.core.parsing.antler.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;

public abstract class AlterTableVisitor extends AbstractStatementVisitor {

    public AlterTableVisitor() {
        addVisitor(new TableNameVisitor()); 
        addVisitor(new RenameTableVisitor());
        addVisitor(new AddColumnVisitor());
        addVisitor(new DropColumnVisitor());
    }
    
    @Override
    protected SQLStatement newStatement() {
        return new AlterTableStatement();
    }
}
