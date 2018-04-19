package gov.nist.secauto.oscal.tools.cli.core.operations;

import org.xml.sax.SAXParseException;

public class ValidationFinding {
  private final Severity severity;
  private final String systemId;
  private final int lineNumber;
  private final int columnNumber;
  private final String message;

  
  public ValidationFinding(Severity severity, SAXParseException ex) {
    this.severity = severity;
    
    this.systemId = ex.getSystemId();
    this.lineNumber = ex.getLineNumber();
    this.columnNumber = ex.getColumnNumber();
    this.message = ex.getMessage();
  }

  /**
   * @return the severity
   */
  public Severity getSeverity() {
    return severity;
  }
  /**
   * @return the systemId
   */
  public String getSystemId() {
    return systemId;
  }

  /**
   * @return the lineNumber
   */
  public int getLineNumber() {
    return lineNumber;
  }
  /**
   * @return the columnNumber
   */
  public int getColumnNumber() {
    return columnNumber;
  }
  /**
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  
}
