package com.itesoft.contrib.popbridge.microsoft.azuread;

import java.util.concurrent.CompletableFuture;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IntegratedWindowsAuthenticationParameters;

class IWAFlow extends BaseAuthenticationFlow
{

  @Override
  public void postInit()
  {
  }

  @Override
  public CompletableFuture<IAuthenticationResult> createAccessToken(String pop3user, String pop3password)
  {
    IntegratedWindowsAuthenticationParameters parameters = IntegratedWindowsAuthenticationParameters.builder(getScopes(), pop3user).build();
    return getClientApplication().acquireToken(parameters);
  }
  
}
