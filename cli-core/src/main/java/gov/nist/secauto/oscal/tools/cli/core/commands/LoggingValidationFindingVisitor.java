
package gov.nist.secauto.oscal.tools.cli.core.commands;

import static org.fusesource.jansi.Ansi.ansi;

import gov.nist.secauto.metaschema.binding.validation.ConstraintContentValidator.ConstraintValidationFinding;
import gov.nist.secauto.metaschema.binding.validation.IValidationFinding;
import gov.nist.secauto.metaschema.binding.validation.IValidationFindingVisitor;
import gov.nist.secauto.metaschema.binding.validation.JsonSchemaContentValidator.JsonValidationFinding;
import gov.nist.secauto.metaschema.binding.validation.XmlSchemaContentValidator.XmlValidationFinding;
import gov.nist.secauto.metaschema.model.common.constraint.IConstraint.Level;

import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXParseException;

public class LoggingValidationFindingVisitor implements IValidationFindingVisitor<Void, Void> {
  private static final Logger LOGGER = LogManager.getLogger(LoggingValidationFindingVisitor.class);

  @Override
  public Void visit(@NotNull JsonValidationFinding finding, Void context) {
    Ansi ansi = generatePreamble(finding.getSeverity());

    getLogger(finding).log(
        ansi.a('[')
            .fgBright(Color.WHITE)
            .a(finding.getCause().getPointerToViolation())
            .reset()
            .a(']')
            .format(" {} [{}]",
                finding.getMessage(),
                finding.getDocumentUri().toString(),
                finding.getDocumentUri()));
    return null;
  }

  @Override
  public Void visit(XmlValidationFinding finding, Void context) {
    Ansi ansi = generatePreamble(finding.getSeverity());
    SAXParseException ex = finding.getCause();

    getLogger(finding).log(
        ansi.format("{} [{}{{},{}}]",
            finding.getMessage(),
            finding.getDocumentUri().toString(),
            ex.getLineNumber(),
            ex.getColumnNumber()));
    return null;
  }

  @Override
  public Void visit(@NotNull ConstraintValidationFinding finding, Void context) {
    Ansi ansi = generatePreamble(finding.getSeverity());

    getLogger(finding).log(
        ansi.format("[{}] {}", finding.getNode().getMetapath(), finding.getMessage()));
    return null;
  }

  @NotNull
  protected LogBuilder getLogger(@NotNull IValidationFinding finding) {
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
  protected Ansi generatePreamble(@NotNull Level level) {
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
    ansi = ansi().fgBright(Color.WHITE).a("] ").reset();
    return ansi;
  }
}
