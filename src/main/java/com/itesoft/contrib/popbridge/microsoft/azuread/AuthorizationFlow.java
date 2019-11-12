package com.itesoft.contrib.popbridge.microsoft.azuread;

import static java.awt.Desktop.getDesktop;
import static java.awt.Desktop.isDesktopSupported;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.IAuthenticationResult;

class AuthorizationFlow extends BaseAuthenticationFlow
{

  private OAuth20Service _oAuthService;
  
  @Override
  public void postInit()
  {
    _oAuthService = new ServiceBuilder(getClientId())
        .callback("http://localhost:111/")
        .scope(String.join(" ", getScopes()))
        .build(new AuthorizationFlowApi());

    getAccessToken("", "");
  }

  @Override
  public CompletableFuture<IAuthenticationResult> createAccessToken(String pop3user, String pop3password)
  {
    return createAccessToken();
  }

  public CompletableFuture<IAuthenticationResult> createAccessToken()
  {
    String authorizationCode = getAuthorizationCode();
    AuthorizationCodeParameters parameters;
    try
    {
      parameters = AuthorizationCodeParameters.builder(authorizationCode, new URI("http://localhost:111/")).build();
    }
    catch (URISyntaxException e)
    {
      /* Should not happen as the URI is a constant */
      throw new RuntimeException(e);
    }
    return getClientApplication().acquireToken(parameters);
  }
  
  private String getAuthorizationCode()
  {
    HttpServer httpServer = new HttpServer(getConfiguration().getOption("httpport", "111"));
    CompletableFuture<String> futureAuthorizationCode = httpServer.getFutureCode();
    String authorizationUrl = _oAuthService.getAuthorizationUrl();
    httpServer.setMessage("Not authenticated", "Please go to <a href='" + authorizationUrl + "'>this page</a> to authenticate");
    if (isDesktopSupported())
    {
      System.out.println("Trying to open a web-browser to authenticate. If it fails, please go manually to following URL ON THIS COMPUTER:");
      System.out.println(authorizationUrl);
      try
      {
        getDesktop().browse(new URI(authorizationUrl));
      }
      catch (IOException | URISyntaxException e)
      {
        /* the user can go manually to the displayed URI */
      }
    }
    else
    {
      System.out.println("Please open a browser ON THIS COMPUTER and go to the URL below and authorize this application:");
      System.out.println(authorizationUrl);
    }

    futureAuthorizationCode.thenRun(() -> httpServer.setMessage("Authenticated", "You can now close your browser"));        
    String code = futureAuthorizationCode.join();

    return code;
  }
}
