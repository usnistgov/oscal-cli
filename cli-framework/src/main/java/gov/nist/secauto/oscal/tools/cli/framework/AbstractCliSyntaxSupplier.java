package gov.nist.secauto.oscal.tools.cli.framework;

import gov.nist.secauto.oscal.tools.cli.framework.command.Command;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandCollection;

import java.util.Collection;
import java.util.function.Supplier;

public abstract class AbstractCliSyntaxSupplier implements CliSyntaxSupplier {
  private final CLIProcessor cliProcessor;

  public AbstractCliSyntaxSupplier(CLIProcessor cliProcessor) {
    this.cliProcessor = cliProcessor;
  }

  protected CLIProcessor getCliProcessor() {
    return cliProcessor;
  }

  protected abstract CommandCollection getCommandCollection();

  @Override
  public CharSequence get() {
    StringBuilder builder = new StringBuilder(64);
    builder.append(getCliProcessor().getExec());

    handleCallContext(builder);

    Collection<Command> subCommands = getCommandCollection().getSubCommands();
    if (!subCommands.isEmpty()) {
      builder.append(' ');
      if (!getCommandCollection().isSubCommandRequired()) {
        builder.append('[');
      }

      builder.append("<command>");

      if (!getCommandCollection().isSubCommandRequired()) {
        builder.append(']');
      }
    }

    builder.append(" [<options>]");

    handleExtraCommandArgs(builder);
    
    return builder;
  }

  protected abstract void handleExtraCommandArgs(StringBuilder builder);

  protected abstract void handleCallContext(StringBuilder builder);

}
