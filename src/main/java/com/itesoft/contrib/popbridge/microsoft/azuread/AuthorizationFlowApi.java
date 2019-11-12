package com.itesoft.contrib.popbridge.microsoft.azuread;

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.oauth2.clientauthentication.ClientAuthentication;
import com.github.scribejava.core.oauth2.clientauthentication.RequestBodyAuthenticationScheme;

class AuthorizationFlowApi extends DefaultApi20
{

  @Override
  public String getAccessTokenEndpoint()
  {
    return "https://login.microsoftonline.com/common/oauth2/v2.0/token";
  }

  @Override
  protected String getAuthorizationBaseUrl()
  {
    return "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
  }

  @Override
  public ClientAuthentication getClientAuthentication()
  {
    return RequestBodyAuthenticationScheme.instance();
  }
}
