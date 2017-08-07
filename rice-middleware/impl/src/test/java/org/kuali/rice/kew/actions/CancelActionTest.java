/*
 * Copyright 2006-2017 The Kuali Foundation
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

package org.kuali.rice.kew.actions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.rice.kew.actionrequest.ActionRequestValue;
import org.kuali.rice.kew.api.action.ActionType;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kim.api.identity.principal.PrincipalContract;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CancelActionTest {

    private static final String PRINCIPAL_ID = "01234567";

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DocumentRouteHeaderValue routeHeaderValue;

    @Mock
    private  PrincipalContract principalContract;

    @Mock
    private ActionRequestValue actionRequestValue;

    private CancelAction cancelAction;

    private List<ActionRequestValue> actionRequestValues;

    @Before
    public void setUp() {
        cancelAction = new CancelAction(ActionType.CANCEL, routeHeaderValue, principalContract, "");
        when(routeHeaderValue.isStateInitiated()).thenReturn(false);
        when(routeHeaderValue.isStateSaved()).thenReturn(false);
        when(routeHeaderValue.getDocumentType().getInitiatorCancelPendingPolicy().getPolicyValue().booleanValue()).thenReturn(true);
        when(routeHeaderValue.getStatus()).thenReturn(DocumentStatus.SAVED);
        when(principalContract.getPrincipalId()).thenReturn(PRINCIPAL_ID);
        when(routeHeaderValue.getInitiatorPrincipalId()).thenReturn(PRINCIPAL_ID);
        actionRequestValues = Arrays.asList(actionRequestValue);
    }

    @Test
    public void testInitiatorCanCancelSavedDocumentWithInitiatorCancelPendingPolicy() {
        when(routeHeaderValue.getStatus()).thenReturn(DocumentStatus.ENROUTE);
        assertTrue(cancelAction.isActionCompatibleRequest(actionRequestValues));
        verify(actionRequestValue, never()).getActionRequested();
    }

    @Test
    public void testInitiatorCanCancelEnrouteDocumentWithInitiatorCancelPendingPolicy() {
        when(routeHeaderValue.getStatus()).thenReturn(DocumentStatus.EXCEPTION);
        assertTrue(cancelAction.isActionCompatibleRequest(actionRequestValues));
        verify(actionRequestValue, never()).getActionRequested();
    }

    @Test
    public void testInitiatorCancelPendingPolicyDoesNotApplyIfNotInDoctype() {
        when(routeHeaderValue.getDocumentType().getInitiatorCancelPendingPolicy().getPolicyValue().booleanValue()).thenReturn(false);
        cancelAction.isActionCompatibleRequest(actionRequestValues);
        // If we get down to iterating the action values, the initiatorCancelPendingPolicy did not apply
        verify(actionRequestValue, times(1)).getActionRequested();
    }

    @Test
    public void testInitiatorCancelPendingPolicyDoesNotApplyIfValueIsFalse() {
        when(routeHeaderValue.getDocumentType().getInitiatorCancelPendingPolicy().getPolicyValue().booleanValue()).thenReturn(false);
        cancelAction.isActionCompatibleRequest(actionRequestValues);
        verify(actionRequestValue, times(1)).getActionRequested();
    }

    @Test
    public void testInitiatorCancelPendingPolicyDoesNotApplyIfUserIsNotInitiator() {
        when(principalContract.getPrincipalId()).thenReturn("76543210");
        cancelAction.isActionCompatibleRequest(actionRequestValues);
        verify(actionRequestValue, times(1)).getActionRequested();
    }

    @Test
    public void testInitiatorCancelPendingPolicyDoesNotApplyIfStatusIsNotPending() {
        when(routeHeaderValue.getStatus()).thenReturn(DocumentStatus.FINAL);
        cancelAction.isActionCompatibleRequest(actionRequestValues);
        verify(actionRequestValue, times(1)).getActionRequested();
    }

}
