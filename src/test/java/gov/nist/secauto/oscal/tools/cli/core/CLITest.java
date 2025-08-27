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

import gov.nist.secauto.metaschema.cli.processor.ExitCode;
import gov.nist.secauto.metaschema.cli.processor.ExitStatus;
import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.oscal.lib.profile.resolver.ProfileResolutionException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
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
    assertAll(
        () -> assertEquals(expectedCode, status.getExitCode(), "exit code mismatch"),
        () -> assertEquals(
            thrownClass,
            thrown == null ? null : thrown.getClass(),
            "Throwable mismatch"));
  }

  private static String generateOutputPath(@NonNull Path source, @NonNull Format targetFormat) throws IOException {
    String filename = ObjectUtils.notNull(source.getFileName()).toString();

    int pos = filename.lastIndexOf('.');
    filename = filename.substring(0, pos) + "_converted" + targetFormat.getDefaultExtension();

    Path dir = Files.createDirectories(Path.of("target/oscal-cli-convert"));

    return dir.resolve(filename).toString();
  }

  private static Stream<Arguments> providesValues() throws IOException {
    final String[] commands = { "ap", "ar", "catalog", "component-definition", "profile", "poam", "ssp" };
    final Map<Format, List<Format>> formatEntries = Map.of(
        Format.XML, Arrays.asList(Format.JSON, Format.YAML),
        Format.JSON, Arrays.asList(Format.XML, Format.JSON),
        Format.YAML, Arrays.asList(Format.XML, Format.JSON));
    List<Arguments> values = new ArrayList<>();

    values.add(Arguments.of(new String[] { "--version" }, ExitCode.OK, null));
    for (String cmd : commands) {
      // test helps
      values.add(Arguments.of(new String[] { cmd, "validate", "-h" }, ExitCode.OK, null));
      values.add(Arguments.of(new String[] { cmd, "convert", "-h" }, ExitCode.OK, null));

      for (Format format : Format.values()) {
        String sourceExtension = format.getDefaultExtension();
        values.add(
            Arguments.of(
                new String[] {
                    cmd,
                    "validate",
                    Paths.get("src/test/resources/cli/example_" + cmd + "_invalid" + sourceExtension).toString()
                },
                ExitCode.FAIL,
                null));
        values.add(
            Arguments.of(
                new String[] {
                    cmd,
                    "validate",
                    Paths.get("src/test/resources/cli/example_" + cmd + "_valid" + sourceExtension).toString()
                },
                ExitCode.OK,
                null));

        for (Format targetFormat : formatEntries.get(format)) {
          Path path = Paths.get("src/test/resources/cli/example_" + cmd + "_valid" + sourceExtension);
          values.add(
              Arguments.of(
                  new String[] {
                      cmd,
                      "convert",
                      "--to=" + targetFormat.name().toLowerCase(),
                      path.toString(),
                      generateOutputPath(path, targetFormat),
                      "--overwrite"
                  },
                  ExitCode.OK,
                  null));

          // TODO: Update when usnistgov/oscal#217 fix merged.
          path = Paths.get("src/test/resources/cli/example_" + cmd + "_invalid" + sourceExtension);
          values.add(
              Arguments.of(
                  new String[] {
                      cmd,
                      "convert",
                      "--to=" + targetFormat.name().toLowerCase(),
                      path.toString(),
                      generateOutputPath(path, targetFormat),
                      "--overwrite"
                  },
                  ExitCode.OK,
                  null));
        }
        if (cmd == "profile") {
          values.add(
              Arguments.of(
                  new String[] {
                      cmd,
                      "resolve",
                      "--to=" + format.name().toLowerCase(),
                      Paths.get("src/test/resources/cli/example_profile_valid" + sourceExtension).toString()
                  },
                  ExitCode.OK,
                  null));
          values.add(
              Arguments.of(
                  new String[] {
                      "profile",
                      "resolve",
                      "--to=" + format.name().toLowerCase(),
                      Paths.get("src/test/resources/cli/example_profile_invalid" + sourceExtension).toString()
                  },
                  ExitCode.PROCESSING_ERROR,
                  ProfileResolutionException.class));
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
