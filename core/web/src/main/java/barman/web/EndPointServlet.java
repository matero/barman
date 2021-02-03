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

import argo.format.CompactJsonFormatter;
import argo.format.JsonFormatter;
import argo.format.PrettyJsonFormatter;
import argo.jdom.JdomParser;
import argo.jdom.JsonNode;
import argo.saj.InvalidSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class EndPointServlet
    extends RouterServlet
{
  /** interprets strings trimming its contents. */
  @SuppressWarnings("checkstyle:constantName") protected static final ValueInterpreter<String> trimmed = Interpret::asTrimmedString;
  /** indicates that something is required. */
  @SuppressWarnings("checkstyle:constantName") protected static final boolean required = true;
  /** indicates that something is not required. */
  @SuppressWarnings("checkstyle:constantName") protected static final boolean notRequired = false;
  /** parser to use to interpret json contents. */
  private static final JdomParser JDOM_PARSER;
  /**
   * Formatter used to print json content.
   * <p>
   * It should be an instance of {@link PrettyJsonFormatter} in development and an instance of {@link CompactJsonFormatter} in production.
   */
  private static JsonFormatter jsonFormatter;

  static {
    JDOM_PARSER = new JdomParser();
  }
  /** Defined to avoid possible {@link EndPointServlet} anonymous construction. */
  protected EndPointServlet()
  {
    // nothing to do
  }

  /**
   * Sets the JSON formatter to use to translate {@link JsonNode} to text.
   *
   * @param formatter the {@link JsonFormatter} to be used by <em>ALL</em> endpoints servlets.
   * @throws NullPointerException  if {@code formatter} is {@literal null}.
   * @throws IllegalStateException the {@link JsonFormatter} to be used by <em>ALL</em> endpoints servlets had been already set previously.
   */
  public static synchronized void writeJsonUsing(final JsonFormatter formatter)
  {
    if (formatter == null) {
      throw new NullPointerException("formatter");
    }
    if (EndPointServlet.jsonFormatter != null) {
      throw new IllegalStateException("JSON_FORMATTER already defined as an '" + formatter.getClass().getCanonicalName() + "'.");
    }
    EndPointServlet.jsonFormatter = formatter;
  }

  /**
   * @param value Code of the status code to represent.
   * @return an {@link StatusCode} representing the desired {@code value}.
   * @throws IllegalArgumentException if {@code value < 0}.
   */
  protected static StatusCode statusCode(final int value)
  {
    if (value < 0) {
      throw new IllegalArgumentException("value < 0");
    }
    switch (value) {
    case HttpServletResponse.SC_OK:
      return StatusCode.OK;
    case HttpServletResponse.SC_CREATED:
      return StatusCode.CREATED;
    case HttpServletResponse.SC_ACCEPTED:
      return StatusCode.ACCEPTED;
    case HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION:
      return StatusCode.PARTIAL_INFO;
    case HttpServletResponse.SC_NO_CONTENT:
      return StatusCode.NO_RESPONSE;
    case HttpServletResponse.SC_MOVED_PERMANENTLY:
      return StatusCode.MOVED;
    case HttpServletResponse.SC_FOUND:
      return StatusCode.FOUND;
    case HttpServletResponse.SC_SEE_OTHER:
      return StatusCode.METHOD;
    case HttpServletResponse.SC_NOT_MODIFIED:
      return StatusCode.NOT_MODIFIED;
    case HttpServletResponse.SC_BAD_REQUEST:
      return StatusCode.BAD_REQUEST;
    case HttpServletResponse.SC_UNAUTHORIZED:
      return StatusCode.UNAUTHORIZED;
    case HttpServletResponse.SC_PAYMENT_REQUIRED:
      return StatusCode.PAYMENT_REQUIRED;
    case HttpServletResponse.SC_FORBIDDEN:
      return StatusCode.FORBIDDEN;
    case HttpServletResponse.SC_NOT_FOUND:
      return StatusCode.NOT_FOUND;
    case HttpServletResponse.SC_METHOD_NOT_ALLOWED:
      return StatusCode.METHOD_NOT_ALLOWED;
    case HttpServletResponse.SC_CONFLICT:
      return StatusCode.CONFLICT;
    case HttpServletResponse.SC_GONE:
      return StatusCode.GONE;
    case StatusCode.SC_UNPROCESSABLE_ENTITY:
      return StatusCode.UNPROCESSABLE_ENTITY;
    case HttpServletResponse.SC_INTERNAL_SERVER_ERROR:
      return StatusCode.INTERNAL_ERROR;
    case HttpServletResponse.SC_NOT_IMPLEMENTED:
      return StatusCode.NOT_IMPLEMENTED;
    case HttpServletResponse.SC_BAD_GATEWAY:
      return StatusCode.BAD_GATEWAY;
    case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
      return StatusCode.SERVICE_UNAVAILABLE;
    case HttpServletResponse.SC_GATEWAY_TIMEOUT:
      return StatusCode.GATEWAY_TIMEOUT;
    case HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED:
      return StatusCode.HTTP_VERSION_NOT_SUPPORTED;
    default:
      if (StatusCode.CUSTOM.get() == null) {
        final var code = new StatusCode(value);
        StatusCode.CUSTOM.set(new StatusCode[]{code});
        return code;
      } else {
        synchronized (StatusCode.CUSTOM.get()) {
          final StatusCode[] customStatusCodes = StatusCode.CUSTOM.get();
          for (final var customStatusCode : customStatusCodes) {
            if (customStatusCode.code == value) {
              return customStatusCode;
            }
          }
          final var code = new StatusCode(value);
          final var newCustomStatusCodes = new StatusCode[customStatusCodes.length + 1];
          System.arraycopy(customStatusCodes, 0, StatusCode.CUSTOM.get(), 0, customStatusCodes.length);
          newCustomStatusCodes[customStatusCodes.length] = code;
          StatusCode.CUSTOM.set(newCustomStatusCodes);
          return code;
        }
      }
    }
  }

  /**
   * Gets a Content-Type from its represented media type.
   *
   * @param mediaType media type of the desired Content-Type.
   * @return a {@link ContentType} instance representing the {@code mediaType}. NEVER {@literal null}.
   * @throws NullPointerException     if {@code mediaType} is {@literal null}.
   * @throws IllegalArgumentException if {@code mediaType} is {@code empty} or {@code blank}.
   */
  protected static ContentType contentType(final String mediaType)
  {
    if (mediaType == null) {
      throw new NullPointerException("mediaType");
    }
    if (mediaType.isEmpty()) {
      throw new IllegalArgumentException("mediaType is empty");
    }
    if (mediaType.isBlank()) {
      throw new IllegalArgumentException("mediaType is blank");
    }
    switch (mediaType) {
    case "application/x-www-form-urlencoded":
      return ContentType.APPLICATION_FORM_URLENCODED;
    case "text/json":
      return ContentType.TEXT_JSON;
    case "application/json":
      return ContentType.APPLICATION_JSON;
    case "text/xml":
      return ContentType.TEXT_XML;
    case "application/xml":
      return ContentType.APPLICATION_XML;
    case "text/x-yaml":
      return ContentType.TEXT_X_YAML;
    case "application/x-yaml":
      return ContentType.APPLICATION_X_YAML;
    case "text/html":
      return ContentType.TEXT_HTML;
    case "text/plain":
      return ContentType.TEXT_PLAIN;
    case "application/octet-stream":
      return ContentType.APPLICATION_OCTET_STREAM;
    case "multipart/form-data":
      return ContentType.MULTIPART_FORM_DATA;
    default:
      if (!ContentType.CUSTOM.contains(mediaType)) {
        ContentType.CUSTOM.put(mediaType, new ContentType(mediaType));
      }
      return ContentType.CUSTOM.get(mediaType);
    }
  }

  /**
   * Gets a header from its name.
   *
   * @param name name of the header to fetch.
   * @return a {@link Header} instance representing the {@code name}. NEVER {@literal null}.
   * @throws NullPointerException     if {@code name} is {@literal null}.
   * @throws IllegalArgumentException if {@code name} is {@code empty} or {@code blank}.
   */
  protected static Header header(final String name)
  {
    if (name == null) {
      throw new NullPointerException("name");
    }
    if (name.isEmpty()) {
      throw new IllegalArgumentException("name is empty");
    }
    if (name.isBlank()) {
      throw new IllegalArgumentException("name is blank");
    }
    switch (name) {
    case "Accept":
      return Header.ACCEPT;
    case "Accept-Charset":
      return Header.ACCEPT_CHARSET;
    case "Accept-Encoding":
      return Header.ACCEPT_ENCODING;
    case "Accept-Language":
      return Header.ACCEPT_LANGUAGE;
    case "Accept-Datetime":
      return Header.ACCEPT_DATETIME;
    case "Authorization":
      return Header.AUTHORIZATION;
    case "Pragma":
      return Header.PRAGMA;
    case "Cache-Control":
      return Header.CACHE_CONTROL;
    case "Connection":
      return Header.CONNECTION;
    case "Content-Type":
      return Header.CONTENT_TYPE;
    case "Content-Encoding":
      return Header.CONTENT_ENCODING;
    case "Content-Language":
      return Header.CONTENT_LANGUAGE;
    case "Content-Length":
      return Header.CONTENT_LENGTH;
    case "Content-Location":
      return Header.CONTENT_LOCATION;
    case "Content-MD5":
      return Header.CONTENT_MD5;
    case "Content-Disposition":
      return Header.CONTENT_DISPOSITION;
    case "Date":
      return Header.DATE;
    case "Etag":
      return Header.ETAG;
    case "Expires":
      return Header.EXPIRES;
    case "If-Match":
      return Header.IF_MATCH;
    case "If-Modified-Since":
      return Header.IF_MODIFIED_SINCE;
    case "If-None-Match":
      return Header.IF_NONE_MATCH;
    case "User-Agent":
      return Header.USER_AGENT;
    case "Host":
      return Header.HOST;
    case "Last-Modified":
      return Header.LAST_MODIFIED;
    case "Location":
      return Header.LOCATION;
    default:
      if (!Header.CUSTOM.contains(name)) {
        Header.CUSTOM.put(name, new Header(name));
      }
      return Header.CUSTOM.get(name);
    }
  }

  protected static <T> Supplier<T> byDefault(final Supplier<T> defaultValueSupplier)
  {
    return defaultValueSupplier;
  }

  protected static <T> T byDefault(final T defaultValue)
  {
    return defaultValue;
  }

  protected static <T> PathVariable<T> pathVariable(
      final String name,
      final ValueInterpreter<T> interpreter)
  {
    return new PathVariable<>(name, interpreter);
  }

  /**
   * Reads the text at the request body.
   *
   * @param request {@link HttpServletRequest} which body must be read.
   * @return the raw text at the {@link HttpServletRequest} associated to the controller.
   * @throws IOException if some problem occurs while reading the text.
   */
  protected static String body(final HttpServletRequest request)
      throws IOException
  {
    final BufferedReader reader = request.getReader();
    final StringBuilder requestBody = new StringBuilder();
    {
      String line;
      while ((line = reader.readLine()) != null) {
        requestBody.append(line);
      }
    }
    return requestBody.toString();
  }

  protected static JsonNode json(final String content)
      throws InvalidSyntaxException
  {
    return JDOM_PARSER.parse(content);
  }

  protected static String format(final JsonNode json)
  {
    return jsonFormatter.format(json);
  }

  protected static void unprocessableEntity(final HttpServletResponse response)
      throws IOException
  {
    response.sendError(StatusCode.SC_UNPROCESSABLE_ENTITY);
  }

  protected static void notFound(final HttpServletResponse response)
      throws IOException
  {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  protected static void set(
      final HttpServletResponse response,
      final StatusCode statusCode)
  {
    response.setStatus(statusCode.code);
  }

  protected static void set(
      final HttpServletResponse response,
      final Header header,
      final String value)
  {
    response.setHeader(header.name, value);
  }

  protected static void set(
      final HttpServletResponse response,
      final Header header,
      final int value)
  {
    response.setIntHeader(header.name, value);
  }

  protected static void set(
      final HttpServletResponse response,
      final Header header,
      final Date value)
  {
    response.setDateHeader(header.name, value.getTime());
  }

  protected static void set(
      final HttpServletResponse response,
      final Header header,
      final long timestamp)
  {
    response.setDateHeader(header.name, timestamp);
  }

  protected static void add(
      final HttpServletResponse response,
      final Header header,
      final String value)
  {
    response.addHeader(header.name, value);
  }

  protected static void add(
      final HttpServletResponse response,
      final Header header,
      final int value)
  {
    response.addIntHeader(header.name, value);
  }

  protected static void add(
      final HttpServletResponse response,
      final Header header,
      final Date value)
  {
    response.addDateHeader(header.name, value.getTime());
  }

  protected static void add(
      final HttpServletResponse response,
      final Header header,
      final long timestamp)
  {
    response.addDateHeader(header.name, timestamp);
  }

  protected JsonNode json(final HttpServletRequest request)
      throws IOException, ServletException
  {
    try {
      return json(request.getReader());
    } catch (final InvalidSyntaxException e) {
      throw new ServletException(e);
    }
  }

  protected JsonNode json(final Reader reader)
      throws InvalidSyntaxException
  {
    return JDOM_PARSER.parse(reader);
  }

  protected void forward(
      final String location,
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws ServletException
  {
    logger().debug("Forwarding (to '{}')", location);
    try {
      request.getRequestDispatcher(location).forward(request, response);
    } catch (final IOException e) {
      logger().warn("Forward failure", e);
    }
  }

  /**
   * Trigger a browser redirectTo
   *
   * @param location Where to redirectTo
   * @param response {@link HttpServletResponse} which body must be read.
   */
  protected void redirect(
      final String location,
      final HttpServletResponse response)
  {
    logger().trace("Redirecting ('Found', '{}' to '{}'.", HttpServletResponse.SC_FOUND, location);
    try {
      response.sendRedirect(location);
    } catch (final IOException e) {
      logger().warn("Redirect failure", e);
    }
  }

  /**
   * Trigger a browser redirectTo name specific http 3XX status code.
   *
   * @param response       {@link HttpServletResponse} where the redirect must be performed.
   * @param location       Where to redirectTo permanently
   * @param httpStatusCode the http status code
   */
  protected void redirect(
      final HttpServletResponse response,
      final String location,
      final StatusCode httpStatusCode)
  {
    redirect(response, location, httpStatusCode.code);
  }

  /**
   * Trigger a browser redirectTo name specific http 3XX status code.
   *
   * @param response       {@link HttpServletResponse} where the redirect must be performed.
   * @param location       Where to redirectTo permanently
   * @param httpStatusCode the http status code
   */
  protected void redirect(
      final HttpServletResponse response,
      final String location,
      final int httpStatusCode)
  {
    logger().debug("Redirecting ('{}' to '{}').", httpStatusCode, location);
    response.setStatus(httpStatusCode);
    response.setHeader("Location", location);
    response.setHeader("Connection", "close");
    try {
      response.sendError(httpStatusCode);
    } catch (final IOException e) {
      logger().warn("Exception when trying to redirect permanently", e);
    }
  }

  /* contents updaters */
  protected void writeHtml(
      final HttpServletResponse response,
      final CharSequence content)
      throws ServletException, IOException
  {
    set(response, ContentType.TEXT_HTML);
    send(response, content);
  }

  protected void renderJson(
      final HttpServletResponse response,
      final JsonNode json)
      throws ServletException, IOException
  {
    writeJson(response, jsonFormatter.format(json));
  }

  protected void writeJson(
      final HttpServletResponse response,
      final CharSequence content)
      throws ServletException, IOException
  {
    set(response, ContentType.APPLICATION_JSON);
    send(response, content);
  }

  protected void writeText(
      final HttpServletResponse response,
      final CharSequence content)
      throws ServletException, IOException
  {
    set(response, ContentType.TEXT_PLAIN);
    send(response, content);
  }

  /**
   * Writes the string content directly to the response.
   * <p>
   * This method commits the response.
   *
   * @param content the content to write into the response.
   * @throws javax.servlet.ServletException if the response is already committed.
   */
  void send(
      final HttpServletResponse response,
      final CharSequence content)
      throws ServletException, IOException
  {
    if (response.isCommitted()) {
      throw new ServletException("The response has already been committed");
    }
    if (content == null) {
      commit(response, null);
    } else {
      commit(response, content.toString());
    }
  }

  void commit(
      final HttpServletResponse response,
      final String content)
      throws IOException
  {
    if (response.getContentType() == null) {
      set(response, ContentType.TEXT_HTML);
    }
    if (content != null) {
      response.setContentLength(content.getBytes().length);
      response.getWriter().append(content);
    }
    set(response, StatusCode.OK);
  }

  protected void unprocessableEntity(
      final HttpServletResponse response,
      final JsonNode error)
      throws IOException
  {
    final var json = jsonFormatter.format(error);
    set(response, ContentType.APPLICATION_JSON);
    response.sendError(StatusCode.SC_UNPROCESSABLE_ENTITY, json);
  }

  protected boolean userLogged()
  {
    return getCurrentUser() != null;
  }

  protected abstract HasUserRole getCurrentUser();

  protected void set(
      final HttpServletResponse response,
      final ContentType contentType)
  {
    response.setContentType(contentType.mediaType);
  }

  /** Possible response status codes. */
  protected static final class StatusCode
      implements Serializable
  {
    /** Status code (200) indicating the request succeeded normally. */
    public static final StatusCode OK = new StatusCode(HttpServletResponse.SC_OK);
    /** Status code (201) indicating the request succeeded and created a new resource on the server. */
    public static final StatusCode CREATED = new StatusCode(HttpServletResponse.SC_CREATED);
    /** Status code (202) indicating that a request was accepted for processing, but was not completed. */
    public static final StatusCode ACCEPTED = new StatusCode(HttpServletResponse.SC_ACCEPTED);
    /** Status code (203) indicating that the meta information presented by the client did not originate from the server. */
    public static final StatusCode PARTIAL_INFO = new StatusCode(HttpServletResponse.SC_NON_AUTHORITATIVE_INFORMATION);
    /** Status code (203) indicating that the meta information presented by the client did not originate from the server. */
    public static final StatusCode NO_RESPONSE = new StatusCode(HttpServletResponse.SC_NO_CONTENT);
    /**
     * Status code (301) indicating that the resource has permanently moved to a new location, and that future references should use a new URI with
     * their requests.
     */
    public static final StatusCode MOVED = new StatusCode(HttpServletResponse.SC_MOVED_PERMANENTLY);
    /**
     * Status code (302) indicating that the resource reside temporarily under a different URI. Since the redirection might be altered on occasion,
     * the client should continue to use the Request-URI for future requests.(HTTP/1.1) To represent the status code (302), it is recommended to use
     * this variable.
     */
    public static final StatusCode FOUND = new StatusCode(HttpServletResponse.SC_FOUND);
    /** Status code (303) indicating that the response to the request can be found under a different URI. */
    public static final StatusCode METHOD = new StatusCode(HttpServletResponse.SC_SEE_OTHER);
    /** Status code (304) indicating that a conditional GET operation found that the resource was available and not modified. */
    public static final StatusCode NOT_MODIFIED = new StatusCode(HttpServletResponse.SC_NOT_MODIFIED);
    /** Status code (400) indicating the request sent by the client was syntactically incorrect. */
    public static final StatusCode BAD_REQUEST = new StatusCode(HttpServletResponse.SC_BAD_REQUEST);
    /** Status code (401) indicating that the request requires HTTP authentication. */
    public static final StatusCode UNAUTHORIZED = new StatusCode(HttpServletResponse.SC_UNAUTHORIZED);
    /** Status code (402) reserved for future use. */
    public static final StatusCode PAYMENT_REQUIRED = new StatusCode(HttpServletResponse.SC_PAYMENT_REQUIRED);
    /** Status code (403) indicating the server understood the request but refused to fulfill it. */
    public static final StatusCode FORBIDDEN = new StatusCode(HttpServletResponse.SC_FORBIDDEN);
    /** Status code (404) indicating that the requested resource is not available. */
    public static final StatusCode NOT_FOUND = new StatusCode(HttpServletResponse.SC_NOT_FOUND);
    /**
     * Status code (405) indicating that the method specified in the <code><em>Request-Line</em></code> is not allowed for the resource identified by
     * the
     * <code><em>Request-URI</em></code>.
     */
    public static final StatusCode METHOD_NOT_ALLOWED = new StatusCode(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    /** Status code (409) indicating that the request could not be completed due to a conflict with the current state of the resource. */
    public static final StatusCode CONFLICT = new StatusCode(HttpServletResponse.SC_CONFLICT);
    /**
     * Status code (410) indicating that the resource is no longer available at the server and no forwarding address is known. This condition
     * <em>SHOULD</em> be considered permanent.
     */
    public static final StatusCode GONE = new StatusCode(HttpServletResponse.SC_GONE);
    /** Status code (500) indicating an error inside the HTTP server which prevented it from fulfilling the request. */
    public static final StatusCode INTERNAL_ERROR = new StatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    /** Status code (501) indicating the HTTP server does not support the functionality needed to fulfill the request. */
    public static final StatusCode NOT_IMPLEMENTED = new StatusCode(HttpServletResponse.SC_NOT_IMPLEMENTED);
    /**
     * Status code (502) indicating that the HTTP server received an invalid response from a server it consulted when acting as a proxy or gateway.
     */
    public static final StatusCode BAD_GATEWAY = new StatusCode(HttpServletResponse.SC_BAD_GATEWAY);
    /** Status code (503) indicating that the HTTP server is temporarily overloaded, and unable to handle the request. */
    public static final StatusCode SERVICE_UNAVAILABLE = new StatusCode(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    /** Status code (504) indicating that the server did not receive a timely response from the upstream server while acting as a gateway or proxy. */
    public static final StatusCode GATEWAY_TIMEOUT = new StatusCode(HttpServletResponse.SC_GATEWAY_TIMEOUT);
    /**
     * Status code (505) indicating that the server does not support or refuses to support the HTTP protocol version that was used in the request
     * message.
     */
    public static final StatusCode HTTP_VERSION_NOT_SUPPORTED = new StatusCode(HttpServletResponse.SC_HTTP_VERSION_NOT_SUPPORTED);
    /**
     * User defined status codes.
     *
     * @see #statusCode(int) for details on its use.
     */
    private static final AtomicReference<StatusCode[]> CUSTOM = new AtomicReference<>();
    /**
     * Status code (422) indicating that the server understands the content type of the request entity, and the syntax of the request entity is
     * correct, but it was unable to process the contained instructions.
     */
    private static final int SC_UNPROCESSABLE_ENTITY = 422;
    /**
     * Status code (422) indicating that the server understands the content type of the request entity, and the syntax of the request entity is
     * correct, but it was unable to process the contained instructions.
     *
     * <code><em>Important:</em></code> The client should not repeat this request without modification.
     */
    public static final StatusCode UNPROCESSABLE_ENTITY = new StatusCode(SC_UNPROCESSABLE_ENTITY);
    private final int code;

    public StatusCode(final int code)
    {
      this.code = code;
    }

    @Override public int hashCode()
    {
      return Integer.hashCode(code);
    }

    @Override public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o instanceof StatusCode) {
        return code == ((StatusCode) o).code;
      }
      return false;
    }

    @Override public String toString()
    {
      return "StatusCode(" + code + ')';
    }
  }

  /** Common content type identifying resource's media types. */
  protected static final class ContentType
      implements Serializable
  {
    /**
     * Keys and values are encoded in key-value tuples separated by '&', with a '=' between the key and the value.
     * <p>
     * Non-alphanumeric characters in both keys and values are percent encoded: this is the reason why this type is not suitable to use with binary
     * data (use {@link #MULTIPART_FORM_DATA} instead)
     */
    public static final ContentType APPLICATION_FORM_URLENCODED = new ContentType("application/x-www-form-urlencoded");
    /** The MIME media type for JSON text readable by casual users. The default encoding is UTF-8. */
    public static final ContentType TEXT_JSON = new ContentType("text/json");
    /** The MIME media type for JSON text not readable by casual users. The default encoding is UTF-8. */
    public static final ContentType APPLICATION_JSON = new ContentType("application/json");
    /** The MIME media type for XML text readable by casual users. */
    public static final ContentType TEXT_XML = new ContentType("text/xml");
    /** The MIME media type for XML text not readable by casual users. */
    public static final ContentType APPLICATION_XML = new ContentType("application/xml");
    /** The MIME media type for YAML text readable by casual users, no an standard, that's why the {@code "x-"} in the media type. */
    public static final ContentType TEXT_X_YAML = new ContentType("text/x-yaml");
    /** The MIME media type for YAML text not readable by casual users, no an standard, that's why the {@code "x-"} in the media type. */
    public static final ContentType APPLICATION_X_YAML = new ContentType("application/x-yaml");
    /** media type for HTML resources. */
    public static final ContentType TEXT_HTML = new ContentType("text/html");
    /** Plain text media type. */
    public static final ContentType TEXT_PLAIN = new ContentType("text/plain");
    /**
     * A binary file.
     * <p>
     * Typically, it will be an application or a document that must be opened in an application, such as a spreadsheet or word processor. If the
     * attachment has a filename extension associated with it, you may be able to tell what kind of file it is. A .exe extension, for example,
     * indicates it is a Windows or DOS program (executable), while a file ending in .doc is probably meant to be opened in Microsoft Word.
     */
    public static final ContentType APPLICATION_OCTET_STREAM = new ContentType("application/octet-stream");
    /**
     * Each value is sent as a block of data ("body part"), with a user agent-defined delimiter ("boundary") separating each part. The keys are given
     * in the Content-Disposition header of each part.
     */
    public static final ContentType MULTIPART_FORM_DATA = new ContentType("multipart/form-data");
    /**
     * User defined headers.
     *
     * @see #contentType(String) to understand its use.
     */
    private static final ConcurrentHashMap<String, ContentType> CUSTOM = new ConcurrentHashMap<>();
    /** media type represented by the instance. */
    private final String mediaType;

    /**
     * Constructs an instance of {@link ContentType} given the media type to represent.
     *
     * @param mediaType the media type to represent by the instance. Its assumed it is not {@literal null}.
     */
    private ContentType(final String mediaType)
    {
      this.mediaType = mediaType;
    }

    @Override public int hashCode()
    {
      return mediaType.hashCode();
    }

    @Override public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o instanceof ContentType) {
        return mediaType.equals(((ContentType) o).mediaType);
      }
      return false;
    }

    @Override public String toString()
    {
      return "ContentType(" + mediaType + ')';
    }
  }

  /** Common HTTP headers. */
  protected static final class Header
      implements Serializable
  {
    /** Specify media types which are acceptable for the response. */
    public static final Header ACCEPT = new Header("Accept");
    /** Indicate what character sets are acceptable for the response. */
    public static final Header ACCEPT_CHARSET = new Header("Accept-Charset");
    /** Restricts the content-codings (section 3.5) that are acceptable in the response. */
    public static final Header ACCEPT_ENCODING = new Header("Accept-Encoding");
    /** Restricts the set of natural languages that are preferred as a response to the request. */
    public static final Header ACCEPT_LANGUAGE = new Header("Accept-Language");
    /** Indicate it wants to access a past state of an Original Resource. */
    public static final Header ACCEPT_DATETIME = new Header("Accept-Datetime");
    /** A user agent that wishes to authenticate itself with a server does so by including an Authorization request-header field with the request. */
    public static final Header AUTHORIZATION = new Header("Authorization");
    /** Include implementation- specific directives that might apply to any recipient along the request/response chain. */
    public static final Header PRAGMA = new Header("Pragma");
    /** Specify directives that MUST be obeyed by all caching mechanisms along the request/response chain. */
    public static final Header CACHE_CONTROL = new Header("Cache-Control");
    /** Specify options that are desired for that particular connection and MUST NOT be communicated by proxies over further connections. */
    public static final Header CONNECTION = new Header("Connection");
    /** Used to indicate the media type of the resource. */
    public static final Header CONTENT_TYPE = new Header("Content-Type");
    /**
     * Used as a modifier to the media-type.
     * <p>
     * When present, its value indicates what additional content codings have been applied to the entity-body, and thus what decoding mechanisms must
     * be applied in order to obtain the media-type referenced by the Content-Type header field. Content-Encoding is primarily used to allow a
     * document to be compressed without losing the identity of its underlying media type.
     */
    public static final Header CONTENT_ENCODING = new Header("Content-Encoding");
    /** Describes the natural language(s) of the intended audience for the enclosed entity. */
    public static final Header CONTENT_LANGUAGE = new Header("Content-Language");
    /**
     * Indicates the size of the entity-body, in decimal number of OCTETs, sent to the recipient or, in the case of the HEAD method, the size of the
     * entity-body that would have been sent had the request been a GET.
     */
    public static final Header CONTENT_LENGTH = new Header("Content-Length");
    /**
     * MAY be used to supply the resource location for the entity enclosed in the message when that entity is accessible from a location separate from
     * the requested resource's URI.
     */
    public static final Header CONTENT_LOCATION = new Header("Content-Location");
    /** MD5 digest of the entity-body for the purpose of providing an end-to-end message integrity check (MIC) of the entity-body. */
    public static final Header CONTENT_MD5 = new Header("Content-MD5");
    /**
     * Indicates if the content is expected to be displayed inline in the browser, that is, as a Web page or as part of a Web page, or as an
     * attachment, that is downloaded and saved locally.
     */
    public static final Header CONTENT_DISPOSITION = new Header("Content-Disposition");
    /** Contains the date and time at which the message was originated. */
    public static final Header DATE = new Header("Date");
    /** Provides the current value of the entity tag for the requested variant. */
    public static final Header ETAG = new Header("Etag");
    /** Gives the date/time after which the response is considered stale. */
    public static final Header EXPIRES = new Header("Expires");
    /**
     * Used with a method to make it conditional.
     * <p>
     * A client that has one or more entities previously obtained from the resource can verify that one of those entities is current by including a
     * list of their associated entity tags in the If-Match header field.
     */
    public static final Header IF_MATCH = new Header("If-Match");
    /**
     * Used with a method to make it conditional: if the requested variant has not been modified since the time specified in this field, an entity
     * will not be returned from the server; instead, a 304 (not modified) response will be returned without any message-body.
     */
    public static final Header IF_MODIFIED_SINCE = new Header("If-Modified-Since");
    /**
     * Used with a method to make it conditional.
     * <p>
     * A client that has one or more entities previously obtained from the resource can verify that none of those entities is current by including a
     * list of their associated entity tags in the If-None-Match header field.
     */
    public static final Header IF_NONE_MATCH = new Header("If-None-Match");
    /** Contains information about the user agent originating the request. */
    public static final Header USER_AGENT = new Header("User-Agent");
    /**
     * Specifies the Internet host and port number of the resource being requested, as obtained from the original URI given by the user or referring
     * resource.
     */
    public static final Header HOST = new Header("Host");
    /** Indicates the date and time at which the origin server believes the variant was last modified. */
    public static final Header LAST_MODIFIED = new Header("Last-Modified");
    /**
     * Used to redirect the recipient to a location other than the Request-URI for completion of the request or identification of a new resource.
     * <p>
     * For 201 (Created) responses, the Location is that of the new resource which was created by the request. <br> For 3xx responses, the location
     * SHOULD indicate the server's preferred URI for automatic redirection to the resource. <br> The field value consists of a single absolute URI.
     */
    public static final Header LOCATION = new Header("Location");
    /**
     * User defined headers.
     *
     * @see #header(String) to understand its use.
     */
    private static final ConcurrentHashMap<String, Header> CUSTOM = new ConcurrentHashMap<>();
    private final String name;

    public Header(final String name)
    {
      this.name = name;
    }

    @Override public int hashCode()
    {
      return name.hashCode();
    }

    @Override public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o instanceof Header) {
        return name.equals(((Header) o).name);
      }
      return false;
    }

    @Override public String toString()
    {
      return "Header(" + name + ')';
    }
  }
}
