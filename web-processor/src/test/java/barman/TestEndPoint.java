package barman;

import barman.web.HasUserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEndPoint extends barman.web.EndPointServlet
{
  private static final Logger LOGGER = LoggerFactory.getLogger(TestEndPoint.class);

  protected TestEndPoint() {
    // nothing to do
  }

  @Override protected HasUserRole getCurrentUser() { return null; }

  @Override protected final Logger logger() {return LOGGER;}
}