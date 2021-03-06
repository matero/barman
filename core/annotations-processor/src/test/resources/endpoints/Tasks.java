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

import barman.web.DELETE;
import barman.web.Endpoint;
import barman.web.GET;
import barman.web.POST;
import barman.web.PUT;
import barman.processors.TestEndPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Endpoint class Tasks
    extends TestEndPoint
{
  @GET void index(
      final HttpServletRequest request,
      final HttpServletResponse response)
  {
  }

  @GET("/{id}") void get(
      final HttpServletRequest request,
      final HttpServletResponse response)
  {
  }

  @GET void author(
      final HttpServletRequest request,
      final HttpServletResponse response)
  {
  }

  @POST void save(
      final HttpServletRequest request,
      final HttpServletResponse response)
  {
  }

  @PUT("/{id}") void update(
      final HttpServletRequest request,
      final HttpServletResponse response)
  {
  }

  @DELETE("/{id}") void delete(
      final HttpServletRequest request,
      final HttpServletResponse response)
  {
  }
}
