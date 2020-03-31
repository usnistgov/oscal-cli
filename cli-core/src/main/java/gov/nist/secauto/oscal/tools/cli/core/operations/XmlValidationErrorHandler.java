package gov.nist.secauto.oscal.tools.cli.core.operations;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class XmlValidationErrorHandler implements ErrorHandler {
  private List<ValidationFinding> findings = new LinkedList<>(); 

  @Override
  public void warning(SAXParseException ex) throws SAXException {
    findings.add(new ValidationFinding(Severity.WARNING, ex));
  }

  @Override
  public void error(SAXParseException ex) throws SAXException {
    findings.add(new ValidationFinding(Severity.ERROR, ex));
  }

  @Override
  public void fatalError(SAXParseException ex) throws SAXException {
    findings.add(new ValidationFinding(Severity.FATAL, ex));
  }

  public List<ValidationFinding> getFindings() {
    return Collections.unmodifiableList(findings);
  }

}
