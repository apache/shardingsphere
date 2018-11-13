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

import com.google.common.base.Optional;

import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.context.table.Table;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingsphere.core.parsing.parser.token.IndexToken;
import io.shardingsphere.core.parsing.parser.token.SQLToken;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.util.SQLUtil;
import lombok.Getter;

/**
 * SQL Tokens extract handler.
 * 
 * @author duhongjun
 */
@Getter
public class SQLTokenExtractResult implements ExtractResult {
    private List<SQLToken> sqlTokens = new LinkedList<>();
    
    @Override
    public void inject(SQLStatement statement) {
        String tableName = "";
        if(statement instanceof DDLStatement) {
            DDLStatement ddlStatement = (DDLStatement) statement;
            tableName = ddlStatement.getTables().isEmpty() ? "" : ddlStatement.getTables().getSingleTableName();
        }
        for(SQLToken each : sqlTokens) {
            if(each instanceof IndexToken) {
                IndexToken indexToken = (IndexToken) each;
                if(null == indexToken.getTableName()) {
                    indexToken.setTableName(tableName);
                }
            }else if(each instanceof TableToken) {
                statement.getTables().add(new Table(( (TableToken) each).getTableName(), Optional.<String>absent()));
            }
            statement.getSQLTokens().add(each);
        }
    }
}
