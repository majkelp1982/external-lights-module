package pl.smarthouse.externallightsmodule.exceptions;

public class BadZoneNameException extends RuntimeException {
  public BadZoneNameException(final String response) {
    super(response);
  }
}
