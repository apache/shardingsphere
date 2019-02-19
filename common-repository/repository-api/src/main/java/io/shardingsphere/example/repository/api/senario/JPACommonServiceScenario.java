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

package io.shardingsphere.example.repository.api.senario;

import io.shardingsphere.example.repository.api.service.CommonService;

public final class JPACommonServiceScenario implements Scenario {
    
    private final CommonService commonService;
    
    public JPACommonServiceScenario(final CommonService commonService) {
        this.commonService = commonService;
    }
    
    public CommonService getCommonService() {
        return commonService;
    }
    
    @Override
    public void process() {
        processSuccess();
        processFailure();
    }
    
    private void processSuccess() {
        try {
            commonService.processSuccess();
        } catch (final Exception ignore) {
        }
    }
    
    private void processFailure() {
        try {
            commonService.processFailure();
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
            printData();
        }
    }
    
    private void printData() {
        try {
            commonService.printData();
        } catch (final Exception ignore) {
        }
    }
}
