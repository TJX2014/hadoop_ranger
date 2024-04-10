package org.apache.hadoop;

import java.lang.reflect.Field;
import java.util.Map;

public class EnvUtils {

  public static void setEnv(Map<String, String> newenv, boolean clearExisting) {
    try {
      Map<String, String> env = System.getenv();
      Class<?> clazz = env.getClass();
      Field field = clazz.getDeclaredField("m");
      field.setAccessible(true);
      Map<String, String> map = (Map<String, String>) field.get(env);
      if (clearExisting) {
        map.clear();
      }
      map.putAll(newenv);

      // only for Windows
      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
      try {
        Field theCaseInsensitiveEnvironmentField =
            processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
        theCaseInsensitiveEnvironmentField.setAccessible(true);
        Map<String, String> cienv =
            (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
        if (clearExisting) {
          cienv.clear();
        }
        cienv.putAll(newenv);
      } catch (NoSuchFieldException ignored) {
      }

    } catch (Exception e1) {
      throw new RuntimeException(e1);
    }
  }
}
