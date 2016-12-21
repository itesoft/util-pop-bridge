package com.itesoft.contrib.popbridge;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

public class PopServer extends Thread
{
  private static Logger _logger = Logger.getLogger(PopServer.class.getName());

  private int _port;
  private ServerSocket _serverSocket;
  private boolean _terminated = false;
  private Driver _driver;

  public PopServer(Driver driver, int port) throws IOException
  {
    _port = port;
    _driver = driver;
    _logger.info("Starting POP3 server on port " + _port);

    try
    {
      _serverSocket = new ServerSocket(_port);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(1);
    }
    start();
    _logger.info("Server started, listening on port " + _port);
  }

  @Override
  public void run()
  {
    Socket clientSocket;
    while (!_terminated)
    {
      try
      {
        clientSocket = _serverSocket.accept();
      }
      catch (SocketException e)
      {
        e.printStackTrace();
        break;
      }
      catch (IOException e)
      {
        e.printStackTrace();
        break;
      }
      try
      {
        if (isSocketAllowed(clientSocket))
        {
          _logger.info("Connection accepted from " + clientSocket.getInetAddress());
          Thread newThread = new Thread(new PopWorker(_driver, clientSocket));
          newThread.run();
        }
        else
        {
          _logger.warning("Connection rejected for remote IP " + clientSocket.getInetAddress());
          clientSocket.close();
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    shutdown();
  }

  protected boolean isSocketAllowed(Socket socket)
  {
    return true;
  }

  public void shutdown()
  {
    _logger.info("Shutting down the POP3 Server");
    _terminated = true;
    interrupt();
    try
    {
      if (_serverSocket != null)
      {
        _serverSocket.close();
      }
    }
    catch (IOException e)
    {
    }
    _serverSocket = null;
    _logger.info("POP3 Server shut down");
  }

}
