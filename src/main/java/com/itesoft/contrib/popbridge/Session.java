package com.itesoft.contrib.popbridge;

import java.util.List;

public interface Session
{
  public List<String> list() throws PopException;

  public String retrieve(String id) throws PopException;

  public void delete(String id) throws PopException;

  public int sizeOf(String id) throws PopException;
}
