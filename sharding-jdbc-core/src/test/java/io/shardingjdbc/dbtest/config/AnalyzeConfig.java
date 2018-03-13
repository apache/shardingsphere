package io.shardingjdbc.dbtest.config;

import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import io.shardingjdbc.dbtest.config.bean.AssertsDefinition;

public class AnalyzeConfig {

	public static AssertsDefinition analyze(final String path) throws IOException, JAXBException {
		JAXBContext context = JAXBContext.newInstance(AssertsDefinition.class);

		Unmarshaller unmarshal = context.createUnmarshaller();
		FileReader reader = new FileReader(path);
		return (AssertsDefinition) unmarshal.unmarshal(reader);

	}

}
