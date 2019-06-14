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

package org.apache.shardingsphere.core.parse.filler.encrypt.dml.insert;

import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.aware.EncryptRuleAware;
import org.apache.shardingsphere.core.parse.aware.ShardingTableMetaDataAware;
import org.apache.shardingsphere.core.parse.filler.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.InsertValuesSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Collection;

/**
 * Insert values filler for encrypt.
 *
 * @author zhangliang
 * @author panjuan
 */
@Setter
public final class EncryptInsertValuesFiller implements SQLSegmentFiller<InsertValuesSegment>, EncryptRuleAware, ShardingTableMetaDataAware {
    
    private EncryptRule encryptRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final InsertValuesSegment sqlSegment, final SQLStatement sqlStatement) {
        if (((InsertStatement) sqlStatement).getColumnNames().isEmpty()) {
            fillColumnsFromMetaData((InsertStatement) sqlStatement);
        }
        fillValues(sqlSegment, (InsertStatement) sqlStatement);
    }
    
    private void fillColumnsFromMetaData(final InsertStatement insertStatement) {
        Collection<String> assistedQueryColumns = encryptRule.getEncryptorEngine().getAssistedQueryColumns(insertStatement.getTables().getSingleTableName());
        for (String each : shardingTableMetaData.getAllColumnNames(insertStatement.getTables().getSingleTableName())) {
            if (!assistedQueryColumns.contains(each)) {
                insertStatement.getColumnNames().add(each);
            }
        }
    }
    
    private void fillValues(final InsertValuesSegment sqlSegment, final InsertStatement insertStatement) {
        insertStatement.getValues().add(new InsertValue(sqlSegment.getValues()));
    }
}
