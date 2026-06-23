package com.socotra.deployment.customer;

import com.socotra.coremodel.*;
import com.socotra.deployment.*;
import com.socotra.deployment.plugins.ConfigMigrationTransformer;
import java.util.List;
import java.util.Collection;

@Plugin(type = PluginType.configMigration)
public interface ConfigMigrationPlugin {
  default PersonalAccount migrate(ConfigMigrationPlugin.PersonalAccountRequest request, ConfigMigrationTransformer transformer) {
    return transformer.transform(request.account());
  }
  default ZenCoverQuote migrate(ConfigMigrationPlugin.ZenCoverQuoteRequest request, ConfigMigrationTransformer transformer) {
    return transformer.transform(request.quote());
  }
  default ZenCoverSegment migrate(ConfigMigrationPlugin.ZenCoverRequest request, ConfigMigrationTransformer transformer) {
    return transformer.transform(request.segment());
  }

  public record PersonalAccountRequest(Account account) implements PluginRequest {
    public Collection<?> structures() { return List.of(account()); }
  }
  public record ZenCoverQuoteRequest(Quote quote) implements PluginRequest {
    public Collection<?> structures() { return List.of(quote()); }
  }
  public record ZenCoverRequest(Policy policy, Segment segment) implements PluginRequest {
    public Collection<?> structures() { return List.of(policy(), segment()); }
  }
  public static final class ConfigMigrationPluginStub implements ConfigMigrationPlugin {
  }
}