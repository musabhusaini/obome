package eu.ubipol.opinionmining.polarity_analyzer;

import eu.ubipol.opinionmining.Domain;

public class DomainImpl implements Domain {
  private Long id;
  private String name;

  public DomainImpl(Long domainId, String domainName) {
    id = domainId;
    name = domainName;
  }

  public DomainImpl(Long domainId) {
    id = domainId;
  }

  public void SetName(String domainName) {
    name = domainName;
  }

  public Long getId() {
    return id;
  }

  public String getText() {
    return name;
  }

}
