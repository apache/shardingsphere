package io.shardingjdbc.dbtest.config;

import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.xml.sax.SAXException;

import io.shardingjdbc.dbtest.config.bean.AssertsDefinition;

/**
 *
 */
public class AnalyzeConfig {

	/**
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public static AssertsDefinition analyze(String path) throws IOException, JAXBException {
		JAXBContext context = JAXBContext.newInstance(AssertsDefinition.class);

		Unmarshaller unmarshal = context.createUnmarshaller();
		FileReader reader = new FileReader(path);
		return (AssertsDefinition) unmarshal.unmarshal(reader);

	}

}
