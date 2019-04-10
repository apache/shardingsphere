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

package org.apache.shardingsphere.example.common.jpa.service;

import org.apache.shardingsphere.example.common.jpa.entity.CountryEntity;
import org.apache.shardingsphere.example.common.repository.CountryRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class JPACountryServiceImpl implements JPACountryService {

    @Resource
    private CountryRepository countryRepository;

    @Override
    public void initEnvironment() {
    }

    @Override
    public void cleanEnvironment() {
    }

    @Override
    public void processSuccess() {
        System.out.println("-------------- Process Success Begin ---------------");
        List<String> countryCodes = insertData();
        printData();
        deleteData(countryCodes);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    @Override
    public void processFailure() {
        System.out.println("-------------- Process Failure Begin ---------------");
        insertData();
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }

    @Override
    public void printData() {
        System.out.println("---------------------------- Print Country Data -------------------");
        for (Object each : countryRepository.selectAll()) {
            System.out.println(each);
        }
    }

    private void deleteData(final List<String> countryCodes) {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (String each: countryCodes) {
            countryRepository.delete(each);
        }
    }

    private List<String> insertData() {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<String> result = new ArrayList<>();
        Locale[] locales = Locale.getAvailableLocales();
        int i = 0;
        for (Locale l:locales) {
            final String country = l.getCountry();
            if (country == null || "".equals(country)) {
                continue;
            }
            CountryEntity currCountry = new CountryEntity();
            currCountry.setName(l.getDisplayCountry(l));
            currCountry.setLanguage(l.getLanguage());
            currCountry.setCode(l.getCountry());
            countryRepository.insert(currCountry);
            result.add(currCountry.getCode());
            if (++i == 10) {
                break;
            }
        }
        return result;
    }
}
