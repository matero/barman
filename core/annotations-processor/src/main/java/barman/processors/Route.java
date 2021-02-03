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

import barman.web.RouterServlet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.regex.Pattern;

final class Route
{
  public static final int BASIC_PARAMETERS_COUNT = 4;
  public static final int HASH_PRIME = 17;
  private static final ClassName PATTERN_CLASS_NAME = ClassName.get(Pattern.class);
  private static final String NO_REGEX = "";
  private static final String[] NO_PARAMETERS = {};
  static final String[] NO_ROLES = NO_PARAMETERS;
  private static final String[] OF_PARAMS = NO_PARAMETERS;
  final HttpVerb verb;
  final String path;
  final String pattern;
  final String regex;
  final String handler;
  final String[] parameters;
  final boolean requiresUserLogged;
  final boolean requiresUserNotLogged;
  final String[] allowedRoles;
  final String[] rejectedRoles;

  @SuppressWarnings("checkstyle:parameterNumber") Route(
      final String path,
      final HttpVerb verb,
      final String uri,
      final boolean requiresUserLogged,
      final boolean requiresUserNotLogged,
      final String[] allowedRoles,
      final String[] rejectedRoles,
      final String handler)
  {
    this(path, verb, uri, NO_REGEX, requiresUserLogged, requiresUserNotLogged, allowedRoles, rejectedRoles, handler, NO_PARAMETERS);
  }

  @SuppressWarnings("checkstyle:parameterNumber") Route(
      final String path,
      final HttpVerb verb,
      final String pattern,
      final String regex,
      final boolean requiresUserLogged,
      final boolean requiresUserNotLogged,
      final String[] allowedRoles,
      final String[] rejectedRoles,
      final String handler,
      final List<String> parameters)
  {
    this(path, verb, pattern, regex, requiresUserLogged, requiresUserNotLogged, allowedRoles, rejectedRoles, handler, parameters.toArray(OF_PARAMS));
  }

  @SuppressWarnings("checkstyle:parameterNumber")
  private Route(
      final String path,
      final HttpVerb verb,
      final String pattern,
      final String regex,
      final boolean requiresUserLogged,
      final boolean requiresUserNotLogged,
      final String[] allowedRoles,
      final String[] rejectedRoles,
      final String handler,
      final String[] parameters)
  {
    this.path = path;
    this.verb = verb;
    this.pattern = pattern;
    this.regex = regex;
    this.requiresUserLogged = requiresUserLogged;
    this.requiresUserNotLogged = requiresUserNotLogged;
    if (allowedRoles == null) {
      this.allowedRoles = NO_ROLES;
    } else {
      this.allowedRoles = allowedRoles;
    }
    if (rejectedRoles == null) {
      this.rejectedRoles = NO_ROLES;
    } else {
      this.rejectedRoles = rejectedRoles;
    }
    this.parameters = parameters;
    this.handler = handler;
  }

  @Override public int hashCode()
  {
    int hash = verb.hashCode();
    hash = HASH_PRIME * hash + pattern.hashCode();
    return hash;
  }

  @Override public boolean equals(final Object o)
  {
    if (this == o) {
      return true;
    }
    if (o instanceof Route) {
      final var other = (Route) o;
      return this.verb == other.verb && pattern.equals(other.pattern);
    }
    return false;
  }

  @Override public String toString()
  {
    return "Route(verb=" + verb + ", pattern='" + pattern + "')";
  }

  boolean hasRoleConstrains()
  {
    return this.allowedRoles.length > 0;
  }

  String routeField()
  {
    return this.verb.name() + '_' + handler;
  }

  boolean isParameterized()
  {
    return parameters.length > 0;
  }

  int parametersCount()
  {
    return parameters.length;
  }

  MethodSpec.Builder makeMatcher(final MethodSpec.Builder httpVerbHandler)
  {
    if (isIndex()) {
      return httpVerbHandler.beginControlFlow("if (indexPath.matches(request))");
    } else {
      return httpVerbHandler.beginControlFlow("if ($L.matches(request))", routeField());
    }
  }

  boolean isIndex()
  {
    return pattern == null || "".equals(pattern) || "/".equals(pattern);
  }


  @SuppressWarnings("checkstyle:magicNumber")
  FieldSpec makeField()
  {
    if (isIndex()) {
      return null;
    }

    final var pkg = RouterServlet.class.getPackage().getName();
    final var servlet = RouterServlet.class.getSimpleName();

    final var pathClassName = ClassName.get(pkg, servlet, "Path");
    final var property = FieldSpec.builder(pathClassName, routeField(), Modifier.PRIVATE, Modifier.FINAL);

    if (isParameterized()) {
      final var extraArgs = ", $S".repeat(parametersCount() - 1);
      final var code = "path($S, $S, $T.compile($S), $S" + extraArgs + ')';
      final var args = new Object[BASIC_PARAMETERS_COUNT + parametersCount()];
      args[0] = path;
      args[1] = pattern;
      args[2] = PATTERN_CLASS_NAME;
      args[3] = regex;
      {
        int i = BASIC_PARAMETERS_COUNT;
        for (final var parameter : parameters) {
          args[i++] = parameter;
        }
      }

      property.initializer(code, args);
    } else {
      property.initializer("path($S, $S)", this.path, pattern);
    }
    return property.build();
  }

  boolean hasOneAllowedRole()
  {
    return allowedRoles.length == 1;
  }

  boolean hasManyAllowedRole()
  {
    return allowedRoles.length > 1;
  }

  String allowedRole()
  {
    return allowedRoles[0];
  }

  boolean hasOneRejectedRole()
  {
    return rejectedRoles.length == 1;
  }

  boolean hasManyRejectedRole()
  {
    return rejectedRoles.length > 1;
  }

  String rejectedRole()
  {
    return rejectedRoles[0];
  }
}
