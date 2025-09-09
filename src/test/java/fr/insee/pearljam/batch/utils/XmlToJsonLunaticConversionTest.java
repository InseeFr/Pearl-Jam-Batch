package fr.insee.pearljam.batch.utils;

import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.insee.pearljam.batch.exception.TransformationException;
import fr.insee.pearljam.batch.sampleprocessing.Data;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@RequiredArgsConstructor
class XmlToJsonLunaticConversionTest {

    private Document doc;

    private XmlToJsonLunaticConverter converter;

    @BeforeEach
    void init() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.newDocument();
        converter = new XmlToJsonLunaticConverter();
    }

    @Test
    @DisplayName("should return empty ObjectNode when data elements are empty")
    void shouldReturnEmptyJsonWhenNodeIsNull() throws TransformationException {
        // Given
        Data data = new Data();

        // When
        ObjectNode result = converter.convertInterrogationData(data);

        // Then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("should throw TransformationException on XML processing error")
    void shouldThrowExceptionOnXmlProcessingError() throws ParserConfigurationException {
        // Given
        Data data = new Data();
        Element child = doc.createElement("plop");
        data.getDataElements().add(child);

        // force DocumentBuilder to throw
        try (MockedStatic<DocumentBuilderFactory> factoryMock = mockStatic(DocumentBuilderFactory.class)) {
            DocumentBuilderFactory mockFactory = mock(DocumentBuilderFactory.class);
            factoryMock.when(DocumentBuilderFactory::newInstance).thenReturn(mockFactory);
            when(mockFactory.newDocumentBuilder()).thenThrow(new RuntimeException("fail"));

            // When / Then
            assertThatThrownBy(() -> converter.convertInterrogationData(data))
                    .isInstanceOf(TransformationException.class)
                    .hasMessageContaining("Error when transforming XML data to JSON");
        }
    }

    @Test
    @DisplayName("should convert an XML file into ObjectNode successfully")
    void shouldConvertXmlFileToJson() throws Exception {
        // Given
        Element dataElement = createElement("src/test/resources/lunatic-conversion/valid-data.xml");
        Data data = new Data();
        List<Element> elements = IntStream.range(0, dataElement.getChildNodes().getLength())
                .mapToObj(i -> dataElement.getChildNodes().item(i))
                .filter(n -> n.getNodeType() == Node.ELEMENT_NODE)
                .map(n -> (Element) n)
                .toList();
        data.getDataElements().addAll(elements);

        // When
        ObjectNode result = converter.convertInterrogationData(data);

        // Then
        String expectedJson = """
                {
                    "COLLECTED":{},
                    "EXTERNAL":{
                        "REPETITION_ADRESSE_I1":null,
                        "LIBELLE_COMMUNE_I1":"ST LOUIS DE MONTFERRAND",
                        "RES_SECOND":"1",
                        "SABIANE12":"2",
                        "SIT_ACT7CL":"1",
                        "MOIS1":"10",
                        "AN_NAISS_HABITANTS31":null,
                        "CODE_POSTAL_I1":"33440",
                        "AN_NAISS_HABITANTS41":null,
                        "NUMERO_ADRESSE_I1":"14",
                        "COMPLEMENT_ADRESSE_I1":null,
                        "AN_NAISS_HABITANTS11":"1",
                        "NUM_TEL_I1":"0102030406",
                        "AN_NAISS_HABITANTS21":null,
                        "LIBELLE_VOIE_I1":"RUE DES LILAS",
                        "SEXE":"2",
                        "PREN_IND":"ANNE-LAURE",
                        "ANNEE":"2024",
                        "MINEUR":"2",
                        "EMPLOI":null,
                        "PRENOM_REPONDANT":"ANNE-LAURE"
                    }
                }""";
        assertThat(result).isNotNull();
        JSONAssert.assertEquals(expectedJson, result.toString(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    @DisplayName("should handle error when invalid data")
    void shouldHandleErrorWhenInvalidData() {
        // Given
        Data data = new Data();
        // invalid elements here
        Element child = doc.createElement("foo");
        child.setTextContent("bar");
        data.getDataElements().add(child);

        // When / Then
        assertThatThrownBy(() -> converter.convertInterrogationData(data))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Error when transforming XML data to JSON");
    }

    private Element createElement(String path) throws IOException, SAXException, ParserConfigurationException {
        File xmlFile = new File(path);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(xmlFile);
        return document.getDocumentElement();
    }
}


