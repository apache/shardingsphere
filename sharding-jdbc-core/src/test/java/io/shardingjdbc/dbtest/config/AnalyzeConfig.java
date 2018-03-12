package io.shardingjdbc.dbtest.config;

import io.shardingjdbc.dbtest.config.bean.AssertsDefinition;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public static AssertsDefinition analyze(String path) throws IOException,  JAXBException {
        JAXBContext context = JAXBContext.newInstance(AssertsDefinition.class);

        Unmarshaller unmarshal = context.createUnmarshaller();
        FileReader reader = new FileReader(path) ;
        return (AssertsDefinition)unmarshal.unmarshal(reader);

    }

}
