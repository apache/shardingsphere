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

package io.shardingsphere.core.parsing.integrate.asserts.meta;

import com.google.common.base.Joiner;
import io.shardingsphere.core.parsing.integrate.asserts.SQLStatementAssertMessage;
import io.shardingsphere.core.parsing.integrate.jaxb.meta.ExpectedTableMetaData;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Table meta data assert.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class TableMetaDataAssert {
    
    private final SQLStatementAssertMessage assertMessage;
    
    /**
     * Assert table meta data.
     * 
     * @param actualColumnNames actual column names
     * @param actualColumnTypes actual column types
     * @param actualPrimaryKeyColumns actual primary key columns
     * @param expected expected meta data
     */
    public void assertMeta(final List<String> actualColumnNames, final List<String> actualColumnTypes, final List<String> actualPrimaryKeyColumns, final ExpectedTableMetaData expected) {
        assertFalse(assertMessage.getFullAssertMessage("Column names should exist: "), actualColumnNames.isEmpty());
        assertThat(assertMessage.getFullAssertMessage("Column names assertion error: "), Joiner.on(",").join(actualColumnNames), is(expected.getColumnNames()));
        assertFalse(assertMessage.getFullAssertMessage("Column types should exist: "), actualColumnTypes.isEmpty());
        assertThat(assertMessage.getFullAssertMessage("Column types assertion error: "), Joiner.on(",").join(actualColumnTypes), is(expected.getColumnTypes()));
        assertThat(assertMessage.getFullAssertMessage("Column primary key columns assertion error: "), Joiner.on(",").join(actualPrimaryKeyColumns), is(expected.getPrimaryKeyColumns()));
    }
}
