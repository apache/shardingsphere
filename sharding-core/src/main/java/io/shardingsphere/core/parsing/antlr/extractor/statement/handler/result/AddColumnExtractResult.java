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


package io.shardingsphere.core.parsing.antlr.extractor.statement.handler.result;

import java.util.LinkedList;
import java.util.List;

import io.shardingsphere.core.parsing.antlr.sql.ddl.AlterTableStatement;
import io.shardingsphere.core.parsing.antlr.sql.ddl.ColumnDefinition;
import io.shardingsphere.core.parsing.antlr.sql.ddl.mysql.MySQLAlterTableStatement;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import lombok.Getter;
import lombok.Setter;

/**
 * Add column result.
 * 
 * @author duhongjun
 */
@Getter
@Setter
public class AddColumnExtractResult implements ExtractResult {
    
    private List<ColumnDefinition> columnDefintions = new LinkedList<>();
    
    @Override
    public void inject(final SQLStatement statement) {
        AlterTableStatement alterTableStatement = (AlterTableStatement)statement;
        for(ColumnDefinition each : columnDefintions) {
            if (!alterTableStatement.findColumnDefinition(each.getName()).isPresent()) {
                alterTableStatement.getAddColumns().add(each);
                
                if(null != each.getPosition()) {
                    MySQLAlterTableStatement mysqlAlterTable = (MySQLAlterTableStatement) statement;
                    mysqlAlterTable.getPositionChangedColumns().add(each.getPosition());
                }
            }
        }
    }

}
