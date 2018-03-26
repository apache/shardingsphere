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

package io.shardingjdbc.console;

import io.shardingjdbc.console.entity.DBConnector;
import io.shardingjdbc.console.entity.UserSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Connection;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MockServletContext.class)
@WebAppConfiguration
public class SelectControllerTests {

    private MockMvc mvc;
    //    @Test
//    public void encdes() {
//        try {
//            EncDes de1 = new EncDes();
//            String msg = "id=aa;pwd=33;";
//            byte[] encontent = de1.Encrytor(msg);
//            byte[] decontent = de1.Decryptor(encontent);
//        } catch (Exception e){
//        }
//
//
//   }
    @Test
    public  void conn(){
        UserSession userInfo = new UserSession("dev_user", "dev_ing_123", "172.25.63.243:3306/test");
        Connection conn = DBConnector.getConnection(userInfo.getUserName(),userInfo.getPassWord(),
                userInfo.getTargetURL(),userInfo.getDriver());


    }
//    @Test
//    public void res(){
//        RespCode aa = RespCode.SUCCESS;
//
//    }

//    @Before
//    public void setUp() {
//        mvc = MockMvcBuilders.standaloneSetup(new SelectController()).build();
//    }

//    @Test
//    public void assertHelloWorld() throws Exception {
//        mvc.perform(MockMvcRequestBuilders.get("/select").accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().string(equalTo("Hello World!")));
//    }
}
