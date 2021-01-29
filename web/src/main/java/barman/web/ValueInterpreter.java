package barman.web;

@FunctionalInterface public interface ValueInterpreter<T>
{
  T from(String rawValue);
}