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
package barman.processors;

import barman.web.LoggedUser;

import javax.lang.model.element.ExecutableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PathSpec
{
  // Matches: {id} AND {id: .*?}
  // group(1) extracts the name of the group (in that case "id").
  // group(3) extracts the regex if defined
  private static final Pattern PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE = Pattern.compile("\\{(.*?)(:\\s(.*?))?\\}");

  // This regex matches everything in between path slashes.
  private static final String VARIABLE_ROUTES_DEFAULT_REGEX = "(?<%s>[^/]+)";

  final String path;
  final String pattern;
  final String regex;
  final List<String> parameters;

  PathSpec(
      final String path,
      final String pattern)
  {
    this(path, pattern, null, List.of());
  }

  PathSpec(
      final String path,
      final String pattern,
      final String regex,
      final List<String> parameters)
  {
    this.path = path;
    this.pattern = pattern;
    this.regex = regex;
    this.parameters = parameters;
  }

  static PathSpec from(
      final String uri,
      final String urlPattern)
  {
    final var regex = findRegex(urlPattern);
    if (regex == null) {
      return new PathSpec(uri, urlPattern);
    } else {
      return new PathSpec(uri, urlPattern, regex, findParameterNames(urlPattern));
    }
  }

  /**
   * Transforms an url pattern like "/{name}/id/*" into a regex like "/([^/]*)/id/*."
   * <p/>
   * Also handles regular expressions if defined inside endpoints: For instance "/users/{username: [a-zA-Z][a-zA-Z_0-9]}" becomes "/users/
   * ([a-zA-Z][a-zA-Z_0-9])"
   *
   * @return The converted regex with default matching regex - or the regex specified by the user.
   */
  static String findRegex(final String urlPattern)
  {
    final var buffer = new StringBuffer();
    final var matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(urlPattern);
    int pathParameterIndex = 0;

    while (matcher.find()) {
      // By convention group 3 is the regex if provided by the user.
      // If it is not provided by the user the group 3 is null.
      final var parameter = matcher.group(1);
      final var namedVariablePartOfRoute = matcher.group(3);
      final String namedVariablePartOfORouteReplacedWithRegex;

      if (namedVariablePartOfRoute != null) {
        // we convert that into a regex matcher group itself
        final var variableRegex = replacePosixClasses(namedVariablePartOfRoute);
        namedVariablePartOfORouteReplacedWithRegex = String.format("(?<%s>%s)", parameter, Matcher.quoteReplacement(variableRegex));
      } else {
        // we convert that into the default namedVariablePartOfRoute regex group
        namedVariablePartOfORouteReplacedWithRegex = String.format(VARIABLE_ROUTES_DEFAULT_REGEX, parameter);
      }
      // we replace the current namedVariablePartOfRoute group
      matcher.appendReplacement(buffer, namedVariablePartOfORouteReplacedWithRegex);
      pathParameterIndex++;
    }

    if (pathParameterIndex == 0) {
      // when no "dynamic" part found, no regex is found ;)
      return null;
    } else {
      // .. and we append the tail to complete the stringBuffer
      matcher.appendTail(buffer);

      return buffer.toString();
    }
  }

  /**
   * Replace any specified POSIX character classes with the Java equivalent.
   *
   * @param input POSIX character class to be replaced.
   * @return a Java regex
   */
  static String replacePosixClasses(final String input)
  {
    return input
               .replace(":alnum:", "\\p{Alnum}")
               .replace(":alpha:", "\\p{L}")
               .replace(":ascii:", "\\p{ASCII}")
               .replace(":digit:", "\\p{Digit}")
               .replace(":xdigit:", "\\p{XDigit}");
  }

  /**
   * Extracts the name of the parameters from a path
   * <p/>
   * /{my_id}/{my_name}
   * <p/>
   * would return a List with "my_id" and "my_name"
   *
   * @param uriPattern path to work with.
   * @return a list with the names of all parameters in the url pattern
   */
  static List<String> findParameterNames(final String uriPattern)
  {
    final var parameters = new ArrayList<String>(4);
    final var matcher = PATTERN_FOR_VARIABLE_PARTS_OF_ROUTE.matcher(uriPattern);
    while (matcher.find()) {
      // group(1) is the name of the group. Must be always there...
      // "/assets/{file}" and "/assets/{file:[a-zA-Z][a-zA-Z_0-9]}"
      // will return file.
      parameters.add(matcher.group(1));
    }
    return List.copyOf(parameters);
  }

  @Override public int hashCode()
  {
    return this.pattern.hashCode();
  }

  @Override public boolean equals(final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o instanceof PathSpec) {
      return pattern.equals(((PathSpec) o).pattern);
    }
    return false;
  }

  boolean isStatic()
  {
    return parameters.isEmpty();
  }

  public Route makeRoute(
      final HttpVerb verb,
      final ExecutableElement method)
  {
    final var handler = method.getSimpleName().toString();

    var spec = method.getAnnotation(LoggedUser.class);
    if (spec == null) {
      spec = method.getEnclosingElement().getAnnotation(LoggedUser.class);
    }

    final boolean requiresUserLogged;
    final boolean requiresUserNotLogged;
    final String[] allowedRoles;
    final String[] rejectedRoles;
    if (spec == null) {
      requiresUserLogged = false;
      requiresUserNotLogged = false;
      allowedRoles = Route.NO_ROLES;
      rejectedRoles = Route.NO_ROLES;
    } else {
      allowedRoles = spec.allowedRoles();
      rejectedRoles = spec.rejectedRoles();
      requiresUserLogged = allowedRoles.length > 0;
      requiresUserNotLogged = rejectedRoles.length == 1 && "*".equals(rejectedRoles[0]);

      if (requiresUserLogged && requiresUserNotLogged) {
        throw new IllegalStateException("or user is required to be logged or to be not logged, but not both!");
      }
    }

    if (isStatic()) {
      return new Route(path, verb, pattern, requiresUserLogged, requiresUserNotLogged, allowedRoles, rejectedRoles, handler);
    } else {
      return new Route(path, verb, pattern, regex, requiresUserLogged, requiresUserNotLogged, allowedRoles, rejectedRoles, handler, parameters);
    }
  }
}
