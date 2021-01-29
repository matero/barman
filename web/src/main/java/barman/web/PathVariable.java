package barman.web;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public final class PathVariable<T> extends RequestValueReader<T>
{
  public static final class NotDefined extends RuntimeException
  {
    public final HttpServletRequest request;

    NotDefined(final String parameterName, final HttpServletRequest request)
    {
      super(parameterName);
      this.request = request;
    }
  }

  PathVariable(final String name, final ValueInterpreter<T> interpretValue) { super(name, interpretValue); }

  @Override public final boolean isDefinedAt(final HttpServletRequest request)
  {
    final Enumeration<String> attributeNames = request.getAttributeNames();
    while (attributeNames.hasMoreElements()) {
      final String attributeName = attributeNames.nextElement();
      if (name.equals(attributeName)) {
        return true;
      }
    }
    return false;
  }

  @Override protected final T valueUndefined(final HttpServletRequest request) { throw new NotDefined(name, request); }

  @Override protected final String read(final HttpServletRequest request) { return (String) request.getAttribute(name); }
}
