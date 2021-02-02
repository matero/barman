/*
The MIT License

Copyright (c) 2021 Juan J. GIL (matero _at_ gmail _dot_ com)

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

/**
 Variable defined as part of a dynamic path.
 <p>
 Samples of this values are {@code "/sara/catung/{id}"} where {@code "{id}"} part MUST be represented as a {@link PathVariable} instance with name
 {@code "id"}.

 @param <T> expected type of the {@link PathVariable} when it is interpreted from a raw {@link String} in the slug of the path. */
public final class PathVariable<T>
    extends RequestValueReader<T>
{
  /**
   Construct a {@link PathVariable} instance.

   @param name        the name of the represented parameter.
   @param interpreter how should the raw value of the path variable be interpreted as an instance of {@code T}?
   */
  PathVariable(
      final String name,
      final ValueInterpreter<T> interpreter)
  {
    super(name, interpreter);
  }

  /**
   Verifies if the request has an attribute representing this parameter.
   <p>
   When a dynamic path is matched, all the path variables recognized in it are added to the request as attributes, so checking that a the variable is
   defined is as simple as checking that an attribute named as it exists under request attribute names.

   @param request {@link HttpServletRequest} where to check if the value is defined or not.
   @return {@literal true} if the variable is defined under the {@code request}; {@literal false} other way.
   */
  @Override public boolean isDefinedAt(final HttpServletRequest request)
  {
    final var attributeNames = request.getAttributeNames();
    while (attributeNames.hasMoreElements()) {
      final var attributeName = attributeNames.nextElement();
      if (name.equals(attributeName)) {
        return true;
      }
    }
    return false;
  }

  /**
   @param request the {@link HttpServletRequest} which doesn't have this variable definition.
   @return nothing, it never returns, it always throws an instance of {@link ValueNotDefined}.
   @throws ValueNotDefined with this variable's name and the request which doesn't hold this parameter definition; ALWAYS!
   */
  @Override protected T valueUndefined(final HttpServletRequest request)
  {
    throw new ValueNotDefined(name, request);
  }

  /**
   Gets the value of the request's attribute named after this variable name.

   @param request {@link HttpServletRequest} where to get the attribute value.
   @return request's attribute named after this variable name, as an {@link String}.
   */
  @Override protected String read(final HttpServletRequest request)
  {
    return request.getAttribute(name).toString();
  }
}
