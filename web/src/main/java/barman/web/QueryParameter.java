package barman.web;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

public final class QueryParameter<T>
    extends RequestValueReader<T>
{
  public static final class NotDefined
      extends RuntimeException
  {
    public final HttpServletRequest request;

    NotDefined(
        final String parameterName,
        final HttpServletRequest request)
    {
      super(parameterName);
      this.request = request;
    }
  }

  private final boolean required;
  private final Supplier<T> defaultValue;

  QueryParameter(
      final String name,
      final boolean required,
      final ValueInterpreter<T> interpreter,
      final Supplier<T> defaultValue)
  {
    super(name, interpreter);
    this.required = required;
    this.defaultValue = defaultValue;
  }

  public final boolean isDefinedAt(final HttpServletRequest request)
  {
    return request.getParameterMap().containsKey(name);
  }

  @Override protected final T valueUndefined(final HttpServletRequest request)
  {
    if (required) {
      throw new NotDefined(name, request);
    }
    return defaultValue.get();
  }

  protected final String read(final HttpServletRequest request)
  {
    return request.getParameter(name);
  }
}
