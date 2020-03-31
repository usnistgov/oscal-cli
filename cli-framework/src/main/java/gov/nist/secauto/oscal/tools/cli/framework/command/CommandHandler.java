package gov.nist.secauto.oscal.tools.cli.framework.command;

import gov.nist.secauto.oscal.tools.cli.framework.ExitStatus;

public interface CommandHandler {

  String getName();

  ExitStatus processCommand(String[] args, CommandContext callingContext);

  String getDescription();

}
