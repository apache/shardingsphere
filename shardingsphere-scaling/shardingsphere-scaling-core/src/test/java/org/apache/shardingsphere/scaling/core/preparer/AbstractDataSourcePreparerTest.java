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

package org.apache.shardingsphere.scaling.core.preparer;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.preparer.AbstractDataSourcePreparer;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public final class AbstractDataSourcePreparerTest {
    
    private static final Pattern PATTERN_CREATE_TABLE_IF_NOT_EXISTS = Pattern.compile("CREATE\\s+TABLE\\s+IF\\s+NOT\\s+EXISTS\\s+", Pattern.CASE_INSENSITIVE);
    
    @Test
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertAddIfNotExistsForCreateTableSQL() {
        Method method = AbstractDataSourcePreparer.class.getDeclaredMethod("addIfNotExistsForCreateTableSQL", String.class);
        method.setAccessible(true);
        AbstractDataSourcePreparer preparer = new AbstractDataSourcePreparer() {
            @Override
            public void prepareTargetTables(final JobConfiguration jobConfig) {
            }
        };
        List<String> createTableSQLs = Arrays.asList("CREATE TABLE IF NOT EXISTS t (id int)", "CREATE TABLE t (id int)",
                "CREATE  TABLE IF \nNOT \tEXISTS t (id int)", "CREATE \tTABLE t (id int)");
        for (String createTableSQL : createTableSQLs) {
            String sql = (String) method.invoke(preparer, createTableSQL);
            assertTrue(PATTERN_CREATE_TABLE_IF_NOT_EXISTS.matcher(sql).find());
        }
    }
}
