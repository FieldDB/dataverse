/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.harvard.iq.dataverse;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author skraffmiller
 */

@NamedQueries({
	@NamedQuery( name="DataverseFeaturedDataverse.removeByOwnerId",
				 query="DELETE FROM DataverseFeaturedDataverse f WHERE f.dataverse.id=:ownerId")
})

@Entity
public class DataverseFeaturedDataverse implements Serializable {
        private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

  @ManyToOne
  @JoinColumn(name="dataverse_id")
  private Dataverse dataverse;

  @ManyToOne
  @JoinColumn(name="featureddataverse_id")
  private Dataverse featuredDataverse;
  
  private int displayOrder;

    public Dataverse getDataverse() {
        return dataverse;
    }

    public void setDataverse(Dataverse dataverse) {
        this.dataverse = dataverse;
    }

    public Dataverse getFeaturedDataverse() {
        return featuredDataverse;
    }

    public void setFeaturedDataverse(Dataverse featuredDataverse) {
        this.featuredDataverse = featuredDataverse;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    } 
  
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof DatasetFieldType)) {
            return false;
        }
        DataverseFeaturedDataverse other = (DataverseFeaturedDataverse) object;
        return !(!Objects.equals(this.id, other.id) && (this.id == null || !this.id.equals(other.id)));
    }

    @Override
    public String toString() {
        return "edu.harvard.iq.dataverse.DataverseFeaturedDataverse[ id=" + id + " ]";
    }
    
}
