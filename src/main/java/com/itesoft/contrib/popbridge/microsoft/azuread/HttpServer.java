package com.itesoft.contrib.popbridge.microsoft.azuread;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import fi.iki.elonen.NanoHTTPD;

public class HttpServer extends NanoHTTPD
{

  private String _title;
  private String _message;
  private CompletableFuture<String> _future;

  public HttpServer(String port)
  {
    super(Integer.parseInt(port));
    try
    {
      start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }

  public synchronized CompletableFuture<String> getFutureCode()
  {
    if (_future == null)
    {
      _future = new CompletableFuture<>();
    }
    return _future;
  }
  
  public void setMessage(String title, String message)
  {
    _title = title;
    _message = message;
  }

  @Override
  public Response serve(IHTTPSession session)
  {
    synchronized(this)
    {
      if (_future != null)
      {
        _future.complete(session.getParms().get("code"));
        _future = null;
      }
    }
    return newFixedLengthResponse("<html><body><h1>" + _title + "</h1><p>" + _message + "</p></body></html>");
  }

}
