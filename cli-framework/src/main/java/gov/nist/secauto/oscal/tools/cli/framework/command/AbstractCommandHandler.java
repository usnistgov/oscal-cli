
package gov.nist.secauto.oscal.tools.cli.framework.command;

import gov.nist.secauto.oscal.tools.cli.framework.CLIProcessor;
import gov.nist.secauto.oscal.tools.cli.framework.ExitCode;
import gov.nist.secauto.oscal.tools.cli.framework.ExitStatus;
import gov.nist.secauto.oscal.tools.cli.framework.InvalidArgumentException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractCommandHandler implements CommandHandler {
  private static final Logger log = LogManager.getLogger(AbstractCommandHandler.class);
  private final Options options;

  protected abstract ExitStatus executeCommand(CommandLine cmdLine);

  public AbstractCommandHandler() {
    this.options = CLIProcessor.newOptions();
  }

  public Options getOptions() {
    return options;
  }
  @Override
  public ExitStatus processCommand(String[] args, CommandContext callingContext) {
    log.debug("Processing command: {}", getName());
    return processArguments(args, callingContext);
  }

  private ExitStatus processArguments(String[] args, CommandContext callingContext) {
    log.debug("Processing options: {}", Arrays.asList(args));

    CommandLineParser parser = new DefaultParser();

    CommandLine cmdLine;
    try {
      cmdLine = parser.parse(getOptions(), args);
    } catch (ParseException e) {
      return ExitCode.INVALID_COMMAND.toExitStatus(e.getMessage());
    }

    if (cmdLine.hasOption("help")) {
      showHelp(cmdLine, callingContext);
      return ExitCode.OK.toExitStatus();
    }

    try {
      validateOptions(cmdLine, callingContext);
    } catch (InvalidArgumentException e) {
      return handleInvalidCommand(cmdLine, callingContext, e.getMessage());
    }

    return executeCommand(cmdLine);
  }

  protected ExitStatus handleInvalidCommand(CommandLine cmdLine, CommandContext callingContext, String message) {
    log.error(message);
    showHelp(cmdLine, callingContext);
    return ExitCode.INVALID_COMMAND.toExitStatus();
  }

  protected String getExtraArgumentsText() {
    return "";
  }

  protected void showHelp(CommandLine cmdLine, CommandContext callingContext) {
    StringBuilder builder = new StringBuilder();
    builder.append(callingContext.getExec());

    for (CommandHandler handler : callingContext.getCallingCommands()) {
      builder.append(" ");
      builder.append(handler.getName());
    }

    String extraArguments = getExtraArgumentsText();
    if (!extraArguments.isEmpty()) {
      builder.append(" ").append(extraArguments);
    }
    builder.append(" [<args>]");
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(builder.toString(), getOptions());
  }

  protected void validateOptions(CommandLine cmdLine, CommandContext callingContext) throws InvalidArgumentException {
    // do nothing by default
  }
}
