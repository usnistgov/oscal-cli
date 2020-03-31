package gov.nist.secauto.oscal.tools.cli.core.commands.profile;

import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractConvertSubcommand;
import gov.nist.secauto.oscal.tools.cli.core.operations.XMLOperations;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

public class ConvertSubcommand extends AbstractConvertSubcommand {
  @Override
  public String getDescription() {
    return "Convert a specified OSCAL Profile to a different format";
  }

  @Override
  protected void performRender(File input, File result) throws IOException, TransformerException {
    XMLOperations.convertXMLToJSON(input, result);
  }
}
