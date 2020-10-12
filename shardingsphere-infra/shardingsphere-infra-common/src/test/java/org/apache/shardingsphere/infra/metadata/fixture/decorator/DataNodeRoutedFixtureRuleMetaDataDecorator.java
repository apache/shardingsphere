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

package org.apache.shardingsphere.infra.metadata.fixture.decorator;

import org.apache.shardingsphere.infra.metadata.fixture.rule.DataNodeRoutedFixtureRule;
import org.apache.shardingsphere.infra.metadata.rule.spi.RuleMetaDataDecorator;
import org.apache.shardingsphere.infra.metadata.database.model.column.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.model.table.TableMetaData;

import java.util.Collections;

public final class DataNodeRoutedFixtureRuleMetaDataDecorator implements RuleMetaDataDecorator<DataNodeRoutedFixtureRule> {
    
    @Override
    public TableMetaData decorate(final String tableName, final TableMetaData tableMetaData, final DataNodeRoutedFixtureRule rule) {
        ColumnMetaData columnMetaData = new ColumnMetaData("id", 1, "INT", true, true, false);
        return new TableMetaData(Collections.singletonList(columnMetaData), Collections.emptyList());
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<DataNodeRoutedFixtureRule> getTypeClass() {
        return DataNodeRoutedFixtureRule.class;
    }
}
