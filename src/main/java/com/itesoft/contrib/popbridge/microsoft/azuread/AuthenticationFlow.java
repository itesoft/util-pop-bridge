package com.itesoft.contrib.popbridge.microsoft.azuread;

import java.util.Set;

import com.itesoft.contrib.popbridge.Configuration;

public interface AuthenticationFlow
{

  public void setScope(Set<String> scopes);

  public void init(Configuration configuration);

  public String getAccessToken(String pop3user, String pop3password);

}
