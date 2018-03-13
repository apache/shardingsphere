package io.shardingjdbc.dbtest.config.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import io.shardingjdbc.dbtest.config.bean.parsecontext.ParseContexDefinition;
import lombok.Getter;

@Getter
@XmlAccessorType(XmlAccessType.FIELD)
public class AssertDefinition {

    @XmlAttribute(name = "id")
    private String id;

    @XmlAttribute(name = "init-data-file")
    private String initDataFile;

    @XmlAttribute(name = "expected-data-file")
    private String expectedDataFile;

    @XmlElement(name = "sql")
    private String sql;

    @XmlElement(name = "parameters")
    private ParametersDefinition parameters;

    @XmlElement(name = "parse-context")
    private ParseContexDefinition parseContex;

}
