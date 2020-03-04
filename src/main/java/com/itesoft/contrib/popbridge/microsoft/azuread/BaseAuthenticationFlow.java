package com.itesoft.contrib.popbridge.microsoft.azuread;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.itesoft.contrib.popbridge.Configuration;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.SilentParameters;

abstract class BaseAuthenticationFlow implements AuthenticationFlow, MsConstants
{

  private static Logger _logger = Logger.getLogger(BaseAuthenticationFlow.class.getName());

  private Configuration _configuration;

  private String _tenant;
  private String _clientId;
  private File _dataStore;
  private PublicClientApplication _clientApplication;
  private Set<String> _scopes;

  @Override
  public void init(Configuration configuration) 
  {
    _configuration = configuration;
    _clientId = configuration.getOption("clientid");
    _tenant = configuration.getOption("tenant");
    _dataStore =  new File(_configuration.getOption("dataStoreDir",
                           new java.io.File(System.getProperty("user.home"), ".pop-bridge/msgraph-driver").toString()));
    postInit();
  }

  public abstract void postInit();

  public Set<String> getScopes()
  {
    return _scopes;
  }
  
  @Override
  public void setScope(Set<String> scopes)
  {
    _scopes = Collections.unmodifiableSet(new HashSet<String>(scopes));
  }

  protected Configuration getConfiguration()
  {
    return _configuration;
  }

  protected String getClientId()
  {
    return _clientId;
  }


  protected PublicClientApplication buildPublicClientApplication()
  {
    try
    {
      return PublicClientApplication.builder(getClientId())
          .authority(getAuthorityURL())
          .setTokenCacheAccessAspect(new TokenPersistence(_dataStore))
          .build();
    }
    catch (MalformedURLException e)
    {
      /* should not happen as BASE_AUTHORITY is a well-formed constant */ 
      throw new RuntimeException(e);
    }
  }
  
  public PublicClientApplication getClientApplication()
  {
    if (_clientApplication == null)
    {
      _clientApplication = buildPublicClientApplication();
    }
    return _clientApplication;
  }
  
  protected String getAuthorityURL()
  {
    return BASE_AUTHORITY + _tenant;
  }

  public abstract CompletableFuture<IAuthenticationResult> createAccessToken(String pop3user, String pop3password);

  @Override
  public String getAccessToken(String pop3user, String pop3password)
  {
    IAuthenticationResult authenticationResult = null;
    CompletableFuture<IAuthenticationResult> futureAuthenticationResult = createAccessToken(pop3user, pop3password);
    try
    {
      authenticationResult = futureAuthenticationResult.join();
    }
    catch (CompletionException e)
    {
      throw new RuntimeException(e);
    }
    return authenticationResult.accessToken();
  }

  private IAuthenticationResult getAccessTokenFromCache()
  {

    Set<IAccount> accounts = getClientApplication().getAccounts().join();
    if(accounts.isEmpty()){
      _logger.warning("[getAccessTokenFromCache] No retrieved account with ClientApplication  [" + getClientApplication().clientId() + ": " + getClientApplication().authority() + "]");
      return null;
   }
   SilentParameters silentParameters = SilentParameters.builder(getScopes(), accounts.iterator().next()).build();

    CompletableFuture<IAuthenticationResult> future;
    try
    {
      future = getClientApplication().acquireTokenSilently(silentParameters);
    }
    catch (MalformedURLException e1)
    {
      throw new RuntimeException(e1);
    }
    IAuthenticationResult result;
    try
    {
      result = future.join();
    }
    catch (CompletionException e)
    {
      _logger.info("Could not refresh token [" + e.getClass() + ": " + e.getMessage() + "]");
      result = null;
    }
    return result;
  }
}
