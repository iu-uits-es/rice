/*
 * Copyright 2005-2006 The Kuali Foundation.
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
package org.kuali.core.workflow.service;

import java.rmi.RemoteException;
import java.util.List;

import edu.iu.uis.eden.clientapp.vo.ActionRequestVO;
import edu.iu.uis.eden.clientapp.vo.ActionTakenVO;
import edu.iu.uis.eden.clientapp.vo.DocumentSearchCriteriaVO;
import edu.iu.uis.eden.clientapp.vo.DocumentSearchResultVO;
import edu.iu.uis.eden.clientapp.vo.DocumentTypeVO;
import edu.iu.uis.eden.clientapp.vo.ReportCriteriaVO;
import edu.iu.uis.eden.clientapp.vo.RouteHeaderVO;
import edu.iu.uis.eden.clientapp.vo.RouteTemplateEntryVO;
import edu.iu.uis.eden.clientapp.vo.UserIdVO;
import edu.iu.uis.eden.clientapp.vo.UserVO;
import edu.iu.uis.eden.clientapp.vo.WorkgroupIdVO;
import edu.iu.uis.eden.clientapp.vo.WorkgroupVO;
import edu.iu.uis.eden.exception.WorkflowException;

/**
 * 
 * This class...
 * 
 * 
 */
public interface KualiWorkflowInfo {
    public abstract RouteHeaderVO getRouteHeader(UserIdVO userId, Long routeHeaderId) throws WorkflowException;

    public abstract RouteHeaderVO getRouteHeader(Long routeHeaderId) throws WorkflowException;

    /**
     * @deprecated
     */
    public abstract WorkgroupVO getWorkgroup(String workgroupName) throws WorkflowException;

    /**
     * @deprecated
     */
    public abstract WorkgroupVO getWorkgroup(Long workgroupId) throws WorkflowException;

    public abstract WorkgroupVO getWorkgroup(WorkgroupIdVO workgroupId) throws WorkflowException;

    public abstract UserVO getWorkflowUser(UserIdVO userId) throws WorkflowException;

    /**
     * @deprecated use getDocType using the name
     */
    public abstract RouteTemplateEntryVO[] getRoute(String documentTypeName) throws WorkflowException;

    public abstract DocumentTypeVO getDocType(Long documentTypeId) throws WorkflowException;

    public abstract DocumentTypeVO getDocType(String documentTypeName) throws WorkflowException;

    public abstract Long getNewResponsibilityId() throws WorkflowException;

    public abstract WorkgroupVO[] getUserWorkgroups(UserIdVO userId) throws WorkflowException;

    public abstract ActionRequestVO[] getActionRequests(Long routeHeaderId) throws WorkflowException;

    public abstract ActionRequestVO[] getActionRequests(Long routeHeaderId, String nodeName, UserIdVO userId) throws WorkflowException;
    
    public abstract ActionTakenVO[] getActionsTaken(Long routeHeaderId) throws WorkflowException;

    public abstract void reResolveRole(String documentTypeName, String roleName, String qualifiedRoleNameLabel) throws WorkflowException;

    public abstract void reResolveRole(Long routeHeaderId, String roleName, String qualifiedRoleNameLabel) throws WorkflowException;

    /**
     * 
     * Determines whether the given routeHeaderId (also known as a documentNumber, or a docHeaderId) exists and is
     * retrievable in workflow.
     * 
     * @param routeHeaderId The docHeaderId/finDocNumber you would like to test.
     * @return True if the document exists in workflow and is retrievable without errors, False otherwise.
     * 
     */
    public abstract boolean routeHeaderExists(Long routeHeaderId);

    /**
     * Determines if a document generated (or retrieved) using the given criteria has (or will have) an action request using
     * one of the given action request codes.  User may or may not pass in a target node name inside the ReportCriteriaVO object.
     * 
     * @param reportCriteriaVO  - Holds either a document type name or a document id as well as other data to help simulate routing
     * @param actionRequestedCodes - List of Action Request Codes from the Workflow system
     * @param ignoreCurrentlyActiveRequests determines if method should look only at simulation generated requests 
     *        or both simulation generated requests and requests that are currently active on the document
     * @return true if the document has or will have at least one request that matches the criteria and has a requested code that matches one of the given codes
     * @throws WorkflowException
     */
    public boolean documentWillHaveAtLeastOneActionRequest(ReportCriteriaVO reportCriteriaVO, String[] actionRequestedCodes, boolean ignoreCurrentlyActiveRequests) throws WorkflowException;
    
    /**
     * @deprecated use {@link #documentWillHaveAtLeastOneActionRequest(ReportCriteriaVO, String[], boolean)} instead
     * 
     * Use of this method passes the value 'false' in for the <code>ignoreCurrentlyActiveRequests</code> parameter of {@link #documentWillHaveAtLeastOneActionRequest(ReportCriteriaVO, String[], boolean)}
     */
    public boolean documentWillHaveAtLeastOneActionRequest(ReportCriteriaVO reportCriteriaVO, String[] actionRequestedCodes) throws WorkflowException;

    /**
     * This method returns a list of Universal User Ids that have approval or completion requested of them for the document represented by the routeHeaderId parameter
     * 
     * @param routeHeaderId - the id of the document to check
     * @return a list of Universal User Ids that have approval or completion requested of them for the document with the given route header id
     * @throws WorkflowException
     */
    public List<String> getApprovalRequestedUsers(Long routeHeaderId) throws WorkflowException;

    /**
     * This method allows a document search to be executed just as would occur from the User Interface
     * 
     * @param criteriaVO - criteria to use for the search
     * @return a {@link DocumentSearchResultVO} object containing a list of search result columns and data rows
     * @throws RemoteException
     * @throws WorkflowException
     */
    public DocumentSearchResultVO performDocumentSearch(DocumentSearchCriteriaVO criteriaVO) throws WorkflowException;
    
    public DocumentSearchResultVO performDocumentSearch(UserIdVO userId, DocumentSearchCriteriaVO criteriaVO) throws RemoteException, WorkflowException;    
}