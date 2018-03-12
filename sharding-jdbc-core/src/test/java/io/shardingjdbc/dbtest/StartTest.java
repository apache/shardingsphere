package io.shardingjdbc.dbtest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import io.shardingjdbc.dbtest.asserts.AssertEngine;
import io.shardingjdbc.dbtest.common.ConfigRuntime;
import io.shardingjdbc.dbtest.common.FileUtils;
import io.shardingjdbc.dbtest.common.PathUtils;
import io.shardingjdbc.dbtest.init.InItCreateSchema;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

@RunWith(value = Parameterized.class)
public class StartTest {

    private String path;

    public StartTest(String path){
        this.path = path;
    }

    @Parameters
    public static Collection<String []> getParams(){

        String assertPath = ConfigRuntime.getAssertPath();
        assertPath = PathUtils.getPath(assertPath);
        //搜索所有用例
        List<String> paths = FileUtils.getAllFilePaths(new File(assertPath), "assert-", "xml");
        List<String[]> params = new ArrayList<>();
        for (String path1 : paths) {
            params.add(new String[]{path1});
        }
        return params;
    }

    @BeforeClass
    public static void beforeClass()  {
        if(ConfigRuntime.isInitialized()){
            InItCreateSchema.createDatabase();
            InItCreateSchema.initTable();
        }
    }

    @Test
    public void test()  {
        try {
            AssertEngine.runAssert(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public  static void afterClass()  {
        if(ConfigRuntime.isInitialized()){
            InItCreateSchema.dropDatabase();
        }
    }


}