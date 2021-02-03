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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class RoutersCodeBuilder
{
  private static final ClassName LOGGER_CLASS = ClassName.get(Logger.class);
  private static final ClassName LOGGER_FACTORY_CLASS = ClassName.get(LoggerFactory.class);

  static MethodSpec overrideVerbHandlerOnDevelopmentEnvironment(final HttpVerb httpVerb)
  {
    final MethodSpec.Builder httpVerbHandler = MethodSpec
                                                   .methodBuilder(httpVerb.handler)
                                                   .addAnnotation(Override.class)
                                                   .addModifiers(Modifier.PUBLIC)
                                                   .addParameter(HttpServletRequest.class, "request", Modifier.FINAL)
                                                   .addParameter(HttpServletResponse.class, "response", Modifier.FINAL)
                                                   .addException(ServletException.class)
                                                   .addException(IOException.class);
    httpVerbHandler.addStatement("response.setHeader(\"Access-Control-Allow-Origin\", \"*\")");
    httpVerbHandler.addStatement("super.$L(request, response)", httpVerb.handler);
    return httpVerbHandler.build();
  }

  private static boolean doAllRoutesRequireUserLogged(final List<Route> routes)
  {
    for (final Route r : routes) {
      if (!r.requiresUserLogged) {
        return false;
      }
    }
    return true;
  }

  private static boolean doAllRoutesRequireUserNotLogged(final List<Route> routes)
  {
    for (final Route r : routes) {
      if (!r.requiresUserNotLogged) {
        return false;
      }
    }
    return true;
  }

  private static boolean doAllRoutesHasSameAllowedRoles(final List<Route> routes)
  {
    if (routes.size() < 2) {
      return true;
    }

    final var roles = routes.get(0).allowedRoles;
    for (int i = 1; i < routes.size(); i++) {
      if (!Arrays.equals(roles, routes.get(i).allowedRoles)) {
        return false;
      }
    }
    return true;
  }

  private static boolean doAllRoutesHasSameRejectedRoles(final List<Route> routes)
  {
    if (routes.size() < 2) {
      return true;
    }

    final var roles = routes.get(0).rejectedRoles;
    for (int i = 1; i < routes.size(); i++) {
      if (!Arrays.equals(roles, routes.get(i).rejectedRoles)) {
        return false;
      }
    }
    return true;
  }

  private static void addUserLoggedValidation(final MethodSpec.Builder httpVerbHandler)
  {
    httpVerbHandler.beginControlFlow("if (!userLogged())")
                   .addStatement("notAuthorized(response)")
                   .addStatement("return")
                   .endControlFlow();
  }

  private static void addUserNotLoggedValidation(final MethodSpec.Builder control)
  {
    control.beginControlFlow("if (userLogged())")
           .addStatement("notAuthorized(response)")
           .addStatement("return")
           .endControlFlow();
  }

  private static void addAllowedRolesValidation(
      final MethodSpec.Builder control,
      final Route route)
  {
    if (route.hasOneAllowedRole()) {
      final var role = route.allowedRole();
      if (!"*".equals(role)) {
        control.beginControlFlow("if (!$S.equals(getCurrentUser().role()))", role)
               .addStatement("notAuthorized(response)")
               .addStatement("return")
               .endControlFlow();
      }
    } else if (route.hasManyAllowedRole()) {
      control.beginControlFlow("switch (getCurrentUser().role())");
      for (final var allowedRole : route.allowedRoles) {
        control.addCode("case $S:\n", allowedRole);
      }
      control.addStatement("break");
      control.addCode("default:\n")
             .addStatement("notAuthorized(response)")
             .addStatement("return")
             .endControlFlow();
    }
  }

  JavaFile buildJavaCode(
      final EndPointSpec declarations,
      final boolean isDevelopmentEnvironment)
  {
    final var classname = declarations.routerClassName();
    final var router = TypeSpec.classBuilder(classname)
                               .superclass(declarations.superClass)
                               .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                               .addAnnotation(AnnotationSpec.builder(Generated.class)
                                                            .addMember("value", "$S", "barman/EndpointsCompiler")
                                                            .addMember("comments", "$S", declarations.paths)
                                                            .addMember("date", "$S", declarations.date)
                                                            .build())
                               .addAnnotation(AnnotationSpec.builder(WebServlet.class)
                                                            .addMember("value", "$S", declarations.path)
                                                            .build());
    if (declarations.noLoggerDefined) {
      router.addField(FieldSpec.builder(LOGGER_CLASS, "LOGGER", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                               .initializer("$T.getLogger($S)", LOGGER_FACTORY_CLASS, declarations.superClass.toString())
                               .build());
      router.addMethod(MethodSpec.methodBuilder("logger")
                                 .addAnnotation(Override.class)
                                 .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                                 .returns(Logger.class)
                                 .addStatement("return LOGGER")
                                 .build());
    }
    addRouteFields(router, declarations);
    addRouteHandlers(router, declarations, isDevelopmentEnvironment);
    return JavaFile.builder(classname.packageName(), router.build()).skipJavaLangImports(true).build();
  }

  void addRouteFields(
      final TypeSpec.Builder router,
      final EndPointSpec declarations)
  {
    for (final HttpVerb httpVerb : HttpVerb.values()) {
      final var routes = declarations.routesByVerb.get(httpVerb);
      if (no(routes)) {
        continue;
      }

      String last = null;
      for (final var route : routes) {
        if (!route.pattern.equals(last)) {
          final var routeField = route.makeField();
          if (routeField != null) {
            router.addField(routeField);
            last = route.pattern;
          }
        }
      }
    }
  }

  void addRouteHandlers(
      final TypeSpec.Builder router,
      final EndPointSpec declarations,
      final boolean isDevelopmentEnvironment)
  {
    for (final var httpVerb : HttpVerb.values()) {
      final var routes = declarations.routesByVerb.get(httpVerb);

      if (no(routes)) {
        if (isDevelopmentEnvironment) {
          overrideVerbHandlerOnDevelopmentEnvironment(httpVerb);
        }
      } else {
        router.addMethod(overrideVerbHandler(httpVerb, routes, isDevelopmentEnvironment));
      }
    }
  }

  MethodSpec overrideVerbHandler(
      final HttpVerb httpVerb,
      final List<Route> routes,
      final boolean isDevelopmentEnvironment)
  {
    final var httpVerbHandler = MethodSpec.methodBuilder(httpVerb.handler)
                                          .addAnnotation(Override.class)
                                          .addModifiers(Modifier.PUBLIC)
                                          .addParameter(HttpServletRequest.class, "request", Modifier.FINAL)
                                          .addParameter(HttpServletResponse.class, "response", Modifier.FINAL)
                                          .addException(ServletException.class)
                                          .addException(IOException.class);
    final var userLoggedChecked = doAllRoutesRequireUserLogged(routes);
    if (userLoggedChecked) {
      addUserLoggedValidation(httpVerbHandler);
    }
    final var userNotLoggedChecked = doAllRoutesRequireUserNotLogged(routes);
    if (userNotLoggedChecked) {
      addUserNotLoggedValidation(httpVerbHandler);
    }
    final var allowedRolesChecked = doAllRoutesHasSameAllowedRoles(routes);
    if (allowedRolesChecked) {
      addAllowedRolesValidation(httpVerbHandler, routes.get(0));
    }
    final var rejectedRolesChecked = doAllRoutesHasSameRejectedRoles(routes);
    if (rejectedRolesChecked) {
      addRejectedRolesValidation(httpVerbHandler, routes.get(0));
    }

    for (final var route : routes) {
      final var ifMatchesRoute = route.makeMatcher(httpVerbHandler);
      addHandle(ifMatchesRoute, route, userLoggedChecked, userNotLoggedChecked, allowedRolesChecked, rejectedRolesChecked);
      httpVerbHandler.endControlFlow();
    }

    if (isDevelopmentEnvironment) {
      httpVerbHandler.addStatement("response.setHeader(\"Access-Control-Allow-Origin\", \"*\")");
    }
    httpVerbHandler.addStatement("$L(request, response)", httpVerb.unhandled);
    return httpVerbHandler.build();
  }

  void addHandle(
      final MethodSpec.Builder control,
      final Route route,
      final boolean userLoggedChecked,
      final boolean userNotLoggedChecked,
      final boolean allowedRolesChecked,
      final boolean rejectedRolesChecked)
  {
    if (!userLoggedChecked && route.requiresUserLogged) {
      addUserLoggedValidation(control);
    }
    if (!userNotLoggedChecked && route.requiresUserNotLogged) {
      addUserNotLoggedValidation(control);
    }
    if (!allowedRolesChecked) {
      addAllowedRolesValidation(control, route);
    }
    if (!rejectedRolesChecked) {
      addRejectedRolesValidation(control, route);
    }
    control.addStatement("$L(request, response)", route.handler);
    control.addStatement("return");
  }

  private void addRejectedRolesValidation(
      final MethodSpec.Builder control,
      final Route route)
  {
    if (route.hasOneRejectedRole()) {
      final var role = route.rejectedRole();
      if (!"*".equals(role)) {
        control.beginControlFlow("if (!$S.equals(getCurrentUser().role()))", route.rejectedRole())
               .addStatement("notAuthorized(response)")
               .addStatement("return")
               .endControlFlow();
      }
    } else if (route.hasManyRejectedRole()) {
      control.beginControlFlow("switch (getCurrentUser().role())");
      for (final var rejectedRole : route.rejectedRoles) {
        control.addCode("case $S:\n", rejectedRole);
      }
      control.addStatement("break");
      control.addCode("default:\n")
             .addStatement("notAuthorized(response)")
             .addStatement("return")
             .endControlFlow();
    }
  }

  private boolean no(final List<Route> routes)
  {
    return routes == null || routes.isEmpty();
  }
}
