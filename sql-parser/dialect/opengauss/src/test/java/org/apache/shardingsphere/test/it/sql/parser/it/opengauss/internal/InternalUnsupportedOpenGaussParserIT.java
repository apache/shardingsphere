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

package org.apache.shardingsphere.test.it.sql.parser.it.opengauss.internal;

import org.apache.shardingsphere.test.it.sql.parser.internal.InternalUnsupportedSQLParserIT;
import org.apache.shardingsphere.test.it.sql.parser.internal.InternalSQLParserTestParameter;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

@RunWith(Parameterized.class)
public final class InternalUnsupportedOpenGaussParserIT extends InternalUnsupportedSQLParserIT {
    
    public InternalUnsupportedOpenGaussParserIT(final InternalSQLParserTestParameter testParam) {
        super(testParam);
    }
    
    @Parameters(name = "{0}")
    public static Collection<InternalSQLParserTestParameter> getTestParameters() {
        return getTestParameters("openGauss");
    }
}
