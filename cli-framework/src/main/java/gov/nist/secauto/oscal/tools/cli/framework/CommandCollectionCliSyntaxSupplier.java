
package gov.nist.secauto.oscal.tools.cli.framework;

import gov.nist.secauto.oscal.tools.cli.framework.command.CommandCollection;

public class CommandCollectionCliSyntaxSupplier
    extends AbstractCliSyntaxSupplier {

  private final CommandCollection commandCollection;

  public CommandCollectionCliSyntaxSupplier(CLIProcessor cliProcessor, CommandCollection commandCollection) {
    super(cliProcessor);
    this.commandCollection = commandCollection;
  }

  @Override
  protected CommandCollection getCommandCollection() {
    return commandCollection;
  }

  @Override
  protected void handleExtraCommandArgs(StringBuilder builder) {
    builder.append("[-h | --help]");
  }

  @Override
  protected void handleCallContext(StringBuilder builder) {
    // add nothing
  }

}
