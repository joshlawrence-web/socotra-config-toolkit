package com.socotra.coremodel;

public enum ResourceType {
  table("tables"),
  rangeTable("rangeTables"),
  constraintTable("constraintTables"),
  secret("secrets"),
  documentTemplate("documentTemplates"),
  staticDocument("staticDocuments"),
  documentTemplateSnippet("documentTemplateSnippets"),
  customFont("customFonts"),
  prompt("prompts"),
  riskAssessmentCriteria("riskAssessmentCriteria"),
  uiConfig("uiConfigs");

  private final String pluralName;

  ResourceType(String pluralName) {
    this.pluralName = pluralName;
  }

  public String pluralName() {
    return pluralName;
  }
}
