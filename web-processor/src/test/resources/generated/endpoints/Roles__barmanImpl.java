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

  private final RouterServlet.Path PUT_update = path("/api/tasks/{id}", "/{id}", Pattern.compile("/(?<id>[^/]+)"), "id");

  private final RouterServlet.Path DELETE_delete = path("/api/tasks/{id}", "/{id}", Pattern.compile("/(?<id>[^/]+)"), "id");

  @Override
  public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws
                                                                                          ServletException, IOException {
    if (indexPath.matches(request)) {
      if (!userRoleIsIn("user", "configurator")) {
        notAuthorized(response);
        return;
      }
      index(request, response);
      return;
    }
    if (GET_get.matches(request)) {
      if (!userRoleIsIn("user", "configurator")) {
        notAuthorized(response);
        return;
      }
      get(request, response);
      return;
    }
    if (GET_author.matches(request)) {
      if (!userRoleIsIn("user", "configurator")) {
        notAuthorized(response);
        return;
      }
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
      if (!userRoleIsIn("user", "configurator")) {
        notAuthorized(response);
        return;
      }
      save(request, response);
      return;
    }
    response.setHeader("Access-Control-Allow-Origin", "*");
    unhandledPost(request, response);
  }

  @Override
  public void doPut(final HttpServletRequest request, final HttpServletResponse response) throws
                                                                                          ServletException, IOException {
    if (PUT_update.matches(request)) {
      if (!userRoleIsIn("user", "configurator")) {
        notAuthorized(response);
        return;
      }
      update(request, response);
      return;
    }
    response.setHeader("Access-Control-Allow-Origin", "*");
    unhandledPut(request, response);
  }

  @Override
  public void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws
                                                                                             ServletException, IOException {
    if (DELETE_delete.matches(request)) {
      if (!userRoleIsIn("user", "configurator")) {
        notAuthorized(response);
        return;
      }
      delete(request, response);
      return;
    }
    response.setHeader("Access-Control-Allow-Origin", "*");
    unhandledDelete(request, response);
  }
}