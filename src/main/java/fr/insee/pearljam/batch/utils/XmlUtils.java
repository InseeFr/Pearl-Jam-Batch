package fr.insee.pearljam.batch.utils;

import fr.insee.pearljam.batch.communication.Courrier;
import fr.insee.pearljam.batch.communication.Courriers;
import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.PublicationException;
import fr.insee.pearljam.batch.exception.ValidateException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Operation on XML Content 
 * - getXmlNodeFile 
 * - validateXMLSchema 
 * - xmlToObject 
 * - objectToXML 
 * - removeSurveyUnitNode
 * - updateSampleFileErrorList
 * 
 * @author Claudel Benjamin
 * 
 */
public class XmlUtils {
	private static final Logger logger = LogManager.getLogger(XmlUtils.class);

	private XmlUtils() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Validate an XML file by XSD validator
	 * 
	 * @param xmlPath xml path
	 * @return true if XML is valid
	 * @throws XMLStreamException
	 */
	public static void validateXMLSchema(URL model, String xmlPath) throws ValidateException, XMLStreamException {
		ValidateException ve = null;
		
		XMLStreamReader xmlEncoding= null;
		try (FileInputStream fis = new FileInputStream(xmlPath);
			FileReader fr = new FileReader(xmlPath);
			) {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			Schema schema = factory.newSchema(model);
			Validator validator = schema.newValidator();
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
			inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
			xmlEncoding = inputFactory.createXMLStreamReader(fr);

			String encoding = xmlEncoding.getCharacterEncodingScheme();
			if ("UTF8".equalsIgnoreCase(encoding) || StandardCharsets.UTF_8.name().equalsIgnoreCase(encoding)) {
				validator.validate(new StreamSource(fis));
			}
		} catch (Exception e) {
			ve = new ValidateException("Error during validation : " + e.getMessage());
		} finally {
			if(xmlEncoding!=null)xmlEncoding.close();
		}
		if(ve!=null)throw ve;
		logger.log(Level.INFO, "{} validate with {}", xmlPath, model.getFile());
	}
	
	
	public static <T> T xmlToObject(String filename, Class<T> clazz) throws ValidateException{
		try{
			DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
			df.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	        df.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        	df.setFeature("http://xml.org/sax/features/external-general-entities", false);
    	    df.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			DocumentBuilder builder = df.newDocumentBuilder();
			Document document = builder.parse(new InputSource(filename));
			document.getDocumentElement().normalize();
			DOMSource domSource = new DOMSource(document);
	
	        StringWriter strWriter = new StringWriter();
	        StreamResult streamResult = new StreamResult(strWriter);

 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

	        transformerFactory.newTransformer().transform(domSource, streamResult);
			StreamSource xmlStream = new StreamSource(new StringReader(strWriter.getBuffer().toString()));
			
			JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[]{clazz}, null);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			return clazz.cast(unmarshaller.unmarshal(xmlStream));
		} catch (ParserConfigurationException | SAXException | IOException | TransformerException | JAXBException e) {
			throw new ValidateException("Error during transfo xml to object : " + e.getMessage());
		}
	}
	
	
	 
public static File objectToXML(String filename, Object object) throws BatchException{
		try {
            //Create JAXB Context
			JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[]{object.getClass()}, null);

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
           //Store XML to File
            File file = new File(filename);
            //Writes XML file to file-system
            jaxbMarshaller.marshal(object, file); 
            return file;
		}catch (JAXBException e) {
			throw new BatchException("Error during transfo object to xml : " + e.getMessage());
		}
    }

	public static Path printToXmlFile(Courriers courriersToPrint, String outputDir) throws PublicationException {
		try {
			// Convert Courriers to Document
			JAXBContext jaxbContext = JAXBContextFactory.createContext(new Class[]{Courriers.class}, null);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document document = factory.newDocumentBuilder().newDocument();
			marshaller.marshal(courriersToPrint, document);

			// In each Courrier : Sort then add Variables dynamic fields
			NodeList courrierNodes = document.getElementsByTagName("Courrier");
			for (int i = 0; i < courrierNodes.getLength(); i++) {
				Element courrierElement = (Element) courrierNodes.item(i);
				Element variablesElement = (Element) courrierElement.getElementsByTagName("Variables").item(0);

				if (variablesElement != null) { // will never be null
					Courrier courrier = courriersToPrint.getCourriers().get(i);
					Map<String, String> additionalFields = courrier.getVariables().getAdditionalFields();

					// Create Element for each Map entry and append to the Variables node
					additionalFields.entrySet().stream().sorted((entryA, entryB) -> entryA.getKey().compareToIgnoreCase(entryB.getKey()))
							.forEach(
									entry -> {
										// Check if the element already exists
										NodeList existingNodes = variablesElement.getElementsByTagName(entry.getKey());
										if (existingNodes.getLength() > 0) {
											// If the node exists, update its text content
											existingNodes.item(0).setTextContent(entry.getValue());
										} else {
											// Otherwise, create a new element and append it
											Element additionalElement = document.createElement(entry.getKey());
											additionalElement.setTextContent(entry.getValue());
											variablesElement.appendChild(additionalElement);
										}
									}
							);
				}
			}

			// Step 5: Write the Courriers XML file with expected name
			String fileName = String.format("xml_d_%s_%s.xml", courriersToPrint.getCommunicationModel(), courriersToPrint.getEditionId());
			Path outDirComm = Paths.get(outputDir + "/communication");
			Path tempFilePath = outDirComm.resolve(fileName);

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(new DOMSource(document), new StreamResult(Files.newOutputStream(tempFilePath)));

			return tempFilePath;

		} catch (JAXBException | IOException | ParserConfigurationException |
				 javax.xml.transform.TransformerException e) {
			String errorMessage =
			String.format("Error when printing courriers : communicationModel : %s - idOperation : %s",courriersToPrint.getCommunicationModel(), courriersToPrint.getIdOperation());
			logger.warn("Error when printing courriers file",e);
			throw new PublicationException(errorMessage,e);
		}
	}
}
