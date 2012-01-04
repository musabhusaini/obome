package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Document extends Model {
    public String text;
    
    public Document(String text) {
    	this.text = text;
    }
}
