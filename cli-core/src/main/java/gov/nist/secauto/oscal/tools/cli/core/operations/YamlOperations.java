package gov.nist.secauto.oscal.tools.cli.core.operations;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class YamlOperations {
  private static final Yaml YAML_PARSER = new Yaml(new Constructor(), new Representer(), new DumperOptions(), new Resolver() {
    @Override
    protected void addImplicitResolvers() {
      addImplicitResolver(Tag.BOOL, BOOL, "yYnNtTfFoO");
      addImplicitResolver(Tag.INT, INT, "-+0123456789");
      addImplicitResolver(Tag.FLOAT, FLOAT, "-+0123456789.");
      addImplicitResolver(Tag.MERGE, MERGE, "<");
      addImplicitResolver(Tag.NULL, NULL, "~nN\0");
      addImplicitResolver(Tag.NULL, EMPTY, null);
      // addImplicitResolver(Tag.TIMESTAMP, TIMESTAMP, "0123456789");
    }

  });

  private YamlOperations() {
    // disable construction
  }

  @SuppressWarnings("unchecked")
  @NotNull
  public static Map<String, Object> parseYaml(Path target) throws IOException {
    try (BufferedReader reader = Files.newBufferedReader(target)) {
      return (Map<String, Object>)YAML_PARSER.load(reader);
    }
  }

  public static JSONObject yamlToJson(@NotNull Map<String, Object> map) throws JSONException {
    return new JSONObject(map);
  }
}
