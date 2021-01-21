/**
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
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandResolver;
import gov.nist.secauto.oscal.tools.cli.framework.command.ExtraArgument;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

// TODO: remove oscal specific strings
public class CLIProcessor {
  private static final Logger log = LogManager.getLogger(CLIProcessor.class);

  private final Map<String, Command> commandToCommandHandlerMap = new LinkedHashMap<>();
  private final String exec;
  private final VersionInfo versionInfo;
  private final Terminal terminal;

  public CLIProcessor(String exec, VersionInfo versionInfo) {
    this.terminal = initTerminal();
    this.exec = exec;
    this.versionInfo = versionInfo;
  }

  private Terminal initTerminal() {
    Terminal retval;
    try {
      AnsiConsole.systemInstall();
      retval = TerminalBuilder.builder().system(true).jna(false).jansi(true).build();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    return retval;
  }

  public Terminal getTerminal() {
    return terminal;
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
    return retval;
  }

  /**
   * Process a set of CLIProcessor arguments.
   * 
   * @param args
   *          the arguments to process
   */
  public void process(String[] args) {
    ExitStatus status = parseCommand(args);
    String message = status.getMessage();
    if (message != null && !message.isEmpty()) {
      if (status.isError()) {
        AnsiConsole.err().println(ansi().fgBrightRed().format("Error: %s%n", message).reset());
        AnsiConsole.err().flush();
      } else {
        getTerminal().writer().println(message);
        terminal.flush();
      }
    }
    int exitCode = status.getExitCode().getStatusCode();
    System.exit(exitCode);
  }

  private ExitStatus parseCommand(String line) {
    String[] args = line.split("\\s");
    return parseCommand(args);
  }

  private ExitStatus parseCommand(String[] args) {
    ExitStatus status;
    // the first two arguments should be the <command> and <operation>, where <type> is the object type
    // the <operation> is performed against.
    if (args.length < 1) {
      status = ExitCode.INVALID_COMMAND.toExitStatus();
      showHelp(newOptionsInstance());
    } else if ("interactive".equals(args[0].toLowerCase())) {
      status = processInteractive();
    } else {
      status = processCommand(args);
    }

    return status;
  }

  private ExitStatus processCommand(String[] args) {
    // the first two arguments should be the <command> and <operation>, where <type> is the object type
    // the <operation> is performed against.
    LinkedList<String> extraArgs = new LinkedList<String>(Arrays.asList(args));
    Stack<Command> commandStack = CommandResolver.resolveCommand(extraArgs, new CommandCollection() {
      @Override
      public Command getCommandByName(String name) {
        return commandToCommandHandlerMap.get(name);
      }
    });

    ExitStatus retval;
    if (commandStack.isEmpty()) {
      CommandLineParser parser = new DefaultParser();
      try {
        Options options = newOptionsInstance();
        CommandLine cmdLine = parser.parse(options, args);
        if (cmdLine.hasOption("no-color")) {
          Ansi.setEnabled(false);
        }
        if (cmdLine.hasOption("version")) {
          showVersion();
          retval = ExitCode.OK.toExitStatus();
        } else if (cmdLine.hasOption("help")) {
          showHelp(options);
          retval = ExitCode.OK.toExitStatus();
        } else {
          retval = ExitCode.INVALID_COMMAND.toExitStatus(
              "Invalid command arguments: " + cmdLine.getArgList().stream().collect(Collectors.joining(" ")));
        }
      } catch (ParseException e) {
        retval = ExitCode.INVALID_COMMAND.toExitStatus(e.getMessage());
      }
    } else {
      retval = invokeCommand(commandStack, extraArgs);
    }
    return retval;
  }

  private ExitStatus invokeCommand(Stack<Command> commandStack, LinkedList<String> args) {

    if (log.isDebugEnabled()) {
      StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (Command cmd : commandStack) {
        if (first) {
          first = false;
        } else {
          builder.append(" -> ");
        }
        builder.append(cmd.getName());
      }
      log.debug("Processing command chain: {}", builder.toString());
    }

    Command command = commandStack.peek();

    Options options = newOptionsInstance();

    for (Command cmd : commandStack) {
      cmd.gatherOptions(options);
    }

    CommandLineParser parser = new DefaultParser();
    CommandLine cmdLine;
    try {
      cmdLine = parser.parse(options, args.toArray(new String[args.size()]));
    } catch (ParseException e) {
      showCommandHelp(commandStack, options);
      return ExitCode.INVALID_COMMAND.toExitStatus(e.getMessage());
    }

    boolean useColor = true;
    if (cmdLine.hasOption("no-color")) {
      Ansi.setEnabled(false);
      useColor = false;
    }

    if (cmdLine.hasOption("help")) {
      showCommandHelp(commandStack, options);
      return ExitCode.OK.toExitStatus();
    }

    CommandContext context = new CommandContext(commandStack, options, cmdLine);

    for (Command cmd : commandStack) {
      try {
        cmd.validateOptions(this, context);
      } catch (InvalidArgumentException e) {
        return handleInvalidCommand(commandStack, options, e.getMessage());
      }
    }

    ExitStatus retval = commandStack.peek().executeCommand(this, context);
    if (ExitCode.INVALID_COMMAND.equals(retval.getExitCode())) {
      showCommandHelp(commandStack, options);
    }
    return retval;
  }

  protected ExitStatus handleInvalidCommand(Stack<Command> callingCommands, Options options, String message) {
    AnsiConsole.err().println(ansi().fgBrightRed().format("Error: %s%n", message).reset());
    AnsiConsole.err().flush();
    showCommandHelp(callingCommands, options);
    return ExitCode.INVALID_COMMAND.toExitStatus();
  }

  protected void showCommandHelp(Stack<Command> callingCommands, Options options) {
    StringBuilder builder = new StringBuilder();
    builder.append(getExec());

    for (Command handler : callingCommands) {
      builder.append(' ');
      builder.append(handler.getName());
    }

    builder.append(" [<args>]");

    Command command = callingCommands.peek();
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

    HelpFormatter formatter = new HelpFormatter();
    Terminal terminal = getTerminal();
    PrintWriter writer = terminal.writer();
    formatter.printHelp(writer, terminal.getWidth(), builder.toString(), null, options, HelpFormatter.DEFAULT_LEFT_PAD,
        HelpFormatter.DEFAULT_DESC_PAD, null, false);
    terminal.flush();
  }

  private void showVersion() {
    VersionInfo info = getVersionInfo();
    getTerminal().writer().println(ansi().bold().a(getExec()).boldOff().a(" version ").bold().a(info.getVersion())
        .boldOff().a(" built on ").bold().a(info.getBuildTime()).reset());
    terminal.flush();
  }

  private void showHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    Terminal terminal = getTerminal();
    PrintWriter writer = terminal.writer();
    int width = terminal.getWidth();
    if (width == 0) {
      width = HelpFormatter.DEFAULT_WIDTH;
    }
    formatter.printHelp(writer, width, String.format("%s [-h | --help] <command> [<args>]", getExec()),
        null, options, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, null, true);
    if (!commandToCommandHandlerMap.isEmpty()) {
      writer.println();
      writer.println("The following are available commands:");
      for (Command handler : commandToCommandHandlerMap.values()) {
        writer.printf("   %-12.12s %-60.60s%n", handler.getName(), handler.getDescription());
      }
      writer.println();
      writer.println("'oscal <command> --help' will show help on that specific command.");
    }
    terminal.flush();
  }

  private ExitStatus processInteractive() {
    ExitStatus status = null;
    try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
      String line;
      while ((line = in.readLine()) != null) {
        status = parseCommand(line);
      }
    } catch (IOException e) {
      status = ExitCode.INPUT_ERROR.toExitStatus();
    }
    if (status == null) {
      status = ExitCode.OK.toExitStatus();
    }
    return status;
  }

}
