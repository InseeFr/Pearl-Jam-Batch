package fr.insee.pearljam.batch.communication;


import javax.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
public class Variables {

    // Classic attributes
    @XmlElement(name = "NumeroDocument")
    private String numeroDocument;
    @XmlElement(name = "BddIdentifiantUniteEnquetee")
    private String bddIdentifiantUniteEnquetee;
    @XmlElement(name = "CodePostalDestinataire")
    private String codePostalDestinataire;
    @XmlElement(name = "BddAdressePosteeL1", defaultValue="")
    private String bddAdressePosteeL1;
    @XmlElement(name = "BddAdressePosteeL2", defaultValue="")
    private String bddAdressePosteeL2;
    @XmlElement(name = "BddAdressePosteeL3", defaultValue="")
    private String bddAdressePosteeL3;
    @XmlElement(name = "BddAdressePosteeL4", defaultValue="")
    private String bddAdressePosteeL4;
    @XmlElement(name = "BddAdressePosteeL5", defaultValue="")
    private String bddAdressePosteeL5;
    @XmlElement(name = "BddAdressePosteeL6", defaultValue="")
    private String bddAdressePosteeL6;
    @XmlElement(name = "BddAdressePosteeL7", defaultValue="")
    private String bddAdressePosteeL7;

    @XmlElement(name = "Barcode")
    private String barcode;

    @XmlElement(name = "InitAccuseReception")
    private String initAccuseReception;

    // Dynamic key-value pairs, for construction needs, not to be XMLed
    @XmlTransient
    private Map<String, String> additionalFields = new HashMap<>();

    // XML projection of additional fields
    @XmlAnyElement(lax = true)
    List<Element> dynamicElements = new ArrayList<>();

 
    public void addAdditionalField(String key, String value) {
        // trim key to prevent XML generation error
        this.additionalFields.put(key.trim(), value);
    }
}
