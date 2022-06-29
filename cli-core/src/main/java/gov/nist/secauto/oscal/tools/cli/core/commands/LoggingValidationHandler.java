
package gov.nist.secauto.oscal.tools.cli.core.commands;

import static org.fusesource.jansi.Ansi.ansi;

import gov.nist.secauto.metaschema.model.common.constraint.ConstraintValidationFinding;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;
import gov.nist.secauto.metaschema.model.common.validation.IValidationFinding;
import gov.nist.secauto.metaschema.model.common.validation.IValidationResult;
import gov.nist.secauto.metaschema.model.common.validation.JsonSchemaContentValidator.JsonValidationFinding;
import gov.nist.secauto.metaschema.model.common.validation.XmlSchemaContentValidator.XmlValidationFinding;

import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXParseException;

public final class LoggingValidationHandler {
  private static final Logger LOGGER = LogManager.getLogger(LoggingValidationHandler.class);

  private LoggingValidationHandler() {
    // disable construction
  }

  public static boolean handleValidationResults(IValidationResult result) {
    for (IValidationFinding finding : result.getFindings()) {
      if (finding instanceof JsonValidationFinding) {
        handleJsonValidationFinding((JsonValidationFinding) finding);
      } else if (finding instanceof XmlValidationFinding) {
        handleXmlValidationFinding((XmlValidationFinding) finding);
      } else if (finding instanceof ConstraintValidationFinding) {
        handleConstraintValidationFinding((ConstraintValidationFinding) finding);
      } else {
        throw new IllegalStateException();
      }

    }
    return result.isPassing();
  }

  public static void handleJsonValidationFinding(@NotNull JsonValidationFinding finding) {
    Ansi ansi = generatePreamble(finding.getSeverity());

    getLogger(finding).log(
        ansi.a('[')
            .fgBright(Color.WHITE)
            .a(finding.getCause().getPointerToViolation())
            .reset()
            .a(']')
            .format(" %s [%s]",
                finding.getMessage(),
                finding.getDocumentUri().toString()));
  }

  public static void handleXmlValidationFinding(XmlValidationFinding finding) {
    Ansi ansi = generatePreamble(finding.getSeverity());
    SAXParseException ex = finding.getCause();

    getLogger(finding).log(
        ansi.format("%s [%s{%d,%d}]",
            finding.getMessage(),
            finding.getDocumentUri().toString(),
            ex.getLineNumber(),
            ex.getColumnNumber()));
  }

  public static void handleConstraintValidationFinding(@NotNull ConstraintValidationFinding finding) {
    Ansi ansi = generatePreamble(finding.getSeverity());

    getLogger(finding).log(
        ansi.format("[%s] %s", finding.getNode().getMetapath(), finding.getMessage()));
  }

  @NotNull
  private static LogBuilder getLogger(@NotNull IValidationFinding finding) {
    LogBuilder retval;
    switch (finding.getSeverity()) {
    case CRITICAL:
      retval = LOGGER.atFatal();
      break;
    case ERROR:
      retval = LOGGER.atError();
      break;
    case WARNING:
      retval = LOGGER.atWarn();
      break;
    case INFORMATIONAL:
      retval = LOGGER.atInfo();
      break;
    default:
      throw new IllegalArgumentException("Unknown level: " + finding.getSeverity().name());
    }

    if (finding.getCause() != null) {
      retval.withThrowable(finding.getCause());
    }

    return retval;
  }

  @NotNull
  private static Ansi generatePreamble(@NotNull Level level) {
    Ansi ansi = ansi().fgBright(Color.WHITE).a('[').reset();

    switch (level) {
    case CRITICAL:
      ansi = ansi.fgRed().a("CRITICAL").reset();
      break;
    case ERROR:
      ansi = ansi.fgBrightRed().a("ERROR").reset();
      break;
    case WARNING:
      ansi = ansi.fgBrightYellow().a("WARNING").reset();
      break;
    case INFORMATIONAL:
      ansi = ansi.fgBrightBlue().a("INFO").reset();
      break;
    default:
      ansi = ansi().fgBright(Color.MAGENTA).a(level.name()).reset();
      break;
    }
    ansi = ansi.fgBright(Color.WHITE).a("] ").reset();
    return ansi;
  }
}
