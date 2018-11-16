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

import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import lombok.Getter;

/**
 * Table join extract result.
 * 
 * @author duhongjun
 */
@Getter
public class TableJoinExtractResult extends TableExtractResult {
    
    public TableJoinExtractResult(String name, String alias, String schemaName, TableToken token) {
        super(name, alias, schemaName, token);
    }
    
    public TableJoinExtractResult(TableExtractResult parent) {
        super(parent.getName(), parent.getAlias(), parent.getSchemaName(), parent.getToken());
    }

    private final List<OrCondition> joinConditions = new LinkedList<>();
    
}
