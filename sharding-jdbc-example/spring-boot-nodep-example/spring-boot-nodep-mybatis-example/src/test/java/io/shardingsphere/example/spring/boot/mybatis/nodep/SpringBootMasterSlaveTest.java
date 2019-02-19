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

package io.shardingsphere.example.spring.boot.mybatis.nodep;

import io.shardingsphere.example.repository.api.senario.AnnotationCommonServiceScenario;
import io.shardingsphere.example.repository.api.senario.AnnotationTractionServiceScenario;
import io.shardingsphere.example.repository.api.trace.SpringResultAssertUtils;
import io.shardingsphere.example.repository.mybatis.service.SpringPojoService;
import io.shardingsphere.example.repository.mybatis.service.SpringPojoTransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootTestMain.class)
@ActiveProfiles("master-slave")
public class SpringBootMasterSlaveTest {
    
    @Autowired
    private SpringPojoService commonService;
    
    @Autowired
    @Qualifier("jdbcTransactionService")
    private SpringPojoTransactionService transactionService;
    
    @Test
    public void assertCommonService() {
        AnnotationCommonServiceScenario scenario = new AnnotationCommonServiceScenario(commonService);
        scenario.process();
        SpringResultAssertUtils.assertMasterSlaveResult(commonService);
    }
    
    @Test
    public void assertTransactionService() {
        AnnotationTractionServiceScenario scenario = new AnnotationTractionServiceScenario(transactionService);
        scenario.process();
        SpringResultAssertUtils.assertTransactionServiceResult(transactionService);
    }
}
