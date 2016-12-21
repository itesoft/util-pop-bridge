package com.itesoft.contrib.popbridge;

public class PopException extends Exception
{

  private static final long serialVersionUID = 1L;

  public PopException()
  {
    super();
  }

  public PopException(String message,
                      Throwable cause,
                      boolean enableSuppression,
                      boolean writableStackTrace)
  {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public PopException(String message, Throwable cause)
  {
    super(message, cause);
  }

  public PopException(String message)
  {
    super(message);
  }

  public PopException(Throwable cause)
  {
    super(cause);
  }

}
