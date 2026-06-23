package com.socotra.coremodel;

import com.socotra.deployment.DeploymentFactory;
import java.util.Arrays;

public enum PluginType {
  rating("com.socotra.deployment.customer.RatePlugin", "RatePluginStub", "rate", "statelessRate"),
  underwriting(
      "com.socotra.deployment.customer.UnderwritingPlugin",
      "UnderwritingPluginStub",
      "underwrite",
      "statelessUnderwrite"),
  validation(
      "com.socotra.deployment.customer.ValidationPlugin",
      "ValidationPluginStub",
      "validate",
      "statelessValidate"),
  documentSelection(
      "com.socotra.deployment.customer.DocumentSelectionPlugin",
      "DocumentSelectionPluginStub",
      "selectDocuments"),
  documentDataSnapshot(
      "com.socotra.deployment.customer.DocumentDataSnapshotPlugin",
      "DocumentDataSnapshotPluginStub",
      "dataSnapshot"),
  preCommit("com.socotra.deployment.customer.PreCommitPlugin", "PreCommitPluginStub", "preCommit"),

  renewal("com.socotra.deployment.customer.RenewalPlugin", "RenewalPluginStub", "renew"),

  delinquencyEvent(
      "com.socotra.deployment.customer.DelinquencyEventPlugin",
      "DelinquencyEventPluginStub",
      "triggerDelinquencyEvent"),

  documentConsolidationSnapshot(
      "com.socotra.deployment.customer.DocumentConsolidationSnapshotPlugin",
      "DocumentConsolidationSnapshotPluginStub",
      "consolidate"),

  installments(
      "com.socotra.deployment.customer.InstallmentsPlugin",
      "InstallmentsPluginStub",
      "updateInstallments"),

  autopay("com.socotra.deployment.customer.AutopayPlugin", "AutopayPluginStub", "autopay"),
  paymentPostProcessing(
      "com.socotra.deployment.customer.PaymentPostProcessingPlugin",
      "PaymentPostProcessingPluginStub",
      "postProcess"),
  cancellation(
      "com.socotra.deployment.customer.CancellationPlugin", "CancellationPluginStub", "cancel"),
  // automation PluginTypes contains dummy values - Automation Plugins are user-configured
  automationHttp(
      "com.socotra.deployment.customer.AutomationPlugin", "AutomationPluginStub", "action"),
  automationWebhook(
      "com.socotra.deployment.customer.AutomationPlugin",
      "AutomationPluginStub",
      "handleWebhookEvent"),
  documentConsolidationSelection(
      "com.socotra.deployment.customer.DocumentConsolidationSelectionPlugin",
      "DocumentConsolidationSelectionPluginStub",
      "selectDocuments"),
  workplanSelection(
      "com.socotra.deployment.customer.WorkplanSelectionPlugin",
      "WorkplanSelectionPluginStub",
      "selectWorkplans"),
  workplanExecution(
      "com.socotra.deployment.customer.WorkplanExecutionPlugin",
      "WorkplanExecutionPluginStub",
      "decorateWorkplanExecution"),
  deserialization(
      "com.socotra.deployment.customer.DeserializationPlugin",
      "DeserializationPluginStub",
      "deserialize"),
  configMigration(
      "com.socotra.deployment.customer.ConfigMigrationPlugin",
      "ConfigMigrationPluginStub",
      "migrate");

  private final String className;
  private final String stubName;
  private final String methodName;
  private final String statelessMethodName;

  PluginType(String className, String stubName, String methodName) {
    this(className, stubName, methodName, methodName);
  }

  PluginType(String className, String stubName, String methodName, String statelessMethodName) {
    this.className = className;
    this.methodName = methodName;
    this.stubName = stubName;
    this.statelessMethodName = statelessMethodName;
  }

  public String getStubName() {
    return className + "$" + stubName;
  }

  public String getClassName() {
    return className;
  }

  public String getMethodName() {
    return methodName;
  }

  public String getStatelessMethodName() {
    return statelessMethodName;
  }

  public String getRequestName(String prefix) {
    return className + "$" + prefix + "Request";
  }

  public static PluginType forMethod(String methodName) {
    return Arrays.stream(PluginType.values())
        .filter(
            t ->
                t.getMethodName().equals(methodName)
                    || t.getStatelessMethodName().equals(methodName))
        .findFirst()
        .orElse(null);
  }

  public static PluginType forClassName(String className) {
    String classNameWithPackage =
        className.startsWith(DeploymentFactory.DEPLOYMENT_PACKAGE_NAME)
            ? className
            : DeploymentFactory.DEPLOYMENT_PACKAGE_NAME + "." + className;
    return Arrays.stream(PluginType.values())
        .filter(t -> t.getClassName().equals(classNameWithPackage))
        .findFirst()
        .orElse(null);
  }
}
