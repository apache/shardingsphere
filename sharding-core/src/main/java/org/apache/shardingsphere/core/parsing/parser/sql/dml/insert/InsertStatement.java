/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parsing.parser.sql.dml.insert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValues;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.DMLStatement;
import org.apache.shardingsphere.core.parsing.parser.token.ItemsToken;
import org.apache.shardingsphere.core.parsing.parser.token.SQLToken;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Insert statement.
 *
 * @author zhangliang
 * @author maxiaoguang
 */
@Getter
@Setter
@ToString(callSuper = true)
public final class InsertStatement extends DMLStatement {
    
    private final List<Column> columns = new LinkedList<>();
    
    private List<GeneratedKeyCondition> generatedKeyConditions = new LinkedList<>();
    
    private final InsertValues insertValues = new InsertValues();
    
    private int columnsListLastPosition;
    
    private int generateKeyColumnIndex = -1;
    
    private int insertValuesListLastPosition;
    
    private boolean containGenerateKey;
    
    /**
     * Get items tokens.
     *
     * @return items token list.
     */
    public List<ItemsToken> getItemsTokens() {
        List<ItemsToken> result = new ArrayList<>();
        for (SQLToken each : getSQLTokens()) {
            if (each instanceof ItemsToken) {
                result.add((ItemsToken) each);
            }
        }
        return result;
    }
}
