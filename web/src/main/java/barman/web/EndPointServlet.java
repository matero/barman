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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public abstract class EndPointServlet
    extends RouterServlet
{
  protected static final JdomParser JDOM_PARSER;

  static {
    JDOM_PARSER = new JdomParser();
  }

  private static JsonFormatter JSON_FORMATTER = new PrettyJsonFormatter();

  public synchronized static void writePrettyJson() { JSON_FORMATTER = new PrettyJsonFormatter(); }

  public synchronized static void writeCompactJson() { JSON_FORMATTER = new CompactJsonFormatter(); }

  protected EndPointServlet()
  {
    /* nothing to do */
  }

  /* request manipulation ************************************************** */

  /**
   Reads the text at the request body.

   @return the raw text at the {@link HttpServletRequest} associated to the controller.
   @throws IOException if some problem occurs while reading the text.
   */
  protected String body(final HttpServletRequest request)
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

  protected JsonNode json(final String content)
      throws InvalidSyntaxException
  {
    return JDOM_PARSER.parse(content);
  }

  /* response manipulation ************************************************* */
  protected static String to(final String location) { return location; }

  protected static int withStatus(final int httpStatusCode) { return httpStatusCode; }

  protected static final String location(final String value) { return value; }

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
   Trigger a browser redirectTo

   @param location Where to redirectTo
   */
  protected void redirect(
      final String location,
      final HttpServletResponse response)
  {
    logger().debug("Redirecting ('Found', '{}' to '{}'.", HttpServletResponse.SC_FOUND, location);
    try {
      response.sendRedirect(location);
    } catch (final IOException e) {
      logger().warn("Redirect failure", e);
    }
  }

  /**
   Trigger a browser redirectTo name specific http 3XX status code.

   @param location       Where to redirectTo permanently
   @param httpStatusCode the http status code
   */
  protected void redirect(
      final HttpServletResponse response,
      final String location,
      final StatusCode httpStatusCode)
  {
    redirect(response, to(location), withStatus(httpStatusCode.value));
  }

  /**
   Trigger a browser redirectTo name specific http 3XX status code.

   @param location       Where to redirectTo permanently
   @param httpStatusCode the http status code
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

  protected String format(final JsonNode json)
  {
    return JSON_FORMATTER.format(json);
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

  protected void writeXhtml(
      final HttpServletResponse response,
      final CharSequence content)
      throws ServletException, IOException
  {
    set(response, ContentType.TEXT_XHTML);
    send(response, content);
  }

  protected void renderJson(
      final HttpServletResponse response,
      final JsonNode json)
      throws ServletException, IOException
  {
    writeJson(response, JSON_FORMATTER.format(json));
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
   Writes the string content directly to the response.
   <p>
   This method commits the response.

   @param content the content to write into the response.
   @throws javax.servlet.ServletException if the response is already committed.
   @throws java.io.IOException             */
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

  protected void unprocessableEntity(final HttpServletResponse response)
      throws IOException
  {
    response.sendError(422);
  }

  protected void unprocessableEntity(
      final HttpServletResponse response,
      final JsonNode error)
      throws IOException
  {
    final var json = JSON_FORMATTER.format(error);
    set(response, ContentType.APPLICATION_JSON);
    response.sendError(422, json);
  }

  protected void notFound(final HttpServletResponse response)
      throws IOException
  {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  protected boolean userLogged() {return getCurrentUser() != null;}

  protected abstract HasUserRole getCurrentUser();

  protected static StatusCode statusCode(final int value) {
    if (value < 0) { throw new IllegalArgumentException("value < 0"); }
    switch (value) {
    case 200:
      return StatusCode.OK;
    case 201:
      return StatusCode.CREATED;
    case 202:
      return StatusCode.ACCEPTED;
    case 203:
      return StatusCode.PARTIAL_INFO;
    case 204:
      return StatusCode.NO_RESPONSE;
    case 301:
      return StatusCode.MOVED;
    case 302:
      return StatusCode.FOUND;
    case 303:
      return StatusCode.METHOD;
    case 304:
      return StatusCode.NOT_MODIFIED;
    case 400:
      return StatusCode.BAD_REQUEST;
    case 401:
      return StatusCode.UNAUTHORIZED;
    case 402:
      return StatusCode.PAYMENT_REQUIRED;
    case 403:
      return StatusCode.FORBIDDEN;
    case 404:
      return StatusCode.NOT_FOUND;
    case 405:
      return StatusCode.METHOD_NOT_ALLOWED;
    case 409:
      return StatusCode.CONFLICT;
    case 410:
      return StatusCode.GONE;
    case 422:
      return StatusCode.UNPROCESSABLE_ENTITY;
    case 500:
      return StatusCode.INTERNAL_ERROR;
    case 501:
      return StatusCode.NOT_IMPLEMENTED;
    case 502:
      return StatusCode.OVERLOADED;
    case 503:
      return StatusCode.SERVICE_UNAVAILABLE;
    case 504:
      return StatusCode.GATEWAY_TIMEOUT;
    default:
      var customStatusCodes = StatusCode.CUSTOM.get();
      if (customStatusCodes == null) {
        final var code = new StatusCode(value);
        StatusCode.CUSTOM.set(Map.of(value, code));
        return code;
      } else {
        var code = customStatusCodes.get(value);
        if (code == null) {
          code = new StatusCode(value);
          customStatusCodes = new HashMap<>(customStatusCodes);
          customStatusCodes.put(value, code);
          StatusCode.CUSTOM.set(Map.copyOf(customStatusCodes));
        }
        return code;
      }
    }
  }

  protected static final class StatusCode
      implements Serializable
  {
    private static final AtomicReference<Map<Integer, StatusCode>> CUSTOM = new AtomicReference<>();

    public static final StatusCode OK = new StatusCode(200);
    public static final StatusCode CREATED = new StatusCode(201);
    public static final StatusCode ACCEPTED = new StatusCode(202);
    public static final StatusCode PARTIAL_INFO = new StatusCode(203);
    public static final StatusCode NO_RESPONSE = new StatusCode(204);
    public static final StatusCode MOVED = new StatusCode(301);
    public static final StatusCode FOUND = new StatusCode(302);
    public static final StatusCode METHOD = new StatusCode(303);
    public static final StatusCode NOT_MODIFIED = new StatusCode(304);
    public static final StatusCode BAD_REQUEST = new StatusCode(400);
    public static final StatusCode UNAUTHORIZED = new StatusCode(401);
    public static final StatusCode PAYMENT_REQUIRED = new StatusCode(402);
    public static final StatusCode FORBIDDEN = new StatusCode(403);
    public static final StatusCode NOT_FOUND = new StatusCode(404);
    public static final StatusCode METHOD_NOT_ALLOWED = new StatusCode(405);
    public static final StatusCode CONFLICT = new StatusCode(409);
    public static final StatusCode GONE = new StatusCode(410);
    public static final StatusCode UNPROCESSABLE_ENTITY = new StatusCode(422);
    public static final StatusCode INTERNAL_ERROR = new StatusCode(500);
    public static final StatusCode NOT_IMPLEMENTED = new StatusCode(501);
    public static final StatusCode OVERLOADED = new StatusCode(502);
    public static final StatusCode SERVICE_UNAVAILABLE = new StatusCode(503);
    public static final StatusCode GATEWAY_TIMEOUT = new StatusCode(504);

    private final int value;

    public StatusCode(final int value) {this.value = value;}

    @Override public int hashCode() {return Integer.hashCode(value);}

    @Override public boolean equals(final Object o)
    {
      if (this == o) { return true; }
      if (o instanceof StatusCode) {
        return value == ((StatusCode) o).value;
      }
      return false;
    }

    @Override public String toString() {return "StatusCode(" + value + ')';}
  }

  protected void set(
      final HttpServletResponse response,
      final StatusCode statusCode)
  {
    response.setStatus(statusCode.value);
  }

  protected static ContentType contentType(final String value) {
    if (value == null) { throw new NullPointerException("value"); }
    if (value.isEmpty()) { throw new IllegalArgumentException("value is empty"); }
    if (value.isBlank()) { throw new IllegalArgumentException("value is blank"); }
    switch (value) {
    case "application/x-www-form-urlencoded":
      return ContentType.APPLICATION_FORM_URLENCODED;
    case "application/json":
      return ContentType.APPLICATION_JSON;
    case "application/xml":
      return ContentType.APPLICATION_XML;
    case "application/x-yaml":
      return ContentType.APPLICATION_X_YAML;
    case "text/html":
      return ContentType.TEXT_HTML;
    case "text/xhtml":
      return ContentType.TEXT_XHTML;
    case "text/plain":
      return ContentType.TEXT_PLAIN;
    case "application/octet-stream":
      return ContentType.APPLICATION_OCTET_STREAM;
    case "multipart/form-data":
      return ContentType.MULTIPART_FORM_DATA;
    default:
      var customContentTypes = ContentType.CUSTOM.get();
      if (customContentTypes == null) {
        final var type = new ContentType(value);
        ContentType.CUSTOM.set(Map.of(value, type));
        return type;
      } else {
        var type = customContentTypes.get(value);
        if (type == null) {
          type = new ContentType(value);
          customContentTypes = new HashMap<>(customContentTypes);
          customContentTypes.put(value, type);
          ContentType.CUSTOM.set(Map.copyOf(customContentTypes));
        }
        return type;
      }
    }
  }

  protected static final class ContentType
      implements Serializable
  {
    private static final AtomicReference<Map<String, ContentType>> CUSTOM = new AtomicReference<>();

    public static final ContentType APPLICATION_FORM_URLENCODED = new ContentType("application/x-www-form-urlencoded");
    public static final ContentType APPLICATION_JSON = new ContentType("application/json");
    public static final ContentType APPLICATION_XML = new ContentType("application/xml");
    public static final ContentType APPLICATION_X_YAML = new ContentType("application/x-yaml");
    public static final ContentType TEXT_HTML = new ContentType("text/html");
    public static final ContentType TEXT_XHTML = new ContentType("text/xhtml");
    public static final ContentType TEXT_PLAIN = new ContentType("text/plain");
    public static final ContentType APPLICATION_OCTET_STREAM = new ContentType("application/octet-stream");
    public static final ContentType MULTIPART_FORM_DATA = new ContentType("multipart/form-data");

    private final String value;

    private ContentType(final String value) {this.value = value;}

    @Override public int hashCode() {return value.hashCode();}

    @Override public boolean equals(final Object o)
    {
      if (this == o) { return true; }
      if (o instanceof ContentType) {
        return value.equals(((ContentType) o).value);
      }
      return false;
    }

    @Override public String toString() {return "ContentType(" + value + ')';}
  }

  protected void set(
      final HttpServletResponse response,
      final ContentType contentType)
  {
    response.setContentType(contentType.value);
  }

  protected static Header header(final String value) {
    if (value == null) { throw new NullPointerException("value"); }
    if (value.isEmpty()) { throw new IllegalArgumentException("value is empty"); }
    if (value.isBlank()) { throw new IllegalArgumentException("value is blank"); }
    switch (value) {
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
    case "Content-Length":
      return Header.CONTENT_LENGTH;
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
      var customHeaders = Header.CUSTOM.get();
      if (customHeaders == null) {
        final var header = new Header(value);
        Header.CUSTOM.set(Map.of(value, header));
        return header;
      } else {
        var header = customHeaders.get(value);
        if (header == null) {
          header = new Header(value);
          customHeaders = new HashMap<>(customHeaders);
          customHeaders.put(value, header);
          Header.CUSTOM.set(Map.copyOf(customHeaders));
        }
        return header;
      }
    }
  }

  protected static final class Header
      implements Serializable
  {
    private static final AtomicReference<Map<String, Header>> CUSTOM = new AtomicReference<>();

    public static final Header ACCEPT = new Header("Accept");
    public static final Header ACCEPT_CHARSET = new Header("Accept-Charset");
    public static final Header ACCEPT_ENCODING = new Header("Accept-Encoding");
    public static final Header ACCEPT_LANGUAGE = new Header("Accept-Language");
    public static final Header ACCEPT_DATETIME = new Header("Accept-Datetime");
    public static final Header AUTHORIZATION = new Header("Authorization");
    public static final Header PRAGMA = new Header("Pragma");
    public static final Header CACHE_CONTROL = new Header("Cache-Control");
    public static final Header CONNECTION = new Header("Connection");
    public static final Header CONTENT_TYPE = new Header("Content-Type");
    public static final Header CONTENT_LENGTH = new Header("Content-Length");
    public static final Header CONTENT_MD5 = new Header("Content-MD5");
    public static final Header CONTENT_DISPOSITION = new Header("Content-Disposition");
    public static final Header DATE = new Header("Date");
    public static final Header ETAG = new Header("Etag");
    public static final Header EXPIRES = new Header("Expires");
    public static final Header IF_MATCH = new Header("If-Match");
    public static final Header IF_MODIFIED_SINCE = new Header("If-Modified-Since");
    public static final Header IF_NONE_MATCH = new Header("If-None-Match");
    public static final Header USER_AGENT = new Header("User-Agent");
    public static final Header HOST = new Header("Host");
    public static final Header LAST_MODIFIED = new Header("Last-Modified");
    public static final Header LOCATION = new Header("Location");

    private final String value;

    public Header(final String value) {this.value = value;}

    @Override public int hashCode() {return value.hashCode();}

    @Override public boolean equals(final Object o)
    {
      if (this == o) { return true; }
      if (o instanceof Header) {
        return value.equals(((Header) o).value);
      }
      return false;
    }

    @Override public String toString() {return "Header(" + value + ')';}

  }

  protected void set(
      final HttpServletResponse response,
      final Header header,
      final String value)
  {
    response.setHeader(header.value, value);
  }

  protected void set(
      final HttpServletResponse response,
      final Header header,
      final int value)
  {
    response.setIntHeader(header.value, value);
  }

  protected void set(
      final HttpServletResponse response,
      final Header header,
      final Date value)
  {
    response.setDateHeader(header.value, value.getTime());
  }

  protected void set(
      final HttpServletResponse response,
      final Header header,
      final long timestamp)
  {
    response.setDateHeader(header.value, timestamp);
  }

  protected void add(
      final HttpServletResponse response,
      final Header header,
      final String value)
  {
    response.addHeader(header.value, value);
  }

  protected void add(
      final HttpServletResponse response,
      final Header header,
      final int value)
  {
    response.addIntHeader(header.value, value);
  }

  protected void add(
      final HttpServletResponse response,
      final Header header,
      final Date value)
  {
    response.addDateHeader(header.value, value.getTime());
  }

  protected void add(
      final HttpServletResponse response,
      final Header header,
      final long timestamp)
  {
    response.addDateHeader(header.value, timestamp);
  }

  protected final <T> T get(
      final HttpServletRequest request,
      final QueryParameter<T> parameter)
  {
    return parameter.of(request);
  }

  protected final boolean has(
      final HttpServletRequest request,
      final QueryParameter<?> parameter)
  {
    return parameter.isDefinedAt(request);
  }

  protected final <T> T get(
      final HttpServletRequest request,
      final PathVariable<T> parameter)
  {
    return parameter.of(request);
  }

  protected final boolean has(
      final HttpServletRequest request,
      final PathVariable<?> parameter)
  {
    return parameter.isDefinedAt(request);
  }

  protected static final ValueInterpreter<String> trimmed = Interpret::asTrimmedString;

  protected static <T> Supplier<T> byDefault(final T defaultValue)
  {
    return () -> defaultValue;
  }

  protected static final boolean required = true;

  protected static final boolean notRequired = false;

  protected static <T> Supplier<T> nullByDefault() { return () -> null; }

  protected static <T> QueryParameter<T> queryParameter(
      final String name,
      final ValueInterpreter<T> interpreter)
  {
    return queryParameter(name, notRequired, interpreter, nullByDefault());
  }

  protected static <T> QueryParameter<T> queryParameter(
      final String name,
      final boolean required,
      final ValueInterpreter<T> interpreter)
  {
    return queryParameter(name, required, interpreter, nullByDefault());
  }

  protected static <T> QueryParameter<T> queryParameter(
      final String name,
      final ValueInterpreter<T> interpreter,
      final Supplier<T> defaultValue)
  {
    return queryParameter(name, notRequired, interpreter, defaultValue);
  }

  protected static <T> QueryParameter<T> queryParameter(
      final String name,
      final boolean required,
      final ValueInterpreter<T> interpreter,
      final Supplier<T> defaultValue)
  {
    return new QueryParameter<T>(name, required, interpreter, defaultValue);
  }

  protected static <T> PathVariable<T> pathVariable(
      final String name,
      final ValueInterpreter<T> interpreter)
  {
    return new PathVariable<>(name, interpreter);
  }
}
