/*
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government and is
 * being made available as a public service. Pursuant to title 17 United States
 * Code Section 105, works of NIST employees are not subject to copyright
 * protection in the United States. This software may be subject to foreign
 * copyright. Permission in the United States and in foreign countries, to the
 * extent that NIST may hold copyright, to use, copy, modify, create derivative
 * works, and distribute this software and its documentation without fee is hereby
 * granted on a non-exclusive basis, provided that this notice and disclaimer
 * of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE.  IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM,
 * OR IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */
package gov.nist.secauto.oscal.tools.cli.framework;

import static org.fusesource.jansi.Ansi.ansi;

import gov.nist.secauto.oscal.tools.cli.framework.command.Command;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandCollection;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: remove oscal specific strings
public class CLIProcessor {
  public static final String QUIET_OPTION_LONG_NAME = "quiet";

  private static final Logger LOGGER = LogManager.getLogger(CLIProcessor.class);

  private final Map<String, Command> commandToCommandHandlerMap = new LinkedHashMap<>(); // NOPMD - intentional
  private final String exec;
  private final VersionInfo versionInfo;

  public CLIProcessor(String exec, VersionInfo versionInfo) {
    this.exec = exec;
    this.versionInfo = versionInfo;
    AnsiConsole.systemInstall();
  }

  /**
   * Gets the command used to execute for use in help text.
   * 
   * @return the command name
   */
  public String getExec() {
    return exec;
  }

  /**
   * Retrieve the version information for this application.
   * 
   * @return the versionInfo
   */
  public VersionInfo getVersionInfo() {
    return versionInfo;
  }

  public void addCommandHandler(Command handler) {
    String commandName = handler.getName();
    this.commandToCommandHandlerMap.put(commandName, handler);
  }

  private Options newOptionsInstance() {
    Options retval = new Options();
    retval.addOption(Option.builder("h").longOpt("help").desc("display this help message").build());
    retval.addOption(Option.builder().longOpt("version").desc("display the application version").build());
    retval.addOption(Option.builder().longOpt("no-color").desc("do not colorize output").build());
    retval.addOption(
        Option.builder("q").longOpt(QUIET_OPTION_LONG_NAME).desc("minimize output to include only errors").build());
    return retval;
  }

  /**
   * Process a set of CLIProcessor arguments.
   * 
   * @param args
   *          the arguments to process
   * @return the exit code
   */
  public int process(String... args) {
    ExitStatus status = parseCommand(args);
    String message = status.getMessage();
    if (message != null && !message.isEmpty()) {
      AnsiConsole.err().println(ansi().fgBrightRed().format("Error: %s%n", message).reset());
      AnsiConsole.err().flush();
    }
    return status.getExitCode().getStatusCode();
  }

  private ExitStatus parseCommand(String... args) {
    CommandCollection commandCollection = new TopLevelCommandCollection();

    ExitStatus status;
    // the first two arguments should be the <command> and <operation>, where <type> is the object type
    // the <operation> is performed against.
    if (args.length < 1) {
      status = ExitCode.INVALID_COMMAND.toExitStatus();
      showHelp(newOptionsInstance(), commandCollection, Collections.emptyList());
      // } else if ("interactive".equalsIgnoreCase(args[0])) {
      // status = processInteractive();
    } else {
      status = processCommand(args, commandCollection);
    }

    return status;
  }

  private static void handleNoColor() {
    System.setProperty(AnsiConsole.JANSI_MODE, AnsiConsole.JANSI_MODE_STRIP);
    AnsiConsole.systemUninstall();
  }

  public static void handleQuiet() {
    LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
    Configuration config = ctx.getConfiguration();
    LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
    Level oldLevel = loggerConfig.getLevel();
    if (oldLevel.isLessSpecificThan(Level.ERROR)) {
      loggerConfig.setLevel(Level.ERROR);
      ctx.updateLoggers();
    }
  }

  private ExitStatus processCommand(String[] args, CommandCollection commandCollection) {
    // the first two arguments should be the <command> and <operation>, where <type> is the object type
    // the <operation> is performed against.
    List<String> commandArgs = Arrays.asList(args);
    CommandResult commandResult = resolveCommand(commandArgs, commandCollection);
    Options options = newOptionsInstance();

    ExitStatus retval;
    if (commandResult.getCommands().isEmpty()) {
      CommandLineParser parser = new DefaultParser();
      try {
        CommandLine cmdLine = parser.parse(options, args);
        if (cmdLine.hasOption("no-color")) {
          handleNoColor();
        }

        if (cmdLine.hasOption(QUIET_OPTION_LONG_NAME)) {
          handleQuiet();
        }

        if (cmdLine.hasOption("version")) {
          showVersion();
          retval = ExitCode.OK.toExitStatus();
        } else if (cmdLine.hasOption("help") || cmdLine.getArgList().isEmpty()) {
          showHelp(options, commandResult.getTargetCommand(), commandResult.getCommands());
          retval = ExitCode.OK.toExitStatus();
        } else {
          retval = handleInvalidCommand(commandResult, options,
              "Invalid command arguments: " + cmdLine.getArgList().stream().collect(Collectors.joining(" ")));
        }
      } catch (ParseException ex) {
        retval = handleInvalidCommand(commandResult, options, ex.getMessage());
      }
    } else {
      retval = invokeCommand(commandResult);
    }
    return retval;
  }

  @SuppressWarnings("PMD")
  private ExitStatus invokeCommand(CommandResult commandResult) {

    if (LOGGER.isDebugEnabled()) {
      String commandChain = commandResult.getCommands().stream()
          .map(command -> command.getName())
          .collect(Collectors.joining(" -> "));
      LOGGER.debug("Processing command chain: {}", commandChain);
    }

    Options options = newOptionsInstance();

    for (Command cmd : commandResult.getCommands()) {
      cmd.gatherOptions(options);
    }

    CommandLineParser parser = new DefaultParser();
    CommandLine cmdLine;
    try {
      cmdLine = parser.parse(options, commandResult.getArgArray());
    } catch (ParseException ex) {
      ExitStatus retval = handleInvalidCommand(commandResult, options, ex.getMessage());
      return retval;
    }

    if (cmdLine.hasOption("no-color")) {
      handleNoColor();
    }

    if (cmdLine.hasOption(QUIET_OPTION_LONG_NAME)) {
      handleQuiet();
    }

    if (cmdLine.hasOption("help")) {
      showHelp(options, commandResult.getTargetCommand(), commandResult.getCommands());
      return ExitCode.OK.toExitStatus();
    }

    CommandContext context = new CommandContext(commandResult.getCommands(), options, cmdLine);

    for (Command cmd : commandResult.getCommands()) {
      try {
        cmd.validateOptions(this, context);
      } catch (InvalidArgumentException e) {
        return handleInvalidCommand(commandResult, options, e.getMessage());
      }
    }

    ExitStatus retval = commandResult.getTargetCommand().executeCommand(this, context);
    if (ExitCode.INVALID_COMMAND.equals(retval.getExitCode())) {
      showHelp(options, commandResult.getTargetCommand(), commandResult.getCommands());
    }
    return retval;
  }

  public ExitStatus handleInvalidCommand(CommandResult commandResult, Options options, String message) {
    PrintStream err = AnsiConsole.err(); // NOPMD - not owner
    err.println(ansi().a('[').fgBrightRed().a("ERROR").reset().a("] ").a(message));
    err.flush();
    showHelp(options, commandResult.getTargetCommand(), commandResult.getCommands());
    return ExitCode.INVALID_COMMAND.toExitStatus();
  }

  public void showHelp(
      Options options,
      CommandCollection targetCommand,
      List<Command> callingCommands) {

    HelpFormatter formatter = new HelpFormatter();

    AnsiPrintStream out = AnsiConsole.out();
    int terminalWidth = Math.max(out.getTerminalWidth(), 40);

    PrintWriter writer = new PrintWriter(out);
    formatter.printHelp(
        writer,
        terminalWidth,
        targetCommand.buildHelpCliSyntax(getExec(), callingCommands),
        targetCommand.buildHelpHeader(),
        options,
        HelpFormatter.DEFAULT_LEFT_PAD,
        HelpFormatter.DEFAULT_DESC_PAD,
        targetCommand.buildHelpFooter(getExec()),
        false);
    writer.flush();
  }

  protected void showVersion() {
    VersionInfo info = getVersionInfo();
    PrintStream out = AnsiConsole.out(); // NOPMD - not owner
    out.println(ansi().bold().a(getExec()).boldOff().a(" version ").bold().a(info.getVersion())
        .boldOff().a(" built on ").bold().a(info.getBuildTime()).boldOff().a(" on commit ").bold().a(info.getCommit())
        .reset());
    info.generateExtraInfo(out);
    out.flush();
  }

  // private ExitStatus processInteractive() {
  // ExitStatus status = null;
  // try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
  // String line;
  // while ((line = reader.readLine()) != null) {
  // status = parseCommand(line);
  // }
  // } catch (IOException e) {
  // status = ExitCode.INPUT_ERROR.toExitStatus();
  // }
  // if (status == null) {
  // status = ExitCode.OK.toExitStatus();
  // }
  // return status;
  // }

  private class TopLevelCommandCollection implements CommandCollection {
    @Override
    public Command getCommandByName(String name) {
      return commandToCommandHandlerMap.get(name);
    }

    @Override
    public Collection<Command> getSubCommands() {
      return Collections.unmodifiableCollection(commandToCommandHandlerMap.values());
    }

    @Override
    public boolean isSubCommandRequired() {
      return true;
    }

    @Override
    public String buildHelpHeader() {
      // no header
      return null;
    }

    @Override
    public String buildHelpFooter(String exec) {
      // no footer
      return null;
    }

    @Override
    public ExitStatus executeCommand(CLIProcessor cliProcessor, CommandContext context) {
      showHelp(context.getOptions(), this, context.getCallingCommands());
      return ExitCode.OK.toExitStatus();
    }
  }

  protected CommandResult resolveCommand(List<String> args, CommandCollection collection) {
    CommandCollection currentCollection = collection;
    List<String> options = new LinkedList<>();
    List<Command> commands = new LinkedList<>();
    List<String> extraArgs = new LinkedList<>();

    boolean endArgs = false;
    for (int idx = 0; idx < args.size(); idx++) {
      String arg = args.get(idx);
      if (arg.startsWith("-")) {
        options.add(arg);
      } else if (!endArgs && "--".equals(arg)) {
        endArgs = true;
      } else {
        Command command = currentCollection.getCommandByName(arg);
        if (command == null || endArgs) {
          extraArgs.add(arg);
        } else {
          commands.add(command);
          currentCollection = command;
        }
      }
    }
    return new CommandResult(currentCollection, commands, options, extraArgs);
  }

  public class CommandResult {
    private final List<String> options;
    private final List<Command> commands;
    private final List<String> extraArgs;
    private final CommandCollection targetCommand;

    public CommandResult(CommandCollection targetCommand) {
      this(targetCommand, Collections.emptyList());
    }

    public CommandResult(CommandCollection targetCommand, List<Command> commands) {
      this(targetCommand, commands, Collections.emptyList(), Collections.emptyList());
    }

    public CommandResult(CommandCollection targetCommand, List<Command> commands, List<String> options,
        List<String> extraArgs) {
      this.targetCommand = targetCommand;
      this.commands = commands;
      this.options = options;
      this.extraArgs = extraArgs;
    }

    public CommandCollection getTargetCommand() {
      return targetCommand;
    }

    public List<String> getOptions() {
      return options;
    }

    public List<Command> getCommands() {
      return commands;
    }

    public List<String> getExtraArgs() {
      return extraArgs;
    }

    public String[] getArgArray() {
      return Stream.concat(options.stream(), extraArgs.stream()).toArray(size -> new String[size]);
    }
  }
}
