package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import com.google.common.collect.Iterables;

import java.util.*;

@Entity
public class ClusterHeads extends Model {
    public String[] seen;
    public String[] unseen;
    
    public ClusterHeads(Iterable<String> seen, Iterable<String> unseen) {
    	this.seen = Iterables.toArray(seen, String.class);
    	this.unseen = Iterables.toArray(unseen, String.class);
    }
}
