package com.itesoft.contrib.popbridge.microsoft.msgraph;

import java.util.logging.Logger;

import com.itesoft.contrib.popbridge.Configuration;
import com.itesoft.contrib.popbridge.Driver;
import com.itesoft.contrib.popbridge.PopException;
import com.itesoft.contrib.popbridge.Session;
import com.itesoft.contrib.popbridge.microsoft.azuread.AuthenticationFlow;
import com.itesoft.contrib.popbridge.microsoft.azuread.AzureAuthenticationManager;

public class MsGraphDriver implements Driver, Constants
{
   
  private static Logger _logger = Logger.getLogger(MsGraphDriver.class.getName());

  private Configuration _configuration;
  private AuthenticationFlow _authenticationFlow;

  @Override
  public void init(Configuration configuration) 
  {
    _configuration = configuration;
    _authenticationFlow = new AzureAuthenticationManager().buildAuthenticationFlow(_configuration, MSGRAPH_SCOPES);
    _authenticationFlow.init(configuration);
  }

  protected Configuration getConfiguration()
  {
    return _configuration;
  }
  
  @Override
  public Session authenticate(String login, String password) throws PopException
  {
    return new MsGraphSession(_authenticationFlow.getAccessToken(login, password));
  }

}
