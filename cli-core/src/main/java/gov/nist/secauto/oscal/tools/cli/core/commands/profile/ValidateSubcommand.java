
package gov.nist.secauto.oscal.tools.cli.core.commands.profile;

import gov.nist.secauto.oscal.tools.cli.core.commands.AbstractValidationSubcommand;
import gov.nist.secauto.oscal.tools.cli.core.operations.XMLOperations;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class ValidateSubcommand extends AbstractValidationSubcommand {
  @Override
  public String getDescription() {
    return "Validate that a specified OSCAL Profile is well-formed";
  }

  @Override
  protected List<Source> getSchemaSources() throws IOException {
    List<Source> retval = new LinkedList<>();
    retval.add(XMLOperations.getStreamSource(getClass().getResource("/schema/oscal-profile.xsd")));
    return Collections.unmodifiableList(retval);
  }

}
