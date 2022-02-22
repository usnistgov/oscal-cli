
package gov.nist.secauto.oscal.tools.cli.framework;

import gov.nist.secauto.oscal.tools.cli.framework.command.Command;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandCollection;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandResolver;
import gov.nist.secauto.oscal.tools.cli.framework.command.ExtraArgument;

import java.util.Collection;

public class CommandResolverResultCliSyntaxSupplier
    extends AbstractCliSyntaxSupplier {

  private final CommandResolver.CommandResult commandResult;

  public CommandResolverResultCliSyntaxSupplier(CLIProcessor cliProcessor,
      CommandResolver.CommandResult commandResult) {
    super(cliProcessor);
    this.commandResult = commandResult;
  }

  public CommandResolver.CommandResult getCommandResult() {
    return commandResult;
  }

  @Override
  protected CommandCollection getCommandCollection() {
    return getCommandResult().getCommands().peek();
  }

  @Override
  protected void handleCallContext(StringBuilder builder) {
    for (Command handler : getCommandResult().getCommands()) {
      builder.append(' ');
      builder.append(handler.getName());
    }
  }

  @Override
  protected void handleExtraCommandArgs(StringBuilder builder) {
    Command command = getCommandResult().getCommands().peek();

    for (ExtraArgument argument : command.getExtraArguments()) {
      builder.append(' ');
      if (!argument.isRequired()) {
        builder.append('[');
      }

      builder.append('<');
      builder.append(argument.getName());
      builder.append('>');

      if (argument.getNumber() > 1) {
        builder.append("...");
      }

      if (!argument.isRequired()) {
        builder.append(']');
      }
    }
  }
}
