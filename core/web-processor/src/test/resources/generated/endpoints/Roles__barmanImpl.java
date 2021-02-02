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
package test;

import barman.web.RouterServlet;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.annotation.processing.Generated;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Generated(
    value = "barman/web-processor",
    comments = "",
    date = "2017-02-23"
)
@WebServlet("/api/tasks/*")
public final class Tasks__barmanImpl extends Tasks {
  private final RouterServlet.Path GET_get = path("/api/tasks/{id}", "/{id}", Pattern.compile("/(?<id>[^/]+)"), "id");

  private final RouterServlet.Path GET_author = path("/api/tasks/author", "/author");

  private final RouterServlet.Path POST_login = path("/api/tasks/login", "/login");

  private final RouterServlet.Path POST_logout = path("/api/tasks/logout", "/logout");

  private final RouterServlet.Path PUT_update = path("/api/tasks/{id}", "/{id}", Pattern.compile("/(?<id>[^/]+)"), "id");

  private final RouterServlet.Path DELETE_delete = path("/api/tasks/{id}", "/{id}", Pattern.compile("/(?<id>[^/]+)"), "id");

  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws
                                                                                          ServletException, IOException {
    if (!userLogged()) {
      notAuthorized(response);
      return;
    }
    switch (getCurrentUser().role()) {
    case "user":
    case "configurator":
      break;
    default:
      notAuthorized(response);
      return;
    }
    if (indexPath.matches(request)) {
      index(request, response);
      return;
    }
    if (GET_get.matches(request)) {
      get(request, response);
      return;
    }
    if (GET_author.matches(request)) {
      author(request, response);
      return;
    }
    response.setHeader("Access-Control-Allow-Origin", "*");
    unhandledGet(request, response);
  }

  @Override
  public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws
                                                                                           ServletException, IOException {
    if (indexPath.matches(request)) {
      if (!userLogged()) {
        notAuthorized(response);
        return;
      }
      switch (getCurrentUser().role()) {
      case "user":
      case "configurator":
        break;
      default:
        notAuthorized(response);
        return;
      }
      save(request, response);
      return;
    }
    if (POST_login.matches(request)) {
      if (userLogged()) {
        notAuthorized(response);
        return;
      }
      login(request, response);
      return;
    }
    if (POST_logout.matches(request)) {
      if (!userLogged()) {
        notAuthorized(response);
        return;
      }
      logout(request, response);
      return;
    }
    response.setHeader("Access-Control-Allow-Origin", "*");
    unhandledPost(request, response);
  }

  @Override
  public void doPut(final HttpServletRequest request, final HttpServletResponse response) throws
                                                                                          ServletException, IOException {
    if (!userLogged()) {
      notAuthorized(response);
      return;
    }
    switch (getCurrentUser().role()) {
    case "user":
    case "configurator":
      break;
    default:
      notAuthorized(response);
      return;
    }
    if (PUT_update.matches(request)) {
      update(request, response);
      return;
    }
    response.setHeader("Access-Control-Allow-Origin", "*");
    unhandledPut(request, response);
  }

  @Override
  public void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws
                                                                                             ServletException, IOException {
    if (!userLogged()) {
      notAuthorized(response);
      return;
    }
    switch (getCurrentUser().role()) {
    case "user":
    case "configurator":
      break;
    default:
      notAuthorized(response);
      return;
    }
    if (DELETE_delete.matches(request)) {
      delete(request, response);
      return;
    }
    response.setHeader("Access-Control-Allow-Origin", "*");
    unhandledDelete(request, response);
  }
}
