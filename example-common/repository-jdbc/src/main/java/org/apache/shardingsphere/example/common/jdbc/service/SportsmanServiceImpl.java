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

package org.apache.shardingsphere.example.common.jdbc.service;

import org.apache.shardingsphere.example.common.entity.Country;
import org.apache.shardingsphere.example.common.entity.Sportsman;
import org.apache.shardingsphere.example.common.repository.CountryRepository;
import org.apache.shardingsphere.example.common.repository.SportsmanRepository;
import org.apache.shardingsphere.example.common.service.CommonService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SportsmanServiceImpl implements CommonService {

    private final CountryRepository countryRepository;

    private final SportsmanRepository sportsmanRepository;

    public SportsmanServiceImpl(final CountryRepository countryRepository, final SportsmanRepository sportsmanRepository) {
        this.countryRepository = countryRepository;
        this.sportsmanRepository = sportsmanRepository;
    }

    @Override
    public void initEnvironment() {
        countryRepository.createTableIfNotExists();
        sportsmanRepository.createTableIfNotExists();
        countryRepository.truncateTable();
        sportsmanRepository.truncateTable();
    }

    @Override
    public void cleanEnvironment() {
        countryRepository.dropTable();
        sportsmanRepository.dropTable();
    }

    @Override
    public void processSuccess() {
        System.out.println("-------------- Process Success Begin ---------------");
        InsertResult insertResult = insertData();
        printData();
        deleteData(insertResult);
        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    private void deleteData(final InsertResult insertResult) {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (String each : insertResult.getCountryCodes()) {
            countryRepository.delete(each);
        }
        for (Long each : insertResult.getSportsmanIds()) {
            sportsmanRepository.delete(each);
        }
    }

    @Override
    public void processFailure() {
        System.out.println("-------------- Process Failure Begin ---------------");
        insertData();
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }

    private InsertResult insertData() {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<String> countryCodes = insertCountry();
        List<Long> sportsmanIds = insertSportman(countryCodes);
        return new InsertResult(countryCodes, sportsmanIds);
    }

    private List<Long> insertSportman(final List<String> countryCodes) {
        List<Long> result = new ArrayList<>(countryCodes.size());
        int index = 0;
        for (String countryCode : countryCodes) {
            Sportsman sportsman = new Sportsman();
            sportsman.setName("test_" + ++index);
            sportsman.setCountryCode(countryCode);
            sportsmanRepository.insert(sportsman);
            result.add(sportsman.getId());
        }
        return result;
    }

    private List<String> insertCountry() {
        Set<String> result = new LinkedHashSet<>();
        for (Locale each : Locale.getAvailableLocales()) {
            if (result.contains(each.getCountry()) || each.getCountry().isEmpty()) {
                continue;
            }
            if (result.size() >= 20) {
                break;
            }
            result.add(each.getCountry());
            Country entity = new Country();
            entity.setName(each.getDisplayCountry(each));
            entity.setLanguage(each.getLanguage());
            entity.setCode(each.getCountry());
            countryRepository.insert(entity);
        }
        return new ArrayList<>(result);
    }

    @Override
    public void printData() {
        System.out.println("---------------------------- Print Country Data -------------------");
        for (Object each : countryRepository.selectAll()) {
            System.out.println(each);
        }
        System.out.println("---------------------------- Print Sportman Data -----------------------");
        for (Object each : sportsmanRepository.selectAll()) {
            System.out.println(each);
        }
    }

    private class InsertResult {

        private final List<String> countryCodes;

        private final List<Long> sportsmanIds;

        InsertResult(final List<String> countryCodes, final List<Long> sportsmanIds) {
            this.countryCodes = countryCodes;
            this.sportsmanIds = sportsmanIds;
        }

        public List<Long> getSportsmanIds() {
            return sportsmanIds;
        }

        public List<String> getCountryCodes() {
            return countryCodes;
        }
    }
}
