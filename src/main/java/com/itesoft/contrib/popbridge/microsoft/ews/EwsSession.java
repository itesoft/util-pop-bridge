package com.itesoft.contrib.popbridge.microsoft.ews;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.itesoft.contrib.popbridge.PopException;
import com.itesoft.contrib.popbridge.Session;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.enumeration.service.SendCancellationsMode;
import microsoft.exchange.webservices.data.core.enumeration.service.calendar.AffectedTaskOccurrence;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.TokenCredentials;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;

public class EwsSession implements Session
{

  private ExchangeService _service;

  public EwsSession(String ewsURL,
                    ExchangeCredentials credentials)
  {
    try
    {
      _service = new ExchangeService();
      _service.setCredentials(credentials);
      if (ewsURL.contains("@"))
      {
        _service.autodiscoverUrl(ewsURL);
      }
      else
      {
        _service.setUrl(new URI(ewsURL));
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> list() throws PopException
  {
    try
    {
      ItemView view = new ItemView(10);
      FindItemsResults<Item> findResults = _service.findItems(WellKnownFolderName.MsgFolderRoot, view);
      _service.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
  
      List<Item> itemList = findResults.getItems();
      List<String> uniqueIdList = new ArrayList<String>(itemList.size());
      for(Item item : findResults.getItems())
      {
        uniqueIdList.add(item.getId().getUniqueId());
      }
      return uniqueIdList;
    }
    catch (Exception e)
    {
      throw new PopException(e);
    }
  }
  
  @Override
  public String retrieve(String id) throws PopException
  {
    EmailMessage email;
    try
    {
      email = EmailMessage.bind(_service, new ItemId(id));
    }
    catch (Exception e)
    {
      throw new PopException(e);
    }
    try
    {
      return email.getMimeContent().toString();
    }
    catch (ServiceLocalException e)
    {
      throw new PopException(e);
    }
  }
  
  @Override
  public void delete(String id) throws PopException
  {
    try
    {
      _service.deleteItem(new ItemId(id),
                          DeleteMode.MoveToDeletedItems,
                          SendCancellationsMode.SendToNone,
                          AffectedTaskOccurrence.SpecifiedOccurrenceOnly);
    }
    catch (Exception e)
    {
      throw new PopException(e);
    }
  }
  
  @Override
  public int sizeOf(String id) throws PopException
  {
    return 0;
  }
}
