package io.shardingjdbc.dbtest;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import io.shardingjdbc.dbtest.asserts.AssertEngine;
import io.shardingjdbc.dbtest.common.ConfigRuntime;
import io.shardingjdbc.dbtest.common.FileUtils;
import io.shardingjdbc.dbtest.common.PathUtils;
import io.shardingjdbc.dbtest.init.InItCreateSchema;

@RunWith(value = Parameterized.class)
public class StartTest {

	private String path;

	public StartTest(String path) {
		this.path = path;
	}

	@Parameters
	public static Collection<String[]> getParams() {

		String assertPath = ConfigRuntime.getAssertPath();
		assertPath = PathUtils.getPath(assertPath);
		List<String> paths = FileUtils.getAllFilePaths(new File(assertPath), "assert-", "xml");
		List<String[]> result = new ArrayList<>();
		for (String each : paths) {
			result.add(new String[] { each });
		}
		return result;
	}

	@BeforeClass
	public static void beforeClass() {
		if (ConfigRuntime.isInitialized()) {
			InItCreateSchema.createDatabase();
			InItCreateSchema.initTable();
		}
	}

	@Test
	public void test() {
		try {
			AssertEngine.runAssert(path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void afterClass() {
		if (ConfigRuntime.isInitialized()) {
			InItCreateSchema.dropDatabase();
		}
	}

}