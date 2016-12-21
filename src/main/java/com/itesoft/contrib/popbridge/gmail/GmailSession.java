package com.itesoft.contrib.popbridge.gmail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.Messages.Get;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.ModifyMessageRequest;
import com.itesoft.contrib.popbridge.Configuration;
import com.itesoft.contrib.popbridge.PopException;
import com.itesoft.contrib.popbridge.PopWorker;
import com.itesoft.contrib.popbridge.Session;

public class GmailSession implements Session
{

  private static Logger _logger = Logger.getLogger(PopWorker.class.getName());

  private Gmail _service;
  private String _user;
  private String _query;
  private boolean _flagMarkAsRead;
  private boolean _flagArchiveOnDelete;
  private Configuration _configuration;

  public GmailSession(Gmail service, Configuration configuration)
  {
    _service = service;
    _configuration = configuration;
    _query = _configuration.getOption("q", configuration.getOption("query", "is:unread label:inbox"));
    _user = _configuration.getOption("user", "me");
    _flagMarkAsRead = _configuration.isFlagEnabled("markAsRead");
    _flagArchiveOnDelete = _configuration.isFlagEnabled("archiveOnDelete");
  }

  @Override
  public List<String> list() throws PopException
  {
    List<String> messageIds = new ArrayList<>();
    try
    {
      ListMessagesResponse listResponse = _service.users().messages().list(_user).setQ(_query).execute();
      while (listResponse.getMessages() != null)
      {
        for (Message message: listResponse.getMessages())
        {
          messageIds.add(message.getId());
        }
        if (listResponse.getNextPageToken() != null)
        {
          String pageToken = listResponse.getNextPageToken();
          listResponse = _service.users().messages().list(_user).setQ(_query).setPageToken(pageToken).execute();
        }
        else
        {
          break;
        }
      }
      return messageIds;
    }
    catch (IOException e)
    {
      _logger.log(Level.SEVERE, "Error while populating e-mails from Gmail", e);
      throw new PopException("Error while populating e-mails from Gmail");
    }
  }

  @Override
  public String retrieve(String id) throws PopException
  {
    try
    {
      Get getMessageResponse = _service.users().messages().get(_user, id);
      Message originalMail = getMessageResponse.setFormat("raw").execute();
      byte[] raw = originalMail.decodeRaw();
      if (_flagMarkAsRead)
      {
        ModifyMessageRequest modifications = new ModifyMessageRequest().setRemoveLabelIds(Arrays.asList("UNREAD"));
        _service.users().messages().modify(_user, id, modifications).execute();
      }
      return new String(raw);
    }
    catch (IOException e)
    {
      _logger.log(Level.SEVERE, "Error while retrieving e-mail " + id + " from Gmail", e);
      throw new PopException("Error while retrieving e-mail " + id + " from Gmail");
    }
  }

  @Override
  public void delete(String id) throws PopException
  {
    if (_flagArchiveOnDelete)
    {
      try
      {
        ModifyMessageRequest modifications = new ModifyMessageRequest().setRemoveLabelIds(Arrays.asList("INBOX"));
        _service.users().messages().modify(_user, id, modifications).execute();
      }
      catch (IOException e)
      {
        _logger.log(Level.SEVERE, "Error while archiving e-mail " + id + " from Gmail", e);
        throw new PopException("Error while archiving e-mail " + id + " from Gmail");
      }
    }
    else
    {
      try
      {
        _service.users().messages().trash(_user, id).execute();
      }
      catch (IOException e)
      {
        _logger.log(Level.SEVERE, "Error while deleting e-mail " + id + " from Gmail", e);
        throw new PopException("Error while deleting e-mail " + id + " from Gmail");
      }
    }
  }

  @Override
  public int sizeOf(String id) throws PopException
  {
    return 0;
  }
}
