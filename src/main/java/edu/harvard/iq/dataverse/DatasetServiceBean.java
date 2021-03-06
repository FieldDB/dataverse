/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import edu.harvard.iq.dataverse.authorization.users.AuthenticatedUser;
import edu.harvard.iq.dataverse.authorization.users.User;
import edu.harvard.iq.dataverse.settings.SettingsServiceBean;
import java.io.OutputStream;
import static java.lang.Math.max;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.lang.RandomStringUtils;

/**
 *
 * @author skraffmiller
 */
@Stateless
@Named
public class DatasetServiceBean implements java.io.Serializable {

    private static final Logger logger = Logger.getLogger(DatasetServiceBean.class.getCanonicalName());
    @EJB
    IndexServiceBean indexService;

    @EJB
    DOIEZIdServiceBean doiEZIdServiceBean;

    @EJB
    SettingsServiceBean settingsService;

    @PersistenceContext(unitName = "VDCNet-ejbPU")
    private EntityManager em;

    public Dataset find(Object pk) {
        return em.find(Dataset.class, pk);
    }

    public List<Dataset> findByOwnerId(Long ownerId) {
        return findByOwnerId(ownerId, false);
    }

    public List<Dataset> findByOwnerId(Long ownerId, Boolean omitDeaccessioned) {
        List<Dataset> retList = new ArrayList();
        Query query = em.createQuery("select object(o) from Dataset as o where o.owner.id =:ownerId order by o.id");
        query.setParameter("ownerId", ownerId);
        if (!omitDeaccessioned) {
            return query.getResultList();
        } else {
            for (Object o : query.getResultList()) {
                Dataset ds = (Dataset) o;
                for (DatasetVersion dsv : ds.getVersions()) {
                    if (!dsv.isDeaccessioned()) {
                        retList.add(ds);
                        break;
                    }
                }
            }
            return retList;
        }
    }

    public List<Dataset> findAll() {
        return em.createQuery("select object(o) from Dataset as o order by o.id").getResultList();
    }

    /**
     * @todo write this method for real. Don't just iterate through every single
     * dataset! See https://redmine.hmdc.harvard.edu/issues/3988
     */
    public Dataset findByGlobalId(String globalId) {

        String protocol = "";
        String authority = "";
        String identifier = "";
        int index1 = globalId.indexOf(':');
        String nonNullDefaultIfKeyNotFound = ""; 
        String separator = settingsService.getValueForKey(SettingsServiceBean.Key.DoiSeparator, nonNullDefaultIfKeyNotFound);        
        int index2 = globalId.indexOf(separator, index1 + 1);
        int index3 = 0;
        if (index1 == -1) {
            throw new EJBException("Error parsing identifier: " + globalId + ". ':' not found in string");
        } else {
            protocol = globalId.substring(0, index1);
        }
        if (index2 == -1 ) {
            throw new EJBException("Error parsing identifier: " + globalId + ". Second separator not found in string");
        } else {
            if (index2 != -1) {
                authority = globalId.substring(index1 + 1, index2);
            }
        }
        if (protocol.equals("doi")) {

            index3 = globalId.indexOf(separator, index2 + 1);
            if (index3 == -1 ) {
                identifier = globalId.substring(index2 + 1).toUpperCase();
            } else {
                if (index3 > -1) {
                    authority = globalId.substring(index1 + 1, index3);
                    identifier = globalId.substring(index3 + 1).toUpperCase();
                }
            }
        } else {
            identifier = globalId.substring(index2 + 1).toUpperCase();
        }
        String queryStr = "SELECT s from Dataset s where s.identifier = :identifier  and s.protocol= :protocol and s.authority= :authority";
        Dataset foundDataset = null;
        try {
            Query query = em.createQuery(queryStr);
            query.setParameter("identifier", identifier);
            query.setParameter("protocol", protocol);
            query.setParameter("authority", authority);
            foundDataset = (Dataset) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            System.out.print("no ds found: " + globalId);
            // DO nothing, just return null.
        }
        return foundDataset;
    }

    public String generateIdentifierSequence(String protocol, String authority, String separator) {

        String identifier = null;
        do {
            identifier = RandomStringUtils.randomAlphanumeric(6).toUpperCase();  
        } while (!isUniqueIdentifier(identifier, protocol, authority, separator));

        return identifier;
    }

    /**
     * Check that a identifier entered by the user is unique (not currently used
     * for any other study in this Dataverse Network) alos check for duplicate
     * in EZID if needed
     */
    public boolean isUniqueIdentifier(String userIdentifier, String protocol, String authority, String separator) {
        String query = "SELECT d FROM Dataset d WHERE d.identifier = '" + userIdentifier + "'";
        query += " and d.protocol ='" + protocol + "'";
        query += " and d.authority = '" + authority + "'";
        boolean u = em.createQuery(query).getResultList().size() == 0;
        String nonNullDefaultIfKeyNotFound = "";
        String doiProvider = settingsService.getValueForKey(SettingsServiceBean.Key.DoiProvider, nonNullDefaultIfKeyNotFound);
        if (doiProvider.equals("EZID")) {
            if (!doiEZIdServiceBean.lookupMetadataFromIdentifier(protocol, authority, separator, userIdentifier).isEmpty()) {
                u = false;
            }
        }
        return u;
    }

    public String getRISFormat(DatasetVersion version) {
        String publisher = version.getRootDataverseNameforCitation();
        List<DatasetAuthor> authorList = version.getDatasetAuthors();
        String retString = "Provider: " + publisher + "\r\n";
        retString += "Content: text/plain; charset=\"us-ascii\"" + "\r\n";
        retString += "TY  - DATA" + "\r\n";
        retString += "T1  - " + version.getTitle() + "\r\n";
        for (DatasetAuthor author : authorList) {
            retString += "AU  - " + author.getName().getDisplayValue() + "\r\n";
        }
        retString += "DO  - " + version.getDataset().getProtocol() + "/" + version.getDataset().getAuthority() + "/" + version.getDataset().getId() + "\r\n";
        retString += "PY  - " + version.getVersionYear() + "\r\n";
        retString += "UR  - " + version.getDataset().getPersistentURL() + "\r\n";
        retString += "PB  - " + publisher + "\r\n";
        retString += "ER  - \r\n";
        return retString;
    }

    private XMLOutputFactory xmlOutputFactory = null;

    public void createXML(OutputStream os, DatasetVersion datasetVersion) {

        xmlOutputFactory = javax.xml.stream.XMLOutputFactory.newInstance();
        XMLStreamWriter xmlw = null;
        try {
            xmlw = xmlOutputFactory.createXMLStreamWriter(os);
            xmlw.writeStartDocument();
            createEndNoteXML(xmlw, datasetVersion);
            xmlw.writeEndDocument();
        } catch (XMLStreamException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
            throw new EJBException("ERROR occurred during creating endnote xml.", ex);
        } finally {
            try {
                if (xmlw != null) {
                    xmlw.close();
                }
            } catch (XMLStreamException ex) {
            }
        }
    }

    private void createEndNoteXML(XMLStreamWriter xmlw, DatasetVersion version) throws XMLStreamException {

        String title = version.getTitle();
        String versionYear = version.getVersionYear();
        String publisher = version.getRootDataverseNameforCitation();

        List<DatasetAuthor> authorList = version.getDatasetAuthors();

        xmlw.writeStartElement("xml");
        xmlw.writeStartElement("records");

        xmlw.writeStartElement("record");

        //<ref-type name="Dataset">59</ref-type> - How do I get that and xmlw.write it?
        xmlw.writeStartElement("ref-type");
        xmlw.writeAttribute("name", "Dataset");
        xmlw.writeCharacters(version.getDataset().getId().toString());
        xmlw.writeEndElement(); // ref-type

        xmlw.writeStartElement("contributors");
        xmlw.writeStartElement("authors");
        for (DatasetAuthor author : authorList) {
            xmlw.writeStartElement("author");
            xmlw.writeCharacters(author.getName().getDisplayValue());
            xmlw.writeEndElement(); // author                    
        }
        xmlw.writeEndElement(); // authors 
        xmlw.writeEndElement(); // contributors 

        xmlw.writeStartElement("titles");
        xmlw.writeStartElement("title");
        xmlw.writeCharacters(title);
        xmlw.writeEndElement(); // title
        xmlw.writeEndElement(); // titles

        xmlw.writeStartElement("section");
        String sectionString = "";
        if (version.getDataset().isReleased()) {
            sectionString = new SimpleDateFormat("yyyy-MM-dd").format(version.getDataset().getPublicationDate());
        } else {
            sectionString = new SimpleDateFormat("yyyy-MM-dd").format(version.getLastUpdateTime());
        }

        xmlw.writeCharacters(sectionString);
        xmlw.writeEndElement(); // publisher

        xmlw.writeStartElement("dates");
        xmlw.writeStartElement("year");
        xmlw.writeCharacters(versionYear);
        xmlw.writeEndElement(); // year
        xmlw.writeEndElement(); // dates

        xmlw.writeStartElement("publisher");
        xmlw.writeCharacters(publisher);
        xmlw.writeEndElement(); // publisher

        xmlw.writeStartElement("urls");
        xmlw.writeStartElement("related-urls");
        xmlw.writeStartElement("url");
        xmlw.writeCharacters(version.getDataset().getPersistentURL());
        xmlw.writeEndElement(); // url
        xmlw.writeEndElement(); // related-urls
        xmlw.writeEndElement(); // urls

        xmlw.writeStartElement("electronic-resource-num");
        String electResourceNum = version.getDataset().getProtocol() + "/" + version.getDataset().getAuthority() + "/" + version.getDataset().getId();
        xmlw.writeCharacters(electResourceNum);
        xmlw.writeEndElement();
        //<electronic-resource-num>10.3886/ICPSR03259.v1</electronic-resource-num>                  
        xmlw.writeEndElement(); // record

        xmlw.writeEndElement(); // records
        xmlw.writeEndElement(); // xml

    }

    public DatasetVersionUser getDatasetVersionUser(DatasetVersion version, User user) {

        DatasetVersionUser ddu = null;
        Query query = em.createQuery("select object(o) from DatasetVersionUser as o "
                + "where o.datasetversionid =:versionId and o.userIdentifier =:userId");
        query.setParameter("versionId", version.getId());
        query.setParameter("userId", user.getIdentifier());
        System.out.print("versionId: " + version.getId());
        System.out.print("userId: " + user.getIdentifier());
        System.out.print(query.toString());
        try {
            ddu = (DatasetVersionUser) query.getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            // DO nothing, just return null.
        }
        return ddu;
    }

    public List<DatasetLock> getDatasetLocks() {
        String query = "SELECT sl FROM DatasetLock sl";
        return (List<DatasetLock>) em.createQuery(query).getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void addDatasetLock(Long datasetId, Long userId, String info) {

        Dataset dataset = em.find(Dataset.class, datasetId);
        DatasetLock lock = new DatasetLock();
        lock.setDataset(dataset);
        lock.setInfo(info);
        lock.setStartTime(new Date());

        if (userId != null) {
            AuthenticatedUser user = em.find(AuthenticatedUser.class, userId);
            lock.setUser(user);
            if (user.getDatasetLocks() == null) {
                user.setDatasetLocks(new ArrayList());
            }
            user.getDatasetLocks().add(lock);
        }

        dataset.setDatasetLock(lock);
        em.persist(lock);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void removeDatasetLock(Long datasetId) {
        Dataset dataset = em.find(Dataset.class, datasetId);
        //em.refresh(dataset); (?)
        DatasetLock lock = dataset.getDatasetLock();
        if (lock != null) {
            AuthenticatedUser user = lock.getUser();
            dataset.setDatasetLock(null);
            user.getDatasetLocks().remove(lock);
            /* 
             * TODO - ?
             * throw an exception if for whatever reason we can't remove the lock?
             try {
             */
            em.remove(lock);
            /*
             } catch (TransactionRequiredException te) {
             ...
             } catch (IllegalArgumentException iae) {
             ...
             }
             */
        }
    }
}
