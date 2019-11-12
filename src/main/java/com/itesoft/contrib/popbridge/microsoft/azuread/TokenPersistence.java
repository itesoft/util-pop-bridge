package com.itesoft.contrib.popbridge.microsoft.azuread;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.logging.Logger;

import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;

class TokenPersistence implements ITokenCacheAccessAspect
{

  private Charset UTF8 = Charset.forName("UTF-8");
  private static Logger _logger = Logger.getLogger(TokenPersistence.class.getName());
  
  private File _dataStore;
  private File _dataFile;

  public TokenPersistence(File dataStore)
  {
    _dataStore = dataStore;
    if (!_dataStore.exists())
    {
      _dataStore.mkdirs();
    }
    if (!_dataStore.isDirectory())
    {
      throw new RuntimeException("[" + _dataStore.getAbsolutePath() + "] is expected to be a directory");
    }
    _dataFile = new File(_dataStore, "tokencache");
  }

  @Override
  public void beforeCacheAccess(ITokenCacheAccessContext tokenCacheAccessContext)
  {
    if (_dataFile.exists())
    {
      byte[] tokenStore;
      try
      {
        tokenStore = Files.readAllBytes(_dataFile.toPath());
      }
      catch (IOException e)
      {
        _logger.severe("Could not read file [" + _dataFile + "]: " + e.getClass() + " " + e.getMessage());
        return;
      }
      String data = new String(tokenStore, UTF8);
      tokenCacheAccessContext.tokenCache().deserialize(data);
    }
  }

  @Override
  public void afterCacheAccess(ITokenCacheAccessContext tokenCacheAccessContext)
  {
    if (tokenCacheAccessContext.hasCacheChanged())
    {
      String data = tokenCacheAccessContext.tokenCache().serialize();
      try
      {
        Files.write(_dataFile.toPath(), data.getBytes(UTF8));
      }
      catch (IOException e)
      {
        _logger.severe("Could not write file [" + _dataFile + "]: " + e.getClass() + " " + e.getMessage());
        return;
      }
    }
  }
  
}
