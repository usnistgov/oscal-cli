
package gov.nist.secauto.oscal.tools.cli.core;

import gov.nist.secauto.oscal.tools.cli.core.commands.catalog.CatalogCommand;
import gov.nist.secauto.oscal.tools.cli.core.commands.profile.ProfileCommand;
import gov.nist.secauto.oscal.tools.cli.framework.CLIProcessor;

public class CLI {

  public static void main(String[] args) {
    new CLI().parse(args);
  }

  private CLIProcessor cliProcessor;

  public CLI() {
    this.cliProcessor = new CLIProcessor("oscal-cli", new Version());
    cliProcessor.addCommandHandler(new CatalogCommand());
    cliProcessor.addCommandHandler(new ProfileCommand());
  }

  private void parse(String[] args) {
    cliProcessor.process(args);
  }
}
