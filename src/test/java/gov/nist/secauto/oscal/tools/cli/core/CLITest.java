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

import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import edu.umd.cs.findbugs.annotations.NonNull;

public class CLITest {
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

  private static Stream<Arguments> providesValues() {
    final String[] commands = { "ap", "ar", "catalog", "component-definition", "profile", "poam", "ssp" };
    final Map<Format, List<Format>> formatEntries = Map.of(
        Format.XML, Arrays.asList(Format.JSON, Format.YAML),
        Format.JSON, Arrays.asList(Format.XML, Format.JSON),
        Format.YAML, Arrays.asList(Format.XML, Format.JSON));
    List<Arguments> values = new ArrayList<>();

    values.add(Arguments.of(new String[] { "--version" }, ExitCode.OK, null));
    // TODO: Test all data formats once usnistgov/oscal-cli#216 fix merged.
    Path path = Paths.get("src/test/resources/cli/example_profile_invalid" + Format.XML.getDefaultExtension());
    values.add(
        Arguments.of(new String[] { "profile", "resolve", "--to=" + Format.XML.name().toLowerCase(), path.toString() },
            ExitCode.PROCESSING_ERROR, ProfileResolutionException.class));

    for (String cmd : commands) {
      values.add(Arguments.of(new String[] { cmd, "validate", "-h" }, ExitCode.OK, null));
      // TODO: Update when usnistgov/oscal-cli#210 fix merged.
      values.add(Arguments.of(new String[] { cmd, "convert", "-h" }, ExitCode.INVALID_COMMAND, null));

      for (Format format : Format.values()) {
        path = Paths.get("src/test/resources/cli/example_" + cmd + "_invalid" + format.getDefaultExtension());
        values.add(Arguments.of(new String[] { cmd, "validate", path.toString() }, ExitCode.FAIL, null));
        path = Paths.get("src/test/resources/cli/example_" + cmd + "_valid" + format.getDefaultExtension());
        values.add(Arguments.of(new String[] { cmd, "validate", path.toString() }, ExitCode.OK, null));
        path = Paths.get("src/test/resources/cli/example_profile_valid" + format.getDefaultExtension());
        List<Format> targetFormats = formatEntries.get(format);
        for (Format targetFormat : targetFormats) {
          path = Paths.get("src/test/resources/cli/example_" + cmd + "_valid" + format.getDefaultExtension());
          String outputPath = path.toString().replace(format.getDefaultExtension(),
              "_converted" + targetFormat.getDefaultExtension());
          values.add(Arguments.of(new String[] { cmd, "convert", "--to=" + targetFormat.name().toLowerCase(),
              path.toString(), outputPath, "--overwrite" }, ExitCode.OK, null));
          // TODO: Update when usnistgov/oscal#217 fix merged.
          path = Paths.get("src/test/resources/cli/example_" + cmd + "_invalid" + format.getDefaultExtension());
          outputPath = path.toString().replace(format.getDefaultExtension(),
              "_converted" + targetFormat.getDefaultExtension());
          values.add(Arguments.of(new String[] { cmd, "convert", "--to=" + targetFormat.name().toLowerCase(),
              path.toString(), outputPath, "--overwrite" }, ExitCode.OK, null));
        }
        if (cmd == "profile") {
          path = Paths.get("src/test/resources/cli/example_profile_valid" + format.getDefaultExtension());
          values
              .add(Arguments.of(new String[] { cmd, "resolve", "--to=" + format.name().toLowerCase(), path.toString() },
                  ExitCode.OK, null));
        }
      }
    }

    return values.stream();
  }

  @ParameterizedTest
  @MethodSource("providesValues")
  void testAllSubCommands(@NonNull String[] args, @NonNull ExitCode expectedExitCode,
      Class<? extends Throwable> expectedThrownClass) {
    if (expectedThrownClass == null) {
      evaluateResult(CLI.runCli(args), expectedExitCode);
    } else {
      evaluateResult(CLI.runCli(args), expectedExitCode, expectedThrownClass);
    }
  }
}
