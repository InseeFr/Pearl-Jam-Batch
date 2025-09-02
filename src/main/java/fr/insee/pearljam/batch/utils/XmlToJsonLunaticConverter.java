package fr.insee.pearljam.batch.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.lunatic.conversion.data.XMLLunaticDataToJSON;

import fr.insee.pearljam.batch.exception.TransformationException;
import fr.insee.pearljam.batch.sampleprocessing.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class XmlToJsonLunaticConverter {

    private final XMLLunaticDataToJSON xmlLunaticDataToJSON;
    private final ObjectMapper objectMapper;

    public XmlToJsonLunaticConverter() {
        this.xmlLunaticDataToJSON = new XMLLunaticDataToJSON();
        this.objectMapper = new ObjectMapper();
    }

    public ObjectNode convertInterrogationData(Data data) throws TransformationException {
        List<Element> dataElements = data.getDataElements();

        if (dataElements.isEmpty() ||
                (dataElements.size() == 1 && dataElements.getFirst().getNodeType() != Node.ELEMENT_NODE)) {
            return JsonNodeFactory.instance.objectNode();
        }

        try {
            String xmlContent = buildXmlFromElements(dataElements);
            try (
                    InputStream xmlInputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
                    OutputStream jsonOutputStream = xmlLunaticDataToJSON.transform(xmlInputStream)
            ) {
                byte[] jsonBytes = ((ByteArrayOutputStream) jsonOutputStream).toByteArray();
                return (ObjectNode) objectMapper.readTree(jsonBytes);
            }

        } catch (Exception e) {
            throw new TransformationException("Error when transforming XML data to JSON", e);
        }
    }

    private String buildXmlFromElements(List<Element> elements) throws TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement("Data");
            doc.appendChild(root);

            for (Element el : elements) {
                Node imported = doc.importNode(el, true);
                root.appendChild(imported);
            }

            TransformerFactory tfactory = TransformerFactory.newInstance();
            tfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            tfactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
            Transformer transformer = tfactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();

        } catch (ParserConfigurationException e) {
            throw new TransformerException("XML Builder config error", e);
        }
    }
}