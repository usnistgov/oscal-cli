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

package gov.nist.secauto.oscal.tools.cli.core;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;

import org.junit.jupiter.api.Test;

import edu.umd.cs.findbugs.annotations.NonNull;

public class CLITest {
  static final String[] MODEL_COMMANDS = { "ap", "ar", "catalog", "component-definition", "profile", "poam", "ssp" };
  static final Map<Format, List<Format>> FORMAT_ENTRIES = Map.of(
      Format.XML, Arrays.asList(Format.JSON, Format.YAML),
      Format.JSON, Arrays.asList(Format.XML, Format.JSON),
      Format.YAML, Arrays.asList(Format.XML, Format.JSON));

  @Test
  void testVersionInfo() {
    String[] args = { "--version" };
    evaluateResult(CLI.runCli(args), ExitCode.OK);
  }

  void evaluateResult(@NonNull ExitStatus status, @NonNull ExitCode expectedCode) {
    status.generateMessage(true);
    assertAll(
        () -> assertEquals(expectedCode, status.getExitCode(), "exit code mismatch"),
        () -> assertNull(status.getThrowable(), "expected null Throwable"));
  }

  void evaluateResult(@NonNull ExitStatus status, @NonNull ExitCode expectedCode,
      @NonNull Class<? extends Throwable> thrownClass) {
    status.generateMessage(true);
    Throwable thrown = status.getThrowable();
    assert thrown != null;
    assertAll(
        () -> assertEquals(expectedCode, status.getExitCode(), "exit code mismatch"),
        () -> assertEquals(thrownClass, thrown.getClass(), "expected Throwable mismatch"));
  }

  @Test
  void testValidateSubcommandsHelp() {
    for (String cmd : MODEL_COMMANDS) {
      String[] args = { cmd, "validate", "-h" };
      evaluateResult(CLI.runCli(args), ExitCode.OK);
    }
  }

  @Test
  void testConvertSubcommandsHelp() {
    for (String cmd : MODEL_COMMANDS) {
      String[] args = { cmd, "convert", "-h" };
      // TODO: Update when usnistgov/oscal-cli#210 fix merged.
      evaluateResult(CLI.runCli(args), ExitCode.INVALID_COMMAND);
    }
  }

  @Test
  void testValidateSubCommandInvalidFile() throws IOException, URISyntaxException {
    for (String cmd : MODEL_COMMANDS) {
      for (Format format : Format.values()) {
        URL url = getClass().getResource("/cli/example_" + cmd + "_invalid" + format.getDefaultExtension());
        String path = Path.of(url.toURI()).toString();
        String[] args = { cmd, "validate", path };
        ExitStatus result = CLI.runCli(args);
        evaluateResult(result, ExitCode.FAIL);
      }
    }
  }

  @Test
  void testValidateSubCommandValidFile() throws IOException, URISyntaxException {
    for (String cmd : MODEL_COMMANDS) {
      for (Format format : Format.values()) {
        URL url = getClass().getResource("/cli/example_" + cmd + "_valid" + format.getDefaultExtension());
        String path = Path.of(url.toURI()).toString();
        String[] args = { cmd, "validate", path };
        ExitStatus result = CLI.runCli(args);
        evaluateResult(result, ExitCode.OK);
      }
    }
  }

  @Test
  void testConvertSubCommandValidFile() throws IOException, URISyntaxException {
    for (String cmd : MODEL_COMMANDS) {
      for (Entry<Format, List<Format>> entry : FORMAT_ENTRIES.entrySet()) {
        Format sourceFormat = entry.getKey();
        List<Format> targetFormats = entry.getValue();
        for (Format targetFormat : targetFormats) {
          URL url = getClass().getResource("/cli/example_" + cmd + "_valid" + sourceFormat.getDefaultExtension());
          String path = Path.of(url.toURI()).toString();
          String outputPath
              = path.replace(sourceFormat.getDefaultExtension(), "_converted" + targetFormat.getDefaultExtension());
          String[] args
              = { cmd, "convert", "--to=" + targetFormat.name().toLowerCase(), path, outputPath, "--overwrite" };
          ExitStatus result = CLI.runCli(args);
          evaluateResult(result, ExitCode.OK);
        }
      }
    }
  }

  @Test
  void testResolveSubCommandValidFile() throws IOException, URISyntaxException {
    for (Format format : Format.values()) {
      URL url = getClass().getResource("/cli/example_profile_valid" + format.getDefaultExtension());
      String path = Path.of(url.toURI()).toString();
      String[] args = { "profile", "resolve", "--to=" + format.name().toLowerCase(), path };
      ExitStatus result = CLI.runCli(args);
      evaluateResult(result, ExitCode.OK);
    }
  }

  @Test
  void testResolveSubCommandInvalidFile() throws IOException, URISyntaxException {
    // TODO: Test all data formats once usnistgov/oscal-cli#216 fix merged.
    URL url = getClass().getResource("/cli/example_profile_invalid" + Format.XML.getDefaultExtension());
    String path = Path.of(url.toURI()).toString();
    String[] args = { "profile", "resolve", "--to=" + Format.XML.name().toLowerCase(), path };
    ExitStatus result = CLI.runCli(args);
    evaluateResult(result, ExitCode.PROCESSING_ERROR, ProfileResolutionException.class);
  }
}
