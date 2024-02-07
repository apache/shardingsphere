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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableItemConstants;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowStatusFromReadwriteSplittingRulesExecutorTest {
    
    @Test
    void assertGetRowData() {
        ShowStatusFromReadwriteSplittingRulesExecutor executor = new ShowStatusFromReadwriteSplittingRulesExecutor();
        executor.setDatabase(mockDatabase());
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowStatusFromReadwriteSplittingRulesStatement.class), mock(ContextManager.class, RETURNS_DEEP_STUBS));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("read_ds_0"));
        assertThat(row.getCell(2), is("ENABLED"));
        row = iterator.next();
        assertThat(row.getCell(1), is("read_ds_1"));
        assertThat(row.getCell(2), is("ENABLED"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ReadwriteSplittingRule readwriteSplittingRule = mock(ReadwriteSplittingRule.class);
        when(readwriteSplittingRule.getExportData()).thenReturn(mockExportData());
        when(result.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singletonList(readwriteSplittingRule));
        return result;
    }
    
    private Map<String, Object> mockExportData() {
        return Collections.singletonMap(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, exportDataSources());
    }
    
    private Map<String, Map<String, String>> exportDataSources() {
        Map<String, String> exportedDataSources = new LinkedHashMap<>(2, 1F);
        exportedDataSources.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, "write_ds");
        exportedDataSources.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, "read_ds_0,read_ds_1");
        return Collections.singletonMap("readwrite_ds", exportedDataSources);
    }
}
