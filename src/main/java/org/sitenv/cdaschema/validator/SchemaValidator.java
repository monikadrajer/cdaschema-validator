package org.sitenv.cdaschema.validator;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;

public class SchemaValidator {

	public static void main(String[] args) {
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();

			// Schema Validation
			Validator sdtcValidator = getValidator("infrastructure/cda/CDA_SDTC.xsd");
			sdtcValidator
					.validate(new StreamSource(new File(classLoader.getResource("lowScoringSample.xml").getFile())));

			// Schematron Validation
			File schematronFile = new File(classLoader.getResource("Schematron.sch").getFile());
			File xmlFile = new File(classLoader.getResource("voc.xml").getFile());
			boolean isSchematronPassed = validateXMLViaPureSchematron(schematronFile, xmlFile);

			System.out.println("Finished" + isSchematronPassed);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Validator getValidator(String xdsFilePath) {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Source schemaFile;
		Schema schema = null;
		Validator validator = null;
		try {
			schemaFile = new StreamSource(new File(classLoader.getResource(xdsFilePath).getFile()));
			schema = factory.newSchema(schemaFile);
			validator = schema.newValidator();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return validator;
	}

	public static boolean validateXMLViaPureSchematron(final File schematronFile, final File xmlFile) throws Exception {
		final ISchematronResource aResPure = SchematronResourcePure.fromFile(schematronFile);
		if (!aResPure.isValidSchematron()) {
			throw new IllegalArgumentException("Invalid Schematron!");
		}
		return aResPure.getSchematronValidity(new StreamSource(xmlFile)).isValid();
	}
}
