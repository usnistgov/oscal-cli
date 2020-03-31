package gov.nist.secauto.oscal.tools.cli.core.commands.catalog;

import gov.nist.secauto.oscal.tools.cli.framework.ExitCode;
import gov.nist.secauto.oscal.tools.cli.framework.ExitStatus;
import gov.nist.secauto.oscal.tools.cli.framework.command.AbstractParentCommandHandler;

import org.apache.commons.cli.CommandLine;

public class CatalogCommand extends AbstractParentCommandHandler {
  private static final String COMMAND = "catalog";

  public CatalogCommand() {
    super();
    addCommandHandler(new ValidateSubcommand());
    addCommandHandler(new RenderSubcommand());
    addCommandHandler(new ConvertSubcommand());
  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  protected ExitStatus executeCommand(CommandLine cmdLine) {
    return ExitCode.OK.toExitStatus();
  }

  @Override
  public String getDescription() {
    return "Perform an operation on an OSCAL Catalog";
  }

}
