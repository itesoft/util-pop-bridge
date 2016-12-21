package com.itesoft.contrib.popbridge;

public interface Driver
{

  public void init(Configuration configuration);

  public Session authenticate(String login, String password) throws PopException;

}
