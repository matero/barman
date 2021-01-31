/*
The MIT License

Copyright (c) Juan José GIL (matero _at_ gmail _dot_ com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package barman.web;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

/**
 Values to be read from query parmeters hold at request.

 @param <T> Type of the query parameter after interpretation. */
public abstract class QueryParameter<T>
    extends RequestValueReader<T>
{
  /**
   Construct a {@link QueryParameter} instance

   @param name        the name of the represented parameter.
   @param interpreter how should the raw value of the query parameter be interpreted as an instance of {@code T}?
   */
  private QueryParameter(
      final String name,
      final ValueInterpreter<T> interpreter)
  {
    super(name, interpreter);
  }

  /**
   @param request {@link HttpServletRequest} where to check if the value is defined or not.
   @return if the {@code request} parameters map holds this query parameter's name.
   */
  public final boolean isDefinedAt(final HttpServletRequest request)
  {
    return request.getParameterMap().containsKey(name);
  }

  protected final String read(final HttpServletRequest request)
  {
    return request.getParameter(name);
  }

  /**
   Required query parameters.
   <p>
   When they are undefined under a request, the query parameter read fails and throws an exception.

   @param <T> Type of the represented query parameter.
   */
  public static final class Required<T>
      extends QueryParameter<T>
  {
    /**
     Construct a {@link QueryParameter} instance.

     @param name        the name of the represented parameter.
     @param interpreter how should the raw value of the query parameter be interpreted as an instance of {@code T}?
     */
    Required(
        final String name,
        final ValueInterpreter<T> interpreter)
    {
      super(name, interpreter);
    }

    /**
     @param request the {@link HttpServletRequest} which doesn't have this parameter definition.
     @return nothing, it never returns, it always throws an instance of {@link ValueNotDefined}.
     @throws ValueNotDefined with this parameter's name and the request which doesn't hold this parameter definition; ALWAYS!
     */
    @Override protected T valueUndefined(final HttpServletRequest request)
    {
      throw new ValueNotDefined(name, request);
    }
  }

  /**
   Not required query parameters.
   <p>
   When they are undefined under a request, the query parameter is interpreted as defined by its default value.

   @param <T> Type of the represented query parameter.
   */
  public static final class NotRequiredWithConstantDefaultValue<T>
      extends QueryParameter<T>
  {

    /** query parameter default value to use when its undefined in a {@link HttpServletRequest}. */
    private final T defaultValue;

    /**
     Construct a {@link QueryParameter.NotRequiredWithConstantDefaultValue} instance

     @param name         the name of the represented parameter.
     @param interpreter  how should the raw value of the query parameter be interpreted as an instance of {@code T}?
     @param defaultValue value to use when the query parameter is undefined under an {@link HttpServletRequest}.
     */
    NotRequiredWithConstantDefaultValue(
        final String name,
        final ValueInterpreter<T> interpreter,
        final T defaultValue)
    {
      super(name, interpreter);
      this.defaultValue = defaultValue;
    }

    /** @return the instance's {@code defaultValue}. */
    @Override protected T valueUndefined(final HttpServletRequest request)
    {
      return defaultValue;
    }
  }

  /**
   Not required query parameters.
   <p>
   When they are undefined under a request, the query parameter is interpreted as defined by its default value supplier.

   @param <T> Type of the represented query parameter.
   */
  public static final class NotRequiredWithSuppliedDefaultValue<T>
      extends QueryParameter<T>
  {

    /** query parameter value supplier to use when its undefined in a {@link HttpServletRequest}. */
    private final Supplier<T> defaultValue;

    /**
     Construct a {@link NotRequiredWithSuppliedDefaultValue} instance

     @param name         the name of the represented parameter.
     @param interpreter  how should the raw value of the query parameter be interpreted as an instance of {@code T}?
     @param defaultValue value supplier to use when the query parameter is undefined under an {@link HttpServletRequest}.
     */
    NotRequiredWithSuppliedDefaultValue(
        final String name,
        final ValueInterpreter<T> interpreter,
        final Supplier<T> defaultValue)
    {
      super(name, interpreter);
      this.defaultValue = defaultValue;
    }

    /** @return the instance's {@code defaultValue} supplied value. */
    @Override protected T valueUndefined(final HttpServletRequest request)
    {
      return defaultValue.get();
    }
  }
}
