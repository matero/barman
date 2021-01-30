/*
 * The MIT License
 *
 * Copyright (c) 2021 Juan José GIL.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package barman.web.processor;

import barman.web.RouterServlet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.regex.Pattern;

final class Route {
  private static final ClassName PATTERN_CLASS_NAME = ClassName.get(Pattern.class);
  private static final String NO_PATTERN = "";
  private static final String NO_REGEX = "";
  private static final String[] NO_PARAMETERS = {};
  private static final String[] OF_PARAMS = NO_PARAMETERS;
  static final String[] NO_ROLES = NO_PARAMETERS;

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

  Route(
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

  Route(
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
    this.requiresUserLogged=requiresUserLogged;
    this.requiresUserNotLogged=requiresUserNotLogged;
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
    int hash = 3;
    hash = 17 * hash + this.verb.hashCode();
    hash = 17 * hash + this.pattern.hashCode();
    return hash;
  }

  @Override public boolean equals(final Object o)
  {
    if (this == o) { return true; }
    if (o instanceof Route) {
      var other = (Route) o;
      return this.verb == other.verb && pattern.equals(other.pattern);
    }
    return false;
  }

  @Override public String toString() { return "Route(verb=" + verb + ", pattern='" + pattern + "')"; }

  boolean hasRoleConstrains() { return this.allowedRoles.length > 0; }

  String routeField() { return this.verb.name() + '_' + handler; }

  boolean isParameterized() { return parameters.length > 0; }

  int parametersCount() { return parameters.length; }

  MethodSpec.Builder makeMatcher(final MethodSpec.Builder httpVerbHandler)
  {
    if (isIndex()) {
      return httpVerbHandler.beginControlFlow("if (indexPath.matches(request))");
    } else {
      return httpVerbHandler.beginControlFlow("if ($L.matches(request))", routeField());
    }
  }

  boolean isIndex() { return pattern == null || "".equals(pattern) || "/".equals(pattern); }

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
      final var args = new Object[4 + parametersCount()];
      args[0] = path;
      args[1] = pattern;
      args[2] = PATTERN_CLASS_NAME;
      args[3] = regex;
      {
        int i = 3;
        for (final var parameter : parameters) {
          args[++i] = parameter;
        }
      }

      property.initializer(code, args);
    } else {
      property.initializer("path($S, $S)", this.path, pattern);
    }
    return property.build();
  }

  boolean hasOneAllowedRole() {
    return allowedRoles.length == 1;
  }

  boolean hasManyAllowedRole() {
    return allowedRoles.length > 1;
  }

  String allowedRole() {
    return allowedRoles[0];
  }

  boolean hasOneRejectedRole() {
    return rejectedRoles.length == 1;
  }

  boolean hasManyRejectedRole() {
    return rejectedRoles.length > 1;
  }

  String rejectedRole() {
    return rejectedRoles[0];
  }
}
