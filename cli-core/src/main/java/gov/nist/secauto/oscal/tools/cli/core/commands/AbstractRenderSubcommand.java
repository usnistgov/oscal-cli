
package gov.nist.secauto.oscal.tools.cli.core.commands;

import gov.nist.secauto.oscal.tools.cli.framework.ExitCode;
import gov.nist.secauto.oscal.tools.cli.framework.ExitStatus;
import gov.nist.secauto.oscal.tools.cli.framework.InvalidArgumentException;
import gov.nist.secauto.oscal.tools.cli.framework.command.AbstractCommandHandler;
import gov.nist.secauto.oscal.tools.cli.framework.command.CommandContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;

public abstract class AbstractRenderSubcommand extends AbstractCommandHandler {
  private static final Logger log = LogManager.getLogger(AbstractRenderSubcommand.class);
  private static final String COMMAND = "render";

  public AbstractRenderSubcommand() {
    super();
    getOptions()
        .addOption(Option.builder().longOpt("overwrite").desc("overwrite the destination if it exists").build());

  }

  @Override
  public String getName() {
    return COMMAND;
  }

  @Override
  protected String getExtraArgumentsText() {
    return "<source file> <destination file>";
  }

  @Override
  protected void validateOptions(CommandLine cmdLine, CommandContext callingContext) throws InvalidArgumentException {
    List<String> extraArgs = cmdLine.getArgList();
    if (extraArgs.size() != 2) {
      throw new InvalidArgumentException("Both a source and destination argument must be provided.");
    }

    File source = new File(extraArgs.get(0));
    if (!source.exists()) {
      throw new InvalidArgumentException("The provided source '" + source.getPath() + "' does not exist.");
    }
    if (!source.canRead()) {
      throw new InvalidArgumentException("The provided source '" + source.getPath() + "' is not readable.");
    }

    File destination = new File(extraArgs.get(1));
    if (destination.exists()) {
      if (!cmdLine.hasOption("overwrite")) {
        throw new InvalidArgumentException("The provided destination '" + source.getPath()
            + "' already exists and the --overwrite option was not provided.");

      }
      if (!destination.canWrite()) {
        throw new InvalidArgumentException("The provided destination '" + source.getPath() + "' is not writable.");
      }
    }
  }

  @Override
  protected ExitStatus executeCommand(CommandLine cmdLine) {
    List<String> extraArgs = cmdLine.getArgList();

    File input = new File(extraArgs.get(0));
    File result = new File(extraArgs.get(1));

    try {
      performRender(input, result);
    } catch (IOException | TransformerException e) {
      return ExitCode.FAIL.toExitStatus(e.getMessage());
    }
    log.info("Generated HTML file: " + result.getPath());
    return ExitCode.OK.toExitStatus();
  }

  protected abstract void performRender(File input, File result) throws IOException, TransformerException;
}
