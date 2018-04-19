
package gov.nist.secauto.oscal.tools.cli.core.operations;

import gov.nist.secauto.oscal.tools.cli.core.commands.catalog.ValidateSubcommand;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ClasspathResourceResolver implements LSResourceResolver {
  private static final Logger log = LogManager.getLogger(ValidateSubcommand.class);

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
    log.debug("Resolving resource: type={}, namespaceURI={}, publicId={}, systemId={}, baseURI={}", type, namespaceURI,
        publicId, systemId, baseURI);
    InputStream resourceAsStream = this.getClass().getResourceAsStream(systemId);
    LSInputImpl retval = new LSInputImpl();
    retval.setPublicId(publicId);
    retval.setSystemId(systemId);
    retval.setBaseURI(baseURI);
    retval.setCharacterStream(new InputStreamReader(resourceAsStream));
    return retval;
  }

  private static class LSInputImpl implements LSInput {
    private Reader characterStream;
    private InputStream byteStream;
    private String stringData;
    private String systemId;
    private String publicId;
    private String baseURI;
    private String encoding;
    private boolean certifiedText;

    /**
     * @return the characterStream
     */
    public Reader getCharacterStream() {
      return characterStream;
    }

    /**
     * @param characterStream
     *          the characterStream to set
     */
    public void setCharacterStream(Reader characterStream) {
      this.characterStream = characterStream;
    }

    /**
     * @return the byteStream
     */
    public InputStream getByteStream() {
      return byteStream;
    }

    /**
     * @param byteStream
     *          the byteStream to set
     */
    public void setByteStream(InputStream byteStream) {
      this.byteStream = byteStream;
    }

    /**
     * @return the stringData
     */
    public String getStringData() {
      return stringData;
    }

    /**
     * @param stringData
     *          the stringData to set
     */
    public void setStringData(String stringData) {
      this.stringData = stringData;
    }

    /**
     * @return the systemId
     */
    public String getSystemId() {
      return systemId;
    }

    /**
     * @param systemId
     *          the systemId to set
     */
    public void setSystemId(String systemId) {
      this.systemId = systemId;
    }

    /**
     * @return the publicId
     */
    public String getPublicId() {
      return publicId;
    }

    /**
     * @param publicId
     *          the publicId to set
     */
    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    /**
     * @return the baseURI
     */
    public String getBaseURI() {
      return baseURI;
    }

    /**
     * @param baseURI
     *          the baseURI to set
     */
    public void setBaseURI(String baseURI) {
      this.baseURI = baseURI;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
      return encoding;
    }

    /**
     * @param encoding
     *          the encoding to set
     */
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    /**
     * @param certifiedText
     *          the certifiedText to set
     */
    public void setCertifiedText(boolean certifiedText) {
      this.certifiedText = certifiedText;
    }

    @Override
    public boolean getCertifiedText() {
      return certifiedText;
    }
  }
}
