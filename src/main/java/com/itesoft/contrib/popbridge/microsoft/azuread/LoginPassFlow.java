package com.itesoft.contrib.popbridge.microsoft.azuread;

import java.util.concurrent.CompletableFuture;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;

public class LoginPassFlow extends BaseAuthenticationFlow
{

  @Override
  public void postInit()
  {
  }

  @Override
  public CompletableFuture<IAuthenticationResult> createAccessToken(String pop3user, String pop3password)
  {
    UserNamePasswordParameters parameters = UserNamePasswordParameters.builder(getScopes(), pop3user, pop3password.toCharArray()).build();
    return getClientApplication().acquireToken(parameters);
  }
  
}
