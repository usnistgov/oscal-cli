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
import org.xml.sax.SAXParseException;

import edu.umd.cs.findbugs.annotations.NonNull;

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

  public static void handleJsonValidationFinding(@NonNull JsonValidationFinding finding) {
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

  public static void handleConstraintValidationFinding(@NonNull ConstraintValidationFinding finding) {
    Ansi ansi = generatePreamble(finding.getSeverity());

    getLogger(finding).log(
        ansi.format("[%s] %s", finding.getNode().getMetapath(), finding.getMessage()));
  }

  @NonNull
  private static LogBuilder getLogger(@NonNull IValidationFinding finding) {
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

  @NonNull
  private static Ansi generatePreamble(@NonNull Level level) {
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
