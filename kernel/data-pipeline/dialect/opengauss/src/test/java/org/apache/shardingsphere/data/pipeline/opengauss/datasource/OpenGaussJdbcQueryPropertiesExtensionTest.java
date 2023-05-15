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

package org.apache.shardingsphere.data.pipeline.opengauss.datasource;

import org.apache.shardingsphere.data.pipeline.spi.datasource.JdbcQueryPropertiesExtension;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenGaussJdbcQueryPropertiesExtensionTest {
    
    @Test
    void assertExtendQueryProperties() {
        Optional<JdbcQueryPropertiesExtension> extension = TypedSPILoader.findService(JdbcQueryPropertiesExtension.class, "openGauss");
        assertTrue(extension.isPresent());
        assertExtension(extension.get());
    }
    
    private void assertExtension(final JdbcQueryPropertiesExtension actual) {
        assertThat(actual, instanceOf(OpenGaussJdbcQueryPropertiesExtension.class));
        assertThat(actual.getType(), equalTo("openGauss"));
        assertThat(actual.extendQueryProperties().size(), equalTo(2));
    }
}
