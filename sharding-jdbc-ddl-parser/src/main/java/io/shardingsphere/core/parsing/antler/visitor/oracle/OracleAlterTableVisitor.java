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

package io.shardingsphere.core.parsing.antler.visitor.oracle;

import io.shardingsphere.core.parsing.antler.phrase.visitor.AddColumnVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.AddPrimaryKeyVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.RenameColumnVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.oracle.OracleDropPrimaryKeyVisitor;
import io.shardingsphere.core.parsing.antler.phrase.visitor.oracle.OracleModifyColumnVisitor;
import io.shardingsphere.core.parsing.antler.statement.visitor.AlterTableVisitor;

public class OracleAlterTableVisitor extends AlterTableVisitor {
    public OracleAlterTableVisitor() {
        addVisitor(new AddColumnVisitor());
        addVisitor(new OracleModifyColumnVisitor());
        addVisitor(new RenameColumnVisitor());
        addVisitor(new AddPrimaryKeyVisitor("addConstraintClause"));
        addVisitor(new OracleDropPrimaryKeyVisitor());
    }
}
