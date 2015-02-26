/*
 * Copyright 2006-2014 The Kuali Foundation
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.framework.config.property.SimpleConfig;
import org.kuali.rice.krad.data.jpa.eclipselink.testentities.TestEntity;
import org.mockito.Mock;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.Order;
import javax.transaction.TransactionManager;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by fraferna on 8/29/14.
 */
public class NativeJpaQueryTranslatorTest {

    @Mock TransactionManager transactionManager;
    @Mock javax.transaction.UserTransaction userTransaction;

    private ClassPathXmlApplicationContext context;
    private EntityManagerFactory entityManagerFactory;

    @Before
    public void setUp() throws Exception {
        initializeConfig();
    }

    @After
    public void tearDown() throws Exception {
        if (context != null) {
            context.destroy();
        }
        ConfigContext.destroy();
    }

    private void initializeConfig() {
        SimpleConfig config = new SimpleConfig();
        config.putProperty("rice.krad.jpa.global.randomProperty", "randomValue");
        config.putProperty("rice.krad.jpa.global.eclipselink.weaving", "false");
        ConfigContext.init(config);
    }

    private void loadContext(String springXmlFile) throws Exception {
        this.context = new ClassPathXmlApplicationContext(springXmlFile, getClass());
        Map<String, EntityManagerFactory> factories =  context.getBeansOfType(EntityManagerFactory.class);
        assertEquals(1, factories.size());
        this.entityManagerFactory = factories.values().iterator().next();
    }

    @Test
    public void testOrderBy() throws Exception {
        loadContext(getClass().getSimpleName() + "_Minimal.xml");
        NativeJpaQueryTranslator translator = new NativeJpaQueryTranslator(entityManagerFactory.createEntityManager());
        QueryByCriteria.Builder criteriaBuilder = QueryByCriteria.Builder.create();
        criteriaBuilder.setOrderByAscending("id");
        NativeJpaQueryTranslator.TranslationContext c = translator.translateCriteria(TestEntity.class,
                criteriaBuilder.build());
        List<Order> orderList = c.query.getOrderList();
        assertEquals(orderList.size(), 1);
    }
}
