package gov.nist.secauto.oscal.tools.cli.framework.command;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractTerminalCommand implements Command {

  @Override
  public Collection<Command> getSubCommands() {
    return Collections.emptyList();
  }

  @Override
  public boolean isSubCommandRequired() {
    return false;
  }
}
