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

import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.core.util.ObjectUtils;
import gov.nist.secauto.metaschema.databind.io.Format;
import gov.nist.secauto.metaschema.databind.io.IBoundLoader;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.AssessmentResults;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class NullYamlValuesTest {
  private static OscalBindingContext bindingContext;
  private static IBoundLoader loader;

  @BeforeAll
  static void initialize() {
    bindingContext = OscalBindingContext.instance();
    loader = bindingContext.newBoundLoader();
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar1() throws IOException {
    // the YAML catalog is currently malformed, this will create a proper one for
    // this test
    AssessmentResults data
        = loader.load(
            ObjectUtils.requireNonNull(Paths.get("src/test/resources/yaml-null/example_ar_nullvar-1.yaml")));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);

    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar2() throws IOException {
    // the YAML catalog is currently malformed, this will create a proper one for
    // this test
    AssessmentResults data
        = loader.load(
            ObjectUtils.requireNonNull(Paths.get("src/test/resources/yaml-null/example_ar_nullvar-2.yaml")));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);

    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar3() throws IOException {
    // the YAML catalog is currently malformed, this will create a proper one for
    // this test
    AssessmentResults data
        = loader.load(
            ObjectUtils.requireNonNull(Paths.get("src/test/resources/yaml-null/example_ar_nullvar-3.yaml")));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);

    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

}
