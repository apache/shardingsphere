package io.shardingjdbc.dbtest.config.bean;

import io.shardingjdbc.dbtest.config.bean.parseContext.ParseContexDefinition;
import lombok.Data;
import lombok.Getter;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

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
