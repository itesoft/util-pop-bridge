package com.itesoft.contrib.popbridge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Configuration
{
  private Map<String, String> _options;
  private Set<String> _flags;

  public Configuration()
  {
    _options = new HashMap<>();
    _flags = new HashSet<>();
  }

  void setFlag(String flag)
  {
    _flags.add(flag.toLowerCase());
  }

  void setOption(String option, String value)
  {
    _options.put(option.toLowerCase(), value);
  }

  public String getOption(String option)
  {
    String value = getOption(option, null);
    if (value == null)
    {
      System.err.println("Driver option [--option " + option + "=<value>] is required on command line");
      System.exit(2);
    }
    return value;
  }

  public Iterable<Entry<String, String>> options()
  {
    return new Iterable<Map.Entry<String,String>>()
    {

      @Override
      public Iterator<Entry<String, String>> iterator()
      {
        return _options.entrySet().iterator();
      }
    };
  }

  public String getOption(String option, String defaultValue)
  {
    option = option.toLowerCase();
    if (_options.containsKey(option))
    {
      return _options.get(option);
    }
    else
    {
      return defaultValue;
    }
  }

  public boolean isFlagEnabled(String flag)
  {
    return _flags.contains(flag.toLowerCase());
  }

  public Iterable<String> flags()
  {
    return new Iterable<String>()
    {
      @Override
      public Iterator<String> iterator()
      {
        return _flags.iterator();
      }
    };
  }
}
