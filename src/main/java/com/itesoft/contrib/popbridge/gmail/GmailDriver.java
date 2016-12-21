package com.itesoft.contrib.popbridge.gmail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.itesoft.contrib.popbridge.Configuration;
import com.itesoft.contrib.popbridge.Driver;
import com.itesoft.contrib.popbridge.Main;
import com.itesoft.contrib.popbridge.Session;

public class GmailDriver implements Driver
{

  private static Logger _logger = Logger.getLogger(Main.class.getName());

  private static final String APPLICATION_NAME = "GMAIL POP3 BRIDGE";
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
  private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_MODIFY);

  private HttpTransport _httpTransport;
  private FileDataStoreFactory _dataStoreFactory;
  private Gmail _service;
  private Configuration _configuration;
  private File _dataStoreDir;

  public Credential authorize() throws IOException
  {
    // Load client secrets.
    File clientSecretFile = new File(_configuration.getOption("clientsecret"));
    if (!clientSecretFile.exists())
    {
      System.err.println("File " + clientSecretFile + " does not exists");
    }
    try(InputStream in = new FileInputStream(clientSecretFile))
    {
      GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

      // Build flow and trigger user authorization request.
      GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(_httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
        .setDataStoreFactory(_dataStoreFactory)
        .setAccessType("offline")
        .build();
      Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
      _logger.info("Credentials saved to " + _dataStoreDir.getAbsolutePath());
      return credential;
    }
  }

  public Gmail getGmailService() throws IOException {
    Credential credential = authorize();
    return new Gmail.Builder(_httpTransport, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME)
        .build();
  }

  @Override
  public void init(Configuration configuration)
  {
    _configuration = configuration;
    try
    {
      _dataStoreDir = new File(_configuration.getOption("dataStoreDir",
                                                        new java.io.File(System.getProperty("user.home"), ".pop-bridge/gmail-driver").toString()));
      _httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      _dataStoreFactory = new FileDataStoreFactory(_dataStoreDir);
      _service = getGmailService();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Session authenticate(String login, String password)
  {
    return new GmailSession(_service, _configuration);
  }
}
