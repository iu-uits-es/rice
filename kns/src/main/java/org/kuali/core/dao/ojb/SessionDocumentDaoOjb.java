/*
 * Copyright 2005-2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.core.dao.ojb;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.core.bo.SessionDocument;
import org.kuali.core.dao.BusinessObjectDao;
import org.kuali.core.dao.SessionDocumentDao;
import org.kuali.rice.kns.util.KNSPropertyConstants;
import org.springframework.dao.DataAccessException;

/**
 * This class is the OJB implementation of the DocumentDao interface.
 */
public class SessionDocumentDaoOjb extends PlatformAwareDaoBaseOjb implements SessionDocumentDao{
    private static Logger LOG = Logger.getLogger(SessionDocumentDaoOjb.class);
    private BusinessObjectDao businessObjectDao;
    

    public SessionDocumentDaoOjb() {
        super();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.kuali.core.dao.SessionDocumentDao#purgeAllSessionDocuments(java.sql.Timestamp)
     */
    public void purgeAllSessionDocuments(Timestamp expirationDate)throws DataAccessException {
    	Criteria criteria = new Criteria();
		criteria.addLessThan(KNSPropertyConstants.LAST_UPDATED_DATE, expirationDate);
		 getPersistenceBrokerTemplate().deleteByQuery(QueryFactory.newQuery(SessionDocument.class, criteria));
		 //getPersistenceBrokerTemplate().clearCache();
	        
    }

 
}