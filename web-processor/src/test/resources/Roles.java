package test;

import barman.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@endpoint @roles({"user", "configurator"}) class Tasks extends TestEndPoint
{
  @GET void index(final HttpServletRequest request, final HttpServletResponse response)
  {
  }

  @GET("/{id}") void get(final HttpServletRequest request, final HttpServletResponse response)
  {
  }

  @GET void author(final HttpServletRequest request, final HttpServletResponse response)
  {
  }

  @POST void save(final HttpServletRequest request, final HttpServletResponse response)
  {
  }

  @PUT("/{id}") void update(final HttpServletRequest request, final HttpServletResponse response)
  {
  }

  @DELETE("/{id}") void delete(final HttpServletRequest request, final HttpServletResponse response)
  {
  }
}