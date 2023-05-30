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

package org.apache.shardingsphere.mask.algorithm.replace;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.mask.algorithm.MaskAlgorithmPropsChecker;
import org.apache.shardingsphere.mask.spi.MaskAlgorithm;

import java.security.SecureRandom;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Unified credit code random replace algorithm.
 */
public final class UnifiedCreditCodeRandomReplaceAlgorithm implements MaskAlgorithm<Object, String> {
    
    private static final String REGISTRATION_DEPARTMENT_CODES = "registration-department-codes";
    
    private static final String CATEGORY_CODES = "category-codes";
    
    private static final String ADMINISTRATIVE_DIVISION_CODES = "administrative-division-codes";
    
    private final Random random = new SecureRandom();
    
    private List<Character> registrationDepartmentCodes;
    
    private List<Character> categoryCodes;
    
    private List<String> administrativeDivisionCodes;
    
    @Getter
    private Properties props;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        this.registrationDepartmentCodes = createRegistrationDepartmentCodes(props);
        this.categoryCodes = createCategoryCodes(props);
        this.administrativeDivisionCodes = createAdministrativeDivisionCodes(props);
    }
    
    private List<Character> createRegistrationDepartmentCodes(final Properties props) {
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, REGISTRATION_DEPARTMENT_CODES, getType());
        return Splitter.on(",").trimResults().splitToList(props.getProperty(REGISTRATION_DEPARTMENT_CODES)).stream().map(each -> each.charAt(0)).collect(Collectors.toList());
    }
    
    private List<Character> createCategoryCodes(final Properties props) {
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, CATEGORY_CODES, getType());
        return Splitter.on(",").trimResults().splitToList(props.getProperty(CATEGORY_CODES)).stream().map(each -> each.charAt(0)).collect(Collectors.toList());
    }
    
    private List<String> createAdministrativeDivisionCodes(final Properties props) {
        MaskAlgorithmPropsChecker.checkAtLeastOneCharConfig(props, ADMINISTRATIVE_DIVISION_CODES, getType());
        return Splitter.on(",").trimResults().splitToList(props.getProperty(ADMINISTRATIVE_DIVISION_CODES));
    }
    
    @Override
    public String mask(final Object plainValue) {
        String result = null == plainValue ? null : String.valueOf(plainValue);
        if (Strings.isNullOrEmpty(result)) {
            return result;
        }
        return randomReplace();
    }
    
    private String randomReplace() {
        StringBuilder result = new StringBuilder();
        result.append(registrationDepartmentCodes.get(random.nextInt(registrationDepartmentCodes.size())))
                .append(categoryCodes.get(random.nextInt(categoryCodes.size())))
                .append(administrativeDivisionCodes.get(random.nextInt(administrativeDivisionCodes.size())));
        for (int i = 0; i < 10; i++) {
            result.append(Character.forDigit(random.nextInt(10), 10));
        }
        return result.toString();
    }
    
    @Override
    public String getType() {
        return "UNIFIED_CREDIT_CODE_RANDOM_REPLACE";
    }
}
