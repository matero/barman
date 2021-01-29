package barman.web;

import javax.servlet.http.HttpServletRequest;

public abstract class RequestValueReader<T> implements java.io.Serializable
{
  protected final String name;
  protected final ValueInterpreter<T> interpretValue;

  protected RequestValueReader(final String name, final ValueInterpreter<T> interpreter)
  {
    this.name = name;
    this.interpretValue = interpreter;
  }

  public abstract boolean isDefinedAt(HttpServletRequest request);

  public final T of(final HttpServletRequest request)
  {
    if (isDefinedAt(request)) {
      return interpret(read(request));
    } else {
      return valueUndefined(request);
    }
  }

  protected abstract T valueUndefined(HttpServletRequest request);

  protected final T interpret(final String raw) { return interpretValue.from(raw); }

  protected abstract String read(HttpServletRequest request);
}
