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
package org.kuali.rice.krms.impl.repository
import groovy.mock.interceptor.MockFor
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.kuali.rice.krad.data.DataObjectService
import org.kuali.rice.krms.api.repository.agenda.AgendaDefinition
import org.kuali.rice.krms.api.repository.type.KrmsAttributeDefinition

import static org.kuali.rice.krms.impl.repository.RepositoryTestUtils.buildQueryResults;

class AgendaBoServiceImplTest {

	private final shouldFail = new GroovyTestCase().&shouldFail
	def mockDataObjectService
    def mockAttributeDefinitionService

	private static final String NAMESPACE = "KRMS_TEST"
	private static final String AGENDA_ID_1 = "AGENDAID001"
	private static final String AGENDA_NAME = "Agenda1"
	private static final String TYPE_ID = "1234XYZ"
	private static final String CONTEXT_ID_1 = "CONTEXT-001"
	private static final String AGENDA_ITEM_ID_1 = "ITEM01"

	private static final String ATTR_NAME_1 = "Department"
	private static final String ATTR_VALUE_1 = "Biology"
	private static final String ATTR_DEF_ID_1 = "1001"
	private static final String ATTR_NAME_2 = "Fund"
	private static final String ATTR_VALUE_2 = "19900A"
	private static final String ATTR_DEF_ID_2 = "1002"
	
	private static final Long VERSION_NUMBER_1 = new Long(1);
	
	private static AgendaDefinition TEST_NEW_AGENDA_DEF;
	private static AgendaDefinition TEST_EXISTING_AGENDA_DEF;
	private static AgendaBo TEST_AGENDA_BO;
	
	private static AgendaDefinition TEST_AGENDA_ITEM_DEF;
	private static AgendaBo TEST_AGENDA_ITEM_BO;

    private static ContextBo CONTEXT1;
	private static KrmsAttributeDefinitionBo ADB1;
	private static KrmsAttributeDefinitionBo ADB2;

    static {
        CONTEXT1 = new ContextBo();
        CONTEXT1.setId(CONTEXT_ID_1);
        CONTEXT1.setNamespace("KRMS_TEST");
    }
	
	@BeforeClass
	static void createSamples() {
		// create two attributes
		Map<String,String> myAttrs = new HashMap<String,String>()
		myAttrs.put(ATTR_NAME_1, ATTR_VALUE_1)
		myAttrs.put(ATTR_NAME_2, ATTR_VALUE_2)
		
		// create a new agenda definition (null id, null version number)
		AgendaDefinition.Builder builder = AgendaDefinition.Builder.create(null, AGENDA_NAME, TYPE_ID, CONTEXT_ID_1)
		builder.setFirstItemId(AGENDA_ITEM_ID_1)
		builder.setAttributes(myAttrs);
		TEST_NEW_AGENDA_DEF = builder.build()
		
		// create existing definition (with id and version number)
		builder = AgendaDefinition.Builder.create(AGENDA_ID_1, AGENDA_NAME, TYPE_ID, CONTEXT_ID_1)
		builder.setFirstItemId(AGENDA_ITEM_ID_1)
		builder.setVersionNumber( VERSION_NUMBER_1 )
		builder.setAttributes(myAttrs);
		TEST_EXISTING_AGENDA_DEF = builder.build()
		
		// create Agenda bo
		TEST_AGENDA_BO = new AgendaBo()
		TEST_AGENDA_BO.setId( AGENDA_ID_1 )
		TEST_AGENDA_BO.setName( AGENDA_NAME )
		TEST_AGENDA_BO.setTypeId( TYPE_ID )
		TEST_AGENDA_BO.setContextId( CONTEXT_ID_1 )
		TEST_AGENDA_BO.setFirstItemId( AGENDA_ITEM_ID_1 )
		TEST_AGENDA_BO.setVersionNumber( VERSION_NUMBER_1 )
		
		// krmsAttributeDefinitionBos
		ADB1 = new KrmsAttributeDefinitionBo()
		ADB1.id = ATTR_DEF_ID_1
		ADB1.name = ATTR_NAME_1
		ADB1.namespace = NAMESPACE
		ADB2 = new KrmsAttributeDefinitionBo()
		ADB2.id = ATTR_DEF_ID_2
		ADB2.name = ATTR_NAME_2
		ADB2.namespace = NAMESPACE

		// build the set of agenda attribute BOs
		AgendaAttributeBo attributeBo1 = new AgendaAttributeBo();
		attributeBo1.setValue( ATTR_VALUE_1 );
		attributeBo1.attributeDefinition = ADB1;
		AgendaAttributeBo attributeBo2 = new AgendaAttributeBo();
		attributeBo2.setValue( ATTR_VALUE_2 );
		attributeBo2.attributeDefinition = ADB2;
		Set<AgendaAttributeBo> attributes = [attributeBo1, attributeBo2]
        
		TEST_AGENDA_BO.setAttributeBos(attributes);
	}

	@Before
	void setupBoServiceMockContext() {
		mockDataObjectService = new MockFor(DataObjectService.class)
	}

    @Before
	void setupAttributeServiceMockContext() {
		mockAttributeDefinitionService = new MockFor(KrmsAttributeDefinitionService.class);
	}

	@Test
	public void test_getAgendaByAgendaId() {
		mockDataObjectService.demand.find(1..1) {clazz, id -> TEST_AGENDA_BO}
		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()

		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
		AgendaDefinition myAgenda = service.getAgendaByAgendaId(AGENDA_ID_1)

		Assert.assertEquals(service.to(TEST_AGENDA_BO), myAgenda)
		mockDataObjectService.verify(dataObjectService)
	}

	@Test
	public void test_getAgendaByAgendaId_when_none_found() {
		mockDataObjectService.demand.find(1..1) {Class clazz, String id -> null}
		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()

		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
		AgendaDefinition myAgenda = service.getAgendaByAgendaId("I_DONT_EXIST")

		Assert.assertNull(myAgenda)
		mockDataObjectService.verify(dataObjectService)
	}

	@Test
	public void test_getAgendaByAgendaId_empty_id() {
		shouldFail(IllegalArgumentException.class) {
			new AgendaBoServiceImpl().getAgendaByAgendaId("")
		}
	}

	@Test
	public void test_getAgendaByAgendaId_null_action_id() {
		shouldFail(IllegalArgumentException.class) {
			new AgendaBoServiceImpl().getAgendaByAgendaId(null)
		}
	}
	
	@Test
	public void test_getAgendaByNameAndContextId() {
		mockDataObjectService.demand.findMatching(1..1) {clazz, map -> buildQueryResults([TEST_AGENDA_BO]) }
		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()

		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
		AgendaDefinition myAgenda = service.getAgendaByNameAndContextId(AGENDA_ID_1, CONTEXT_ID_1)

		Assert.assertEquals(service.to(TEST_AGENDA_BO), myAgenda)
		mockDataObjectService.verify(dataObjectService)
	}

	@Test
	public void test_getAgendaByNameAndContextId_when_none_found() {
		mockDataObjectService.demand.findMatching(1..1) { clazz, map -> buildQueryResults([]) }
		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()

		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
		AgendaDefinition myAgenda = service.getAgendaByNameAndContextId("I_DONT_EXIST", CONTEXT_ID_1)

		Assert.assertNull(myAgenda)
		mockDataObjectService.verify(dataObjectService)
	}

	@Test
	public void test_getAgendaByNameAndContextId_empty_name() {
		shouldFail(IllegalArgumentException.class) {
			new AgendaBoServiceImpl().getAgendaByNameAndContextId("", CONTEXT_ID_1)
		}
	}

	@Test
	public void test_getAgendaByNameAndContextId_null_name() {
		shouldFail(IllegalArgumentException.class) {
			new AgendaBoServiceImpl().getAgendaByNameAndContextId(null, CONTEXT_ID_1)
		}
	}

	@Test
	public void test_getAgendaByNameAndContextId_empty_context_id() {
		shouldFail(IllegalArgumentException.class) {
			new AgendaBoServiceImpl().getAgendaByNameAndContextId(AGENDA_ID_1, "")
		}
	}

	@Test
	public void test_getAgendaByNameAndContextId_null_context_id() {
		shouldFail(IllegalArgumentException.class) {
			new AgendaBoServiceImpl().getAgendaByNameAndContextId(AGENDA_ID_1, null)
		}
	}

	@Test
	public void test_getAgendasByContextId() {
		mockDataObjectService.demand.findMatching(1..1) { clazz, map -> buildQueryResults([TEST_AGENDA_BO])}
		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()

		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
		List<AgendaDefinition> myAgendas = service.getAgendasByContextId(CONTEXT_ID_1)

		Assert.assertEquals(service.to(TEST_AGENDA_BO), myAgendas.iterator().next())
		mockDataObjectService.verify(dataObjectService)
	}

	@Test
	public void test_getAgendasByContextId_when_none_found() {
		mockDataObjectService.demand.findMatching(1..1) { clazz, map -> buildQueryResults([])}
		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()

		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
		Set<AgendaDefinition> myAgendas = service.getAgendasByContextId("I_DONT_EXIST")

		Assert.assertEquals(myAgendas.size(), 0)
		mockDataObjectService.verify(dataObjectService)
	}

	@Test
	public void test_getAgendasByContextId_empty_id() {
		shouldFail(IllegalArgumentException.class) {
			new AgendaBoServiceImpl().getAgendasByContextId("")
		}
	}

	@Test
	public void test_getAgendasByContextId_null_rule_id() {
		shouldFail(IllegalArgumentException.class) {
			new AgendaBoServiceImpl().getAgendasByContextId(null)
		}
	}


  @Test
  public void test_createAgenda_null_input() {
	  def boService = mockDataObjectService.proxyDelegateInstance()
	  AgendaBoService service = new AgendaBoServiceImpl()
	  service.setDataObjectService(boService)
	  shouldFail(IllegalArgumentException.class) {
		  service.createAgenda(null)
	  }
	  mockDataObjectService.verify(boService)
  }

  @Test
  void test_createAgenda_exists() {
		mockDataObjectService.demand.findMatching(1..1) {
			clazz, map -> buildQueryResults([TEST_AGENDA_BO])
		}
		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()
		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
		shouldFail(IllegalStateException.class) {
			service.createAgenda(TEST_NEW_AGENDA_DEF)
		}
		mockDataObjectService.verify(dataObjectService)
  }

  @Test
  void test_createAgenda_success() {
		mockDataObjectService.demand.findMatching(1..1) { clazz, map -> buildQueryResults([]) }
		mockDataObjectService.demand.save { bo, po ->
            ((AgendaBo)bo).setId("1");
            return bo;
        }

		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()

        mockAttributeDefinitionService.demand.findAttributeDefinitionsByType { String typeId ->
            [KrmsAttributeDefinition.Builder.create(ADB1).build(), KrmsAttributeDefinition.Builder.create(ADB2).build()] };
        KrmsAttributeDefinitionService attributeDefinitionService = mockAttributeDefinitionService.proxyDelegateInstance();

		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
        service.setAttributeDefinitionService(attributeDefinitionService);

		KrmsAttributeDefinitionService kads = new KrmsAttributeDefinitionServiceImpl();
		kads.setDataObjectService(dataObjectService)
		KrmsRepositoryServiceLocator.setKrmsAttributeDefinitionService(kads)
				
		service.createAgenda(TEST_NEW_AGENDA_DEF)
		mockDataObjectService.verify(dataObjectService)
  }

  @Test
  public void test_updateAgenda_null_input() {
	  def boService = mockDataObjectService.proxyDelegateInstance()
	  AgendaBoService service = new AgendaBoServiceImpl()
	  service.setDataObjectService(boService)
	  shouldFail(IllegalArgumentException.class) {
		  service.updateAgenda(null)
	  }
	  mockDataObjectService.verify(boService)
  }

  @Test
  void test_updateAgenda_does_not_exist() {
		mockDataObjectService.demand.find(1..1) {
			Class clazz, String id -> null
		}
		DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()
		AgendaBoService service = new AgendaBoServiceImpl()
		service.setDataObjectService(dataObjectService)
		shouldFail(IllegalStateException.class) {
			service.updateAgenda(TEST_EXISTING_AGENDA_DEF)
		}
		mockDataObjectService.verify(dataObjectService)
  }

  @Test
  void test_updateAgenda_success() {
        mockDataObjectService.demand.find(1..1) { clazz, id -> TEST_AGENDA_BO }
        mockDataObjectService.demand.find(1..1) { clazz, id -> TEST_AGENDA_BO.getFirstItem() }
        mockDataObjectService.demand.deleteMatching(1) { clazz, map -> }
        mockDataObjectService.demand.save { bo, po ->
          ((AgendaBo)bo).setId("1");
          return bo;
        }

        mockAttributeDefinitionService.demand.findAttributeDefinitionsByType { String typeId ->
          [KrmsAttributeDefinition.Builder.create(ADB1).build(), KrmsAttributeDefinition.Builder.create(ADB2).build()] };
        KrmsAttributeDefinitionService attributeDefinitionService = mockAttributeDefinitionService.proxyDelegateInstance();

        DataObjectService dataObjectService = mockDataObjectService.proxyDelegateInstance()
        AgendaBoService service = new AgendaBoServiceImpl()
        service.setDataObjectService(dataObjectService)
        service.setAttributeDefinitionService(attributeDefinitionService);

        KrmsAttributeDefinitionService kads = new KrmsAttributeDefinitionServiceImpl();
        kads.setDataObjectService(dataObjectService)
        KrmsRepositoryServiceLocator.setKrmsAttributeDefinitionService(kads)

        service.updateAgenda(TEST_EXISTING_AGENDA_DEF)
      
        mockDataObjectService.verify(dataObjectService)
  }

}
