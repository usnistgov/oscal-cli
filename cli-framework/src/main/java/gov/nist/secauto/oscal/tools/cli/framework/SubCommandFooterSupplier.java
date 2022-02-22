
package gov.nist.secauto.oscal.tools.cli.framework;

import static org.fusesource.jansi.Ansi.ansi;

import gov.nist.secauto.oscal.tools.cli.framework.command.Command;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandCollection;

import java.util.Collection;
import java.util.function.Supplier;

public class SubCommandFooterSupplier implements Supplier<CharSequence> {
  private final CLIProcessor cliProcessor;
  private final CommandCollection commandCollection;

  public SubCommandFooterSupplier(CLIProcessor cliProcessor, CommandCollection commandCollection) {
    this.cliProcessor = cliProcessor;
    this.commandCollection = commandCollection;
  }

  public CLIProcessor getCliProcessor() {
    return cliProcessor;
  }

  public CommandCollection getCommandCollection() {
    return commandCollection;
  }

  @Override
  public CharSequence get() {
    CharSequence retval;
    Collection<Command> subCommands = getCommandCollection().getSubCommands();
    if (subCommands.isEmpty()) {
      retval = "";
    } else {
      StringBuilder builder = new StringBuilder(64);
      builder
          .append(System.lineSeparator())
          .append("The following are available commands:")
          .append(System.lineSeparator());

      int length = subCommands.stream()
          .mapToInt(command -> command.getName().length())
          .max().orElse(0);

      for (Command command : subCommands) {
        builder.append(
            ansi()
                .render(String.format("   @|bold %-" + length + "s|@ %s%n",
                    command.getName(),
                    command.getDescription())));
      }
      builder
          .append(System.lineSeparator())
          .append('\'')
          .append(getCliProcessor().getExec())
          .append(" <command> --help' will show help on that specific command.")
          .append(System.lineSeparator());
      retval = builder;
    }
    return retval;
  }

}
