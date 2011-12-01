package models;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import play.db.jpa.Model;

@Entity
public class User extends Model {
    public String name;
    public String email;
}
