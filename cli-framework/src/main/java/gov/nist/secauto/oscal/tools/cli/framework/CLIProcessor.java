/**
 * Portions of this software was developed by employees of the National Institute
 * of Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 17 United States Code Section 105, works of NIST employees are
 * not subject to copyright protection in the United States and are considered to
 * be in the public domain. Permission to freely use, copy, modify, and distribute
 * this software and its documentation without fee is hereby granted, provided that
 * this notice and disclaimer of warranty appears in all copies.
 *
 * THE SOFTWARE IS PROVIDED 'AS IS' WITHOUT ANY WARRANTY OF ANY KIND, EITHER
 * EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY WARRANTY
 * THAT THE SOFTWARE WILL CONFORM TO SPECIFICATIONS, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND FREEDOM FROM
 * INFRINGEMENT, AND ANY WARRANTY THAT THE DOCUMENTATION WILL CONFORM TO THE
 * SOFTWARE, OR ANY WARRANTY THAT THE SOFTWARE WILL BE ERROR FREE. IN NO EVENT
 * SHALL NIST BE LIABLE FOR ANY DAMAGES, INCLUDING, BUT NOT LIMITED TO, DIRECT,
 * INDIRECT, SPECIAL OR CONSEQUENTIAL DAMAGES, ARISING OUT OF, RESULTING FROM, OR
 * IN ANY WAY CONNECTED WITH THIS SOFTWARE, WHETHER OR NOT BASED UPON WARRANTY,
 * CONTRACT, TORT, OR OTHERWISE, WHETHER OR NOT INJURY WAS SUSTAINED BY PERSONS OR
 * PROPERTY OR OTHERWISE, AND WHETHER OR NOT LOSS WAS SUSTAINED FROM, OR AROSE OUT
 * OF THE RESULTS OF, OR USE OF, THE SOFTWARE OR SERVICES PROVIDED HEREUNDER.
 */

package gov.nist.secauto.oscal.tools.cli.framework;

import gov.nist.secauto.oscal.tools.cli.framework.command.CommandContext;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandHandler;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

// TODO: remove oscal specific strings
public class CLIProcessor {
  private static final Logger log = LogManager.getLogger(CLIProcessor.class);

  public static Options newOptions() {
    Options retval = new Options();
    retval.addOption(Option.builder("h").longOpt("help").desc("display this help message").build());
    return retval;
  }

  private final Map<String, CommandHandler> commandToCommandHandlerMap = new LinkedHashMap<>();
  private final Options options;
  private final String exec;
  private final VersionInfo versionInfo;

  public CLIProcessor(String exec, VersionInfo versionInfo) {
    this.exec = exec;
    this.versionInfo = versionInfo;
    options = newOptions();
    options.addOption(Option.builder("version").desc("display the application version").build());
  }

  /**
   * @return the exec
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

  public void addCommandHandler(CommandHandler handler) {
    String commandName = handler.getName();
    this.commandToCommandHandlerMap.put(commandName, handler);
  }

  /**
   * @return the options
   */
  public Options getOptions() {
    return options;
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
        log.error(message);
      } else {
        log.info(message);
      }
    }
    int exitCode = status.getExitCode().getStatusCode();
    log.debug("exit code: {}", exitCode);
    System.exit(exitCode);
  }

  private ExitStatus parseCommand(String line) {
    String[] args = line.split("\\s");
    return parseCommand(args);
  }

  private ExitStatus parseCommand(String[] args) {
    ExitStatus status;
    // the first two arguments should be the <type> and <operation>, where <type> is the object type
    // the <operation> is performed against.
    if (args.length < 1) {
      status = ExitCode.INVALID_COMMAND.toExitStatus();
      showHelp();
    } else if ("interactive".equals(args[0].toLowerCase())) {
      status = processInteractive();
    } else {
      status = processCommand(args);
    }

    return status;
  }

  private ExitStatus processCommand(String[] args) {
    String commandName = args[0];

    CommandHandler handler = commandToCommandHandlerMap.get(commandName);

    ExitStatus retval;
    if (handler == null) {
      CommandLineParser parser = new DefaultParser();
      try {
        CommandLine cmdLine = parser.parse(getOptions(), args);
        if (cmdLine.hasOption("version")) {
          showVersion();
          retval = ExitCode.OK.toExitStatus();
        } else if (cmdLine.hasOption("help")) {
          showHelp();
          retval = ExitCode.OK.toExitStatus();
        } else {
          retval = ExitCode.INVALID_COMMAND.toExitStatus("Invalid command arguments: "+cmdLine.getArgList().stream().collect(Collectors.joining(" ")));
        }
      } catch (ParseException e) {
        retval = ExitCode.INVALID_COMMAND.toExitStatus(e.getMessage());
      }
    } else {
      retval = handler.processCommand(Arrays.copyOfRange(args, 1, args.length), new CommandContext(handler, "oscal"));
    }
    return retval;
  }

  private void showVersion() {
    VersionInfo info = getVersionInfo();
    StringBuilder builder = new StringBuilder();
    builder.append(getExec()).append(" version ").append(info.getVersion()).append(" built on ").append(info.getBuildTime());
    System.out.println(builder.toString());
  }

  private void showHelp() {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("oscal [-h | --help] <command> [<args>]", getOptions());
    if (!commandToCommandHandlerMap.isEmpty()) {
      System.out.println();
      System.out.println("The following are available commands:");
      for (CommandHandler handler : commandToCommandHandlerMap.values()) {
        System.out.printf("   %-12.12s %-60.60s%n", handler.getName(), handler.getDescription());
      }
      System.out.println();
      System.out.println("'oscal <command> --help' will show help on that specific command.");
    }
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
