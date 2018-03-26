/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.console.controller;

import io.shardingjdbc.console.domain.SqlResponseResult;
import io.shardingjdbc.console.service.SqlService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * SqlController.
 *
 * @author zhangyonglun
 */
@RestController
public class SqlController {
    
    @Autowired
    private SqlService sqlService;
    
    /**
     * execute SQL.
     *
     * @param sql sql string
      @param servletRequest servlet request
     * @return sql response result
     */
    @RequestMapping(value = "/sql", method = RequestMethod.POST)
    public SqlResponseResult executeSQL(@RequestBody final String sql, final ServletRequest servletRequest) {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpSession httpSession = httpRequest.getSession();
        return sqlService.execute(sql, httpSession);
    }
}
