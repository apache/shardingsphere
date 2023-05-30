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

package org.apache.shardingsphere.test.e2e.framework.param.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.test.e2e.cases.IntegrationTestCaseContext;
import org.apache.shardingsphere.test.e2e.cases.SQLCommandType;

/**
 * Case test parameter.
 */
@RequiredArgsConstructor
@Getter
public final class CaseTestParameter implements E2ETestParameter {
    
    private final IntegrationTestCaseContext testCaseContext;
    
    private final String adapter;
    
    private final String scenario;
    
    private final String mode;
    
    private final DatabaseType databaseType;
    
    private final SQLCommandType sqlCommandType;
    
    @Override
    public String toString() {
        return String.format("%s: %s -> %s -> %s", adapter, scenario, databaseType.getType(), testCaseContext.getTestCase().getSql());
    }
}
