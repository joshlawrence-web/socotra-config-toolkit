package com.socotra.deployment.plugins;

public class AutomationPluginException extends RuntimeException {

  private int httpStatusCode;

  public AutomationPluginException(int statusCode, String message) {
    super(message);
    if (statusCode < 100 || statusCode >= 600) {
      throw new IllegalArgumentException(
          "AutomationPluginException statusCode="
              + statusCode
              + " is outside the valid HttpStatus range. Error detail: "
              + message);
    }
    this.httpStatusCode = statusCode;
  }

  public int getHttpStatusCode() {
    return httpStatusCode;
  }

  @Override
  public String toString() {
    return "Failed response: " + httpStatusCode + " - " + getMessage();
  }
}
