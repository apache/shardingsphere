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

package org.apache.shardingsphere.infra.algorithm.core.exception;

import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlgorithmDefinitionExceptionTest {
    
    @Test
    void assertConstruct() {
        SQLException actual = new AlgorithmDefinitionException(XOpenSQLState.GENERAL_ERROR, 1, "Fixture %s.", "foo_algorithm") {
            
            private static final long serialVersionUID = 8144975650775701945L;
        }.toSQLException();
        assertThat(actual.getSQLState(), is("HY000"));
        assertThat(actual.getErrorCode(), is(10401));
        assertThat(actual.getMessage(), is("Fixture foo_algorithm."));
    }
    
    @Test
    void assertConstructWithNegativeErrorCode() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new AlgorithmDefinitionException(XOpenSQLState.GENERAL_ERROR, -1, "Fixture %s.", "foo_algorithm") {
                    
                    private static final long serialVersionUID = 3816892882141106034L;
                });
        assertThat(actual.getMessage(), is("The value range of error code should be [0, 100)."));
    }
    
    @Test
    void assertConstructWithTooLargeErrorCode() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> new AlgorithmDefinitionException(XOpenSQLState.GENERAL_ERROR, 100, "Fixture %s.", "foo_algorithm") {
                    
                    private static final long serialVersionUID = 8007951987427130425L;
                });
        assertThat(actual.getMessage(), is("The value range of error code should be [0, 100)."));
    }
}
