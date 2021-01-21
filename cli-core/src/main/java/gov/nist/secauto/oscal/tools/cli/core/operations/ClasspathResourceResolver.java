/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

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
