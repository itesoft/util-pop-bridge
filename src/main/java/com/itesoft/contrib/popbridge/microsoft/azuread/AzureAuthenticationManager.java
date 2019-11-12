package com.itesoft.contrib.popbridge.microsoft.azuread;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.itesoft.contrib.popbridge.Configuration;

public class AzureAuthenticationManager
{
  
  private static Logger _logger = Logger.getLogger(AzureAuthenticationManager.class.getName());

  private Map<String, Class<? extends AuthenticationFlow>> REGISTERED_FLOWS = new HashMap<>();

  public AzureAuthenticationManager()
  {
    registerFlow(DeviceCodeFlow.class, "device", "devicecode", "code");
    registerFlow(AuthorizationFlow.class, "interactive", "authorization");
    registerFlow(IWAFlow.class, "iwa");
    registerFlow(LoginPassFlow.class, "password");
  }
  
  public void registerFlow(Class<? extends AuthenticationFlow> clazz, String... names)
  {
    for (String name: names)
    {
      REGISTERED_FLOWS.put(name.toLowerCase(), clazz);
    }
  }

  public AuthenticationFlow buildAuthenticationFlow(Configuration configuration, Set<String> scopes)
  {
    String authenticationFlow = configuration.getOption("authflow");
    Class<? extends AuthenticationFlow> clazz = REGISTERED_FLOWS.get(authenticationFlow.toLowerCase());
    if (clazz != null)
    {
      try
      {
        AuthenticationFlow flow = clazz.newInstance();
        flow.setScope(scopes);
        return flow;
      }
      catch (InstantiationException | IllegalAccessException e)
      {
        _logger.log(Level.SEVERE, "Could not instanciate class [" + clazz + "]", e);
        throw new RuntimeException(e);
      }
    }
    else
    {
      String errorMessage = "No class associated to authentication flow [" + authenticationFlow + "]";
      _logger.log(Level.SEVERE, errorMessage);
      throw new RuntimeException(errorMessage);
    }
  }
  
}
