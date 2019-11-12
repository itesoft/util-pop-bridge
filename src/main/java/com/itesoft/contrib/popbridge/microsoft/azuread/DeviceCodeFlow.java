package com.itesoft.contrib.popbridge.microsoft.azuread;

import java.util.concurrent.CompletableFuture;

import com.microsoft.aad.msal4j.DeviceCode;
import com.microsoft.aad.msal4j.DeviceCodeFlowParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;

class DeviceCodeFlow extends BaseAuthenticationFlow
{

  private HttpServer _httpServer;

  @Override
  public void postInit()
  {
    String httpPort = getConfiguration().getOption("httpport", null);
    if (httpPort != null)
    {
      _httpServer = new HttpServer(getConfiguration().getOption("httpport", "11080"));
    }
    getAccessToken("", "");
    setMessage("Authenticated", "You can close the browser now");
  }
  
  private void setMessage(String title, String message)
  {
    if (_httpServer != null)
    {
      _httpServer.setMessage(title, message);
    }
  }

  @Override
  public CompletableFuture<IAuthenticationResult> createAccessToken(String pop3user, String pop3password)
  {
    return createAccessToken();
  }

  public CompletableFuture<IAuthenticationResult> createAccessToken()
  {
    DeviceCodeFlowParameters parameters = DeviceCodeFlowParameters.builder(getScopes(), (DeviceCode deviceCode) ->
    {
        setMessage("Please authenticate", deviceCode.message() + "</p><p>"
            + "URI: <a href='" + deviceCode.verificationUri() + "'>" + deviceCode.verificationUri() + "</a><br/>"
            + "" + deviceCode.userCode());
        System.out.println(deviceCode.message());
    }).build();
    CompletableFuture<IAuthenticationResult> future = getClientApplication().acquireToken(parameters);
    future.thenRun(() -> {
      setMessage("Authenticated", "You can close the browser now");
      System.out.println("Authenticated");}
    );        
    return future;
  }
  
}
