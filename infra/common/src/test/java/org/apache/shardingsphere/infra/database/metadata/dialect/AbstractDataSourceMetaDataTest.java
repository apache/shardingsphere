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

package org.apache.shardingsphere.infra.database.metadata.dialect;

import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

abstract class AbstractDataSourceMetaDataTest {
    
    protected final void assertDataSourceMetaData(final String url, final String hostname, final int port, final String catalog, final String schema, final Properties queryProps) {
        DataSourceMetaData actual = createDataSourceMetaData(url);
        assertThat(actual.getHostname(), is(hostname));
        assertThat(actual.getPort(), is(port));
        assertThat(actual.getCatalog(), is(catalog));
        assertThat(actual.getSchema(), is(schema));
        assertThat(actual.getQueryProperties(), is(queryProps));
    }
    
    protected abstract DataSourceMetaData createDataSourceMetaData(String url);
}
