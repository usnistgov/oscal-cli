
package gov.nist.secauto.oscal.tools.cli.core.commands.profile;

import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractRenderSubcommand;
import gov.nist.secauto.oscal.tools.cli.core.operations.XMLOperations;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.TransformerException;

public class RenderSubcommand extends AbstractRenderSubcommand {
  @Override
  public String getDescription() {
    return "Render a specified OSCAL Profile using a transform (e.g., to HTML)";
  }

  @Override
  protected void performRender(File input, File result) throws IOException, TransformerException {
    XMLOperations.renderProfileHTML(input, result);
  }
}
