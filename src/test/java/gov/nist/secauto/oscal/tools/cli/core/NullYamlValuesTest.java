
package gov.nist.secauto.oscal.tools.cli.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.nist.secauto.metaschema.binding.io.Format;
import gov.nist.secauto.metaschema.binding.io.IBoundLoader;
import gov.nist.secauto.oscal.lib.OscalBindingContext;
import gov.nist.secauto.oscal.lib.model.AssessmentResults;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

public class NullYamlValuesTest {
  private static OscalBindingContext bindingContext;
  private static IBoundLoader loader;

  @BeforeAll
  static void initialize() { // NOPMD actually used
    bindingContext = OscalBindingContext.instance();
    loader = bindingContext.newBoundLoader();
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar1() throws IOException, URISyntaxException {
    // the YAML catalog is currently malformed, this will create a proper one for this test
    AssessmentResults data
        = loader.load(NullYamlValuesTest.class.getResource("/yaml-null/example_ar_nullvar-1.yaml"));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);
    
    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar2() throws IOException, URISyntaxException {
    // the YAML catalog is currently malformed, this will create a proper one for this test
    AssessmentResults data
        = loader.load(NullYamlValuesTest.class.getResource("/yaml-null/example_ar_nullvar-2.yaml"));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);
    
    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

  @SuppressWarnings("null")
  @Test
  void testLoadYamlNullVar3() throws IOException, URISyntaxException {
    // the YAML catalog is currently malformed, this will create a proper one for this test
    AssessmentResults data
        = loader.load(NullYamlValuesTest.class.getResource("/yaml-null/example_ar_nullvar-3.yaml"));

    bindingContext.newSerializer(Format.XML, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.JSON, AssessmentResults.class).serialize(data, System.out);
    bindingContext.newSerializer(Format.YAML, AssessmentResults.class).serialize(data, System.out);
    
    assertTrue(data.getResults().get(0).getFindings().isEmpty());
  }

}
