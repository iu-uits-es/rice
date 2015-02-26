/**
 * Copyright 2005-2014 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.data.jpa;

import org.kuali.rice.core.api.criteria.QueryByCriteria;

/**
 * Translates queries from generic API classes to platform-specific concrete classes.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
interface QueryTranslator<C, Q> {

    /**
     * Begin IU Customization
     * 2014-08-28 - Francis Fernandez (fraferna@iu.edu)
     * EN-3820
     *
     * Modify Predicate param to QueryByCriteria param so we can extract ORDER BY expressions.
     */
    /**
     * Translates the given {@link org.kuali.rice.core.api.criteria.Predicate} to a platform-specific criteria.
     *
     * @param queryClazz the type of the query.
     * @param predicate  the {@link org.kuali.rice.core.api.criteria.QueryByCriteria} to translate.
     * @return a criteria for the given {@link org.kuali.rice.core.api.criteria.QueryByCriteria}.
     */
    C translateCriteria(Class queryClazz, QueryByCriteria predicate);
    /**
     * End IU Customization
     */

    /**
     * Creates a query from the given criteria.
     *
     * @param queryClazz the type of the query.
     * @param criteria the criteria to translate.
     * @return a query from the given criteria.
     */
    Q createQuery(Class queryClazz, C criteria);

    /**
     * Translates the {@link QueryByCriteria} flags to the query.
     * @param qbc the {@link QueryByCriteria} to translate from.
     * @param query the query to translate to.
     */
    void convertQueryFlags(QueryByCriteria qbc, Q query);
}