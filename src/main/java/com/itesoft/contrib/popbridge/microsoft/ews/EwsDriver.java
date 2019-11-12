package com.itesoft.contrib.popbridge.microsoft.ews;

import com.itesoft.contrib.popbridge.Configuration;
import com.itesoft.contrib.popbridge.Driver;
import com.itesoft.contrib.popbridge.PopException;
import com.itesoft.contrib.popbridge.Session;
import com.itesoft.contrib.popbridge.microsoft.azuread.AuthenticationFlow;
import com.itesoft.contrib.popbridge.microsoft.azuread.AzureAuthenticationManager;

import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.TokenCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;

public class EwsDriver implements Driver, EwsConstants
{

  private Configuration _configuration;
  private String _ewsUrl;
  private AuthenticationFlow _authenticationFlow;

  @Override
  public void init(Configuration configuration)
  {
    _configuration = configuration;
    _ewsUrl = configuration.getOption("url", OFFICE365_URL);
    String authflow = configuration.getOption("authflow", null);
    if (authflow != null)
    {
      _authenticationFlow = new AzureAuthenticationManager().buildAuthenticationFlow(_configuration, EWS_SCOPE);
      _authenticationFlow.init(configuration);
    }
  }

  @Override
  public Session authenticate(String login, String password) throws PopException
  {
    ExchangeCredentials credentials;
    if (_authenticationFlow != null)
    {
      try
      {
        credentials = new TokenCredentials(_authenticationFlow.getAccessToken(login, password));
      }
      catch (Exception e)
      {
        throw new PopException("Could not authenticate using Hybrid Modern Authentication flow [" + _authenticationFlow.getClass() + "]", e);
      }
    }
    else
    {
      credentials = new WebCredentials(login, password);
    }
    return new EwsSession(_ewsUrl,
                          credentials);
  }

}
