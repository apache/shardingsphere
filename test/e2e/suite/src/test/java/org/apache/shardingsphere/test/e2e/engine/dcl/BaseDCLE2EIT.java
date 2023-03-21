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

package org.apache.shardingsphere.test.e2e.engine.dcl;

import org.apache.shardingsphere.test.e2e.engine.SingleE2EITContainerComposer;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.authority.AuthorityEnvironmentManager;
import org.apache.shardingsphere.test.e2e.env.runtime.scenario.path.ScenarioCommonPath;
import org.apache.shardingsphere.test.e2e.framework.param.model.AssertionTestParameter;
import org.junit.jupiter.api.AfterEach;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.sql.SQLException;

public abstract class BaseDCLE2EIT {
    
    private AuthorityEnvironmentManager authorityEnvironmentManager;
    
    /**
     * Init.
     * 
     * @param testParam test parameter
     * @param containerComposer container composer
     * @throws JAXBException JAXB exception
     * @throws IOException IO exception
     * @throws SQLException SQL exception
     */
    public final void init(final AssertionTestParameter testParam, final SingleE2EITContainerComposer containerComposer) throws JAXBException, IOException, SQLException {
        authorityEnvironmentManager = new AuthorityEnvironmentManager(
                new ScenarioCommonPath(testParam.getScenario()).getAuthorityFile(), containerComposer.getContainerComposer().getActualDataSourceMap(), testParam.getDatabaseType());
        authorityEnvironmentManager.initialize();
    }
    
    @AfterEach
    public final void tearDown() throws Exception {
        // TODO make sure DCL test case can not be null
        if (null != authorityEnvironmentManager) {
            authorityEnvironmentManager.clean();
        }
    }
}
