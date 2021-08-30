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

package org.apache.shardingsphere.shadow.rule.checker;

import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.algorithm.shadow.note.SimpleSQLNoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

public final class ShadowTableRuleCheckerTest {
    
    @Test
    public void assertCheckTableShadowAlgorithmsPass() {
        Collection<String> tableShadowAlgorithmNames = createTableShadowAlgorithmNames();
        Map<String, ShadowAlgorithm> shadowAlgorithms = createShadowAlgorithms(tableShadowAlgorithmNames);
        ShadowTableRuleChecker.checkTableShadowAlgorithms("t_user", tableShadowAlgorithmNames, shadowAlgorithms);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertCheckTableShadowAlgorithmsFail() {
        Collection<String> tableShadowAlgorithmNames = createTableShadowAlgorithmNames();
        tableShadowAlgorithmNames.add("order-id-insert-regex-algorithm");
        Map<String, ShadowAlgorithm> shadowAlgorithms = createShadowAlgorithms(tableShadowAlgorithmNames);
        ShadowTableRuleChecker.checkTableShadowAlgorithms("t_user", tableShadowAlgorithmNames, shadowAlgorithms);
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms(final Collection<String> tableShadowAlgorithmNames) {
        Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        for (String each : tableShadowAlgorithmNames) {
            switch (each) {
                case "user-id-insert-regex-algorithm":
                    result.put(each, createColumnShadowAlgorithm("user_id", "insert"));
                    break;
                case "user-id-update-regex-algorithm":
                    result.put(each, createColumnShadowAlgorithm("user_id", "update"));
                    break;
                case "order-id-insert-regex-algorithm":
                    result.put(each, createColumnShadowAlgorithm("order_id", "insert"));
                    break;
                case "simple_note-algorithm":
                    result.put(each, createNoteShadowAlgorithm());
                    break;
                default:
                    break;
            }
        }
        return result;
    }
    
    private ShadowAlgorithm createNoteShadowAlgorithm() {
        SimpleSQLNoteShadowAlgorithm simpleSQLNoteShadowAlgorithm = new SimpleSQLNoteShadowAlgorithm();
        simpleSQLNoteShadowAlgorithm.setProps(createNoteProperties());
        simpleSQLNoteShadowAlgorithm.init();
        return simpleSQLNoteShadowAlgorithm;
    }
    
    private Properties createNoteProperties() {
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        return properties;
    }
    
    private ShadowAlgorithm createColumnShadowAlgorithm(final String column, final String operation) {
        ColumnRegexMatchShadowAlgorithm columnRegexMatchShadowAlgorithm = new ColumnRegexMatchShadowAlgorithm();
        columnRegexMatchShadowAlgorithm.setProps(createColumnProperties(column, operation));
        columnRegexMatchShadowAlgorithm.init();
        return columnRegexMatchShadowAlgorithm;
    }
    
    private Properties createColumnProperties(final String column, final String operation) {
        Properties properties = new Properties();
        properties.setProperty("column", column);
        properties.setProperty("operation", operation);
        properties.setProperty("regex", "[1]");
        return properties;
    }
    
    private Collection<String> createTableShadowAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        result.add("user-id-insert-regex-algorithm");
        result.add("user-id-update-regex-algorithm");
        result.add("simple_note-algorithm");
        return result;
    }
}
