/**
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.example.config.service;

import com.dangdang.ddframe.rdb.sharding.example.config.repository.FooRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class FooServiceImpl implements FooService {
    
    @Resource
    private FooRepository fooRepository;
    
    @Override
    public void insert() {
        try {
            fooRepository.insert();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void delete() {
        fooRepository.delete();
    }
    
    @Override
    public void select() {
        fooRepository.select();
    }
}
