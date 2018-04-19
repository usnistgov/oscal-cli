package gov.nist.secauto.oscal.tools.cli.framework.command;

import java.util.LinkedList;
import java.util.List;

public class CommandContext {
  private final List<CommandHandler> callingCommands = new LinkedList<>();
  private final String exec;

  public CommandContext(CommandHandler handler, String exec) {
    this.exec = exec;
    callingCommands.add(handler);
  }

  public CommandContext(CommandHandler handler, CommandContext parentContext) {
    callingCommands.addAll(parentContext.getCallingCommands());
    exec = parentContext.getExec();

    callingCommands.add(handler);
  }

  /**
   * @return the callingCommands
   */
  public List<CommandHandler> getCallingCommands() {
    return callingCommands;
  }

  public String getExec() {
    return exec;
  }
}
