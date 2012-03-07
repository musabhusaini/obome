package eu.ubipol.opinionmining.polarity_analyzer;

import java.util.Date;

import eu.ubipol.opinionmining.Comment;
import eu.ubipol.opinionmining.Domain;



public class CommentImpl implements Comment {
  private Long id;
  private String text;
  private Date date;
  private Domain domain;

  public CommentImpl(Long commentId, String commentText, Date commentDate, Domain commentDomain) {
    id = commentId;
    text = commentText;
    date = commentDate;
    domain = commentDomain;
  }

  public CommentImpl(Long commentId, String commentText) {
    id = commentId;
    text = commentText;
  }

  public void SetDate(Date commentDate) {
    date = commentDate;
  }

  public void SetDomain(Domain commentDomain) {
    domain = commentDomain;
  }

  public Long getId() {
    return id;
  }

  public String getText() {
    return text;
  }

  public Date getDate() {
    return date;
  }

  public Domain getDomain() {
    return domain;
  }
}
