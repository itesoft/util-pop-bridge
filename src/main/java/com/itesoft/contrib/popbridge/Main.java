package com.itesoft.contrib.popbridge;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import com.itesoft.contrib.popbridge.gmail.GmailDriver;
import com.itesoft.contrib.popbridge.microsoft.ews.EwsDriver;
import com.itesoft.contrib.popbridge.microsoft.msgraph.MsGraphDriver;

public class Main
{
  private static Map<String, Class<? extends Driver>> DRIVER_CLASSES = new HashMap<>();
  private static Logger _logger = Logger.getLogger(Main.class.getName());
  static
  {
    DRIVER_CLASSES.put("GMAIL", GmailDriver.class);
    DRIVER_CLASSES.put("MSGRAPH", MsGraphDriver.class);
    DRIVER_CLASSES.put("EWS", EwsDriver.class);
  }

  public static void help()
  {
    System.out.println("POP bridge");
    System.out.println("-----------");
    System.out.println();
    System.out.println("Parameters");
    System.out.println();
    System.out.println("-p <port>                  Listen incoming POP3 connections on <port> (default: 110)");
    System.out.println("--port <port>");
    System.out.println();
    System.out.println("-d <driver>                Use <driver> to fetch emails");
    System.out.println("--driver <driver>");
    System.out.println();
    System.out.println("-f <flag>,<flag>,<flag>    Set driver flag <flag> on");
    System.out.println("--flag <flag>,<flag>,...   (driver dependant)");
    System.out.println();
    System.out.println("-o <key>=<value>           Set driver option <key> to <value>");
    System.out.println("--option <key>=<value>     (driver dependant)");
    System.out.println();
    System.out.println("Currently known drivers: " + DRIVER_CLASSES.keySet());
    System.out.println();
    System.out.println();
    System.exit(0);
  }

  public static void main(String[] args) throws Exception
  {
    int port = 110;
    Class<? extends Driver> driverClass = null;
    Configuration driverOptions = new Configuration();
    LinkedList<String> arguments = new LinkedList<>(Arrays.asList(args));
    _logger.info("[main] ==> arguments [" + arguments + "]");
    while (!arguments.isEmpty())
    {
      String parameter = arguments.removeFirst();
      try
      {
        switch (parameter)
        {
        case "-p":
        case "--port":
          String portArg = arguments.removeFirst();
          try
          {
            port = Integer.parseInt(portArg);
          }
          catch (NumberFormatException e)
          {
            _logger.info("Invalid port " + portArg);
            System.exit(1);
          }
          break;
        case "-d":
        case "--driver":
          String driverArg = arguments.removeFirst().toUpperCase();
          Class<? extends Driver> specifiedDriverClass = DRIVER_CLASSES.get(driverArg);
          if (specifiedDriverClass == null)
          {
            _logger.info("Unknown driver " + driverArg);
            System.exit(1);
          }
          driverClass = specifiedDriverClass;
          break;
        case "-f":
        case "--flag":
          String flagArgs = arguments.removeFirst();
          for (String flag : flagArgs.split(","))
          {
            driverOptions.setFlag(flag.toLowerCase());
          }
          break;
        case "-o":
        case "--option":
          String optionArg = arguments.removeFirst();
          String[] optionArgParts = optionArg.split("=", 2);
          if (optionArgParts.length == 1)
          {
            _logger.info("Option must be <key>=<value>");
            System.exit(1);
          }
          String key = optionArgParts[0].toLowerCase();
          String value = optionArgParts[1];
          driverOptions.setOption(key, value);
          break;
        case "-h":
        case "--help":
          help();
          break;
        default:
          _logger.info("Unknown parameter '" + parameter + "'. Use '-h' for help.");
          System.exit(1);
        }
      }
      catch (NoSuchElementException e)
      {
        _logger.info("Parameter '" + parameter + "' expect more arguments");
        System.exit(1);
      }
    }

    if (driverClass == null)
    {
      _logger.info("Parameter --driver is required");
      System.exit(1);
    }
    Driver driver = driverClass.newInstance();
    driver.init(driverOptions);
    new PopServer(driver, port).join();
  }

}
