package org.sitenv.cdaschema.validator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.helger.schematron.ISchematronResource;
import com.helger.schematron.pure.SchematronResourcePure;
import com.helger.schematron.xslt.SchematronResourceSCH;

public class SchemaValidator {

	public static void main(String[] args) {
		try {
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();

			//GET Schema Validator
			Validator sdtcValidator = getSchemaValidator("infrastructure/cda/CDA_SDTC.xsd");
			
			//Validate schemas
			List<SAXParseException> exceptions = validateSchema(classLoader,sdtcValidator,"schemaErrorSample.xml");
			
			if(exceptions.size() > 0 ){
				exceptions.forEach( ex -> 
	            System.out.println("LineNumber:" +ex.getLineNumber() + " Column:" +ex.getColumnNumber() + " Message:" +ex.getMessage()));
	        }else
	        	System.out.println("No Schema Validations");
	        

			// Schematron Validation
			File schematronFile = new File(classLoader.getResource("TestSchematron.sch").getFile());
			File xmlFile = new File(classLoader.getResource("TestFile.xml").getFile());
			boolean isSchematronPassed = validateXMLViaPureSchematron(schematronFile, xmlFile);
			
			//SchematronOutputType schematronOutput = validateXMLViaXSLTSchematronFull(schematronFile, xmlFile);

			System.out.println("Scematron validation result:" + isSchematronPassed);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Validator getSchemaValidator(String xdsFilePath) {
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
	
	public static List<SAXParseException> validateSchema(ClassLoader classLoader,Validator validator, String xmlFile) throws SAXException, IOException{
		List<SAXParseException> exceptions = new ArrayList<>();
		validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                //exceptions.add(exception);
            }
            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
            	exceptions.add(exception);
            }
            @Override
            public void error(SAXParseException exception) throws SAXException {
            	exceptions.add(exception);
            }
        });
		
		validator.validate(new StreamSource(new File(classLoader.getResource(xmlFile).getFile())));
		return exceptions;
	}
	
	public static SchematronOutputType validateXMLViaXSLTSchematronFull (@Nonnull final File aSchematronFile, @Nonnull final File aXMLFile) throws Exception
	{
	  final ISchematronResource aResSCH = SchematronResourceSCH.fromFile (aSchematronFile);
	  if (!aResSCH.isValidSchematron ())
	    throw new IllegalArgumentException ("Invalid Schematron!");
	  return aResSCH.applySchematronValidationToSVRL (new StreamSource (aXMLFile));
	}

	public static boolean validateXMLViaPureSchematron(final File schematronFile, final File xmlFile) throws Exception {
		final ISchematronResource aResPure = SchematronResourcePure.fromFile(schematronFile);
		if (!aResPure.isValidSchematron()) {
			throw new IllegalArgumentException("Invalid Schematron!");
		}
		return aResPure.getSchematronValidity(new StreamSource(xmlFile)).isValid();
	}
}
