package com.itesoft.contrib.popbridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PopWorker implements Runnable
{

  private static Logger _logger = Logger.getLogger(PopWorker.class.getName());
  private static Pattern TOP_PATTERN = Pattern.compile("^([0-9]+)\\s+([0-9]+)$");

  protected Socket _socket;
  protected BufferedReader _in;
  protected PrintWriter _out;

  private Driver _driver;
  private Session _session;

  private Set<Integer> _trash;
  private List<String> _messages;
  private boolean _authenticated = false;
  private String _login;

  public PopWorker(Driver driver, Socket socket)
  {
    setSocket(socket);
    _driver = driver;
    _trash = new HashSet<>();
  }

  private void setSocket(Socket socket)
  {
    _socket = socket;
    try
    {
      _in = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
      _out = new PrintWriter(_socket.getOutputStream(), true);
    }
    catch (IOException e)
    {
      _logger.log(Level.SEVERE, "Could not get IO streams from socket", e);
    }
  }

  public void close() throws IOException
  {
    if (_socket != null)
    {
      _socket.close();
    }
    _socket = null;
  }

  @Override
  public void run()
  {
    try
    {
      String line;
      boolean done = false;
      sendOk("bridge server ready");
      while (!done)
      {
        line = _in.readLine();
        if (line != null)
        {
          _logger.fine(">>>>> " + line);
          done = parseLine(line);
        }
        else
        {
          _logger.log(Level.INFO, "client socket closed without QUIT");
          done = true;
        }
      }
    }
    catch (Exception e)
    {
      _logger.log(Level.SEVERE, "exception", e);
    }
    finally
    {
      try
      {
        close();
        _logger.fine("Socket closed");
      }
      catch (Exception e)
      {
        _logger.log(Level.SEVERE, "Error while closing client socket", e);
      }
    }
  }

  private void sendOk(String line)
  {
    _out.print("+OK " + line + "\r\n");
    _logger.fine("<<<<< +OK " + line);
    _out.flush();
  }

  private void sendError(String line)
  {
    _out.print("-ERR " + line + "\r\n");
    _logger.fine("<<<<< -ERR " + line);
    _out.flush();
  }

  private List<String> getMessages() throws PopException
  {
    if (_messages == null)
    {
      _messages = _session.list();
    }
    return _messages;
  }

  private String getId(int index) throws PopException
  {
    return getMessages().get(index);
  }

  protected boolean authenticate(String login, String password)
  {
    return true;
  }

  private void checkAuthenticated() throws PopException
  {
    if (!_authenticated)
    {
      throw new PopException("Please authenticate first");
    }
  }

  public boolean parseLine(String line)
  {
    if (line == null)
    {
      sendOk("");
      return false;
    }
    int endCommandIndex = line.indexOf(' ');
    String args;
    if (endCommandIndex == -1)
    {
      args = "";
      endCommandIndex = line.length();
    }
    else
    {
      args = line.substring(endCommandIndex).trim();
    }
    String command = line.substring(0, endCommandIndex);
    try
    {
      switch(command.toUpperCase())
        {
        case "USER":
          {
            _login = args;
            sendOk("user " + _login + " accepted");
            return false;
          }
        case "PASS":
          {
            if (_login == null)
            {
              sendError("please provide username first");
              return false;
            }
            _session = _driver.authenticate(_login, args);
            if (_session != null)
            {
              _logger.fine("User" + _login + " logged in");
              _authenticated = true;
              sendOk("access granted");
            }
            return false;
          }
        case "QUIT":
          {
            if (_authenticated && !_trash.isEmpty())
            {
              try
              {
                update();
              }
              catch (PopException e)
              {
                sendError("some deleted messages were not removed (" + e.getMessage() + ")");
              }
              sendOk("goodbye (" + _trash.size() + " successfully messages deleted)");
            }
            else
            {
              sendOk("goodbye");
            }
            return true;
          }
        case "NOOP":
          {
            checkAuthenticated();
            sendOk("");
            return false;
          }
        case "LIST":
          {
            checkAuthenticated();
            List<String> messages = getMessages();
            if (args.isEmpty())
            {
              sendOk("List of all messages");
              for (int i = 0; i < messages.size(); i++)
              {
                _out.print((i + 1) + " " + _session.sizeOf(getId(i)) + "\r\n");
              }
              _out.print(".\r\n");
              _out.flush();
              return false;
            }
            else
            {
              int index = parseIndex(args);
              int size = _session.sizeOf(getId(index));
              sendOk(index + " " + size);
              return false;
            }
          }
        case "STAT":
          {
            checkAuthenticated();
            List<String> messages = getMessages();
            if (messages.isEmpty())
            {
              sendOk("0 0");
              return false;
            }
            int totalSize = 0;
            for (int i = 0; i < messages.size(); i++)
            {
              totalSize += _session.sizeOf(getId(i));
            }
            sendOk(getMessages().size() + " " + totalSize);
            return false;
          }
        case "RETR":
          {
            checkAuthenticated();
            int index = parseIndex(args);
            String mail = _session.retrieve(getId(index));
            if (mail != null)
            {
              sendOk("message " + getId(index) + " (" + (mail.length() / 1024) + " Kb)");
              _out.print(mail + "\r\n");
              _out.print(".\r\n");
              _out.flush();
            }
            return false;
          }
        case "TOP":
          {
            checkAuthenticated();
            Matcher matcher = TOP_PATTERN.matcher(args);
            matcher.find();
            int index = parseIndex(matcher.group(1));
            int count = 0;
            try
            {
              count = Integer.parseInt(matcher.group(2));
            }
            catch (NumberFormatException e)
            {
              sendError("Not a valid line count number " + matcher.group(2));
              return false;
            }
            String mail = _session.retrieve(getId(index));
            if (mail != null)
            {
              sendOk("top " + count + " lines of message " + getId(index));
              BufferedReader reader = new BufferedReader(new StringReader(mail));
              try
              {
                String currentLine = reader.readLine();
                for (int i = 0; currentLine != null && i < count; i++)
                {
                  _out.print(currentLine + "\r\n");
                  currentLine = reader.readLine();
                }
                _out.print(".\r\n");
                _out.flush();
              }
              catch (IOException e)
              {
                _logger.log(Level.SEVERE, "Unexpected in-memory IO Exception", e);
              }
            }
            return false;
          }
        case "UIDL":
          {
            checkAuthenticated();
            if (args.isEmpty())
            {
              List<String> messages = getMessages();
              if (messages.isEmpty())
              {
                sendOk("");
                _out.print(".\r\n");
                _out.flush();
                return false;
              }
              sendOk("");
              for (int i = 0; i < messages.size(); i++)
              {
                _out.print((i+1) + " " + getId(i) + "\r\n");
              }
              _out.print(".\r\n");
              _out.flush();
              return false;
            }
            else
            {
              int index = parseIndex(args);
              sendOk((index+1) + " " + getId(index));
              return false;
            }
          }
        case "DELE":
          {
            checkAuthenticated();
            int index = parseIndex(args);
            String id = getId(index);
            _trash.add(index);
            sendOk("Message " + (index+1) + " (UID " + id + ")" + " marked for deletion");
            return false;
          }
        case "RSET":
          {
            checkAuthenticated();
            int count = _trash.size();
            _trash = new HashSet<>();
            sendOk("" + count + " messages undeleted");
            return false;
          }
        default:
          {
            sendError("Command " + command + " is not supported");
            return false;
          }
        }
    }
    catch (PopException e)
    {
      _logger.log(Level.SEVERE, "Exception occured while processing line [" + line + "]", e);
      sendError(e.getMessage());
      return false;
    }
  }

  public int parseIndex(String arg) throws PopException
  {
    int index = 0;
    try
    {
      index = Integer.parseInt(arg);
    }
    catch (NumberFormatException e)
    {
      throw new PopException(arg + " is not a valid index number (not a number)");
    }
    if (index < 1)
    {
      throw new PopException(arg + " is not a valid index number (too low)");
    }
    else if (index > getMessages().size())
    {
      throw new PopException(arg + " is not a valid index number (too high)");
    }
    else if (_trash.contains(index))
    {
      throw new PopException(arg + " refer to a message marked for deletion");
    }
    return index - 1;
  }

  public void update() throws PopException
  {
    for (int index : _trash)
    {
      _session.delete(getId(index));
    }
  }
}
