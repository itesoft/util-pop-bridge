package com.itesoft.contrib.popbridge.microsoft.msgraph;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.IHttpRequest;

class AuthorizationBearerAuthenticationProvider implements IAuthenticationProvider
{

  private String _accessToken;
  
  public AuthorizationBearerAuthenticationProvider(String accessToken)
  {
    _accessToken = accessToken;
  }
  
  @Override
  public void authenticateRequest(IHttpRequest request)
  {
    try
    {
      request.addHeader("Authorization", "Bearer " + _accessToken);
    }
    catch (ClientException e)
    {
        e.printStackTrace();
    }
    catch (NullPointerException e)
    {
        e.printStackTrace();
    }
  }
  
}
