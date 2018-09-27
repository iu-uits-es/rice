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
package org.kuali.rice.kew.rule.dao.impl;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.exception.RiceRuntimeException;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.rule.RuleBaseValues;
import org.kuali.rice.kew.rule.RuleDelegationBo;
import org.kuali.rice.kew.rule.RuleExtensionBo;
import org.kuali.rice.kew.rule.RuleResponsibilityBo;
import org.kuali.rice.kew.rule.dao.RuleDelegationDAO;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.data.DataObjectService;
import org.springframework.beans.factory.annotation.Required;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equal;

public class RuleDelegationDAOJpa implements RuleDelegationDAO {

    private EntityManager entityManager;
    private DataObjectService dataObjectService;

    public List<RuleDelegationBo> findByDelegateRuleId(String ruleId) {
        org.kuali.rice.core.api.criteria.QueryByCriteria.Builder builder =
                org.kuali.rice.core.api.criteria.QueryByCriteria.Builder.create();
        /**
         * Begin IU Customization
         * 2014-10-07 - Francis Fernandez (fraferna@iu.edu)
         * EN-3889
         *
         * Look up delegateRuleID by the correct field.
         */
        builder.setPredicates(equal("delegateRuleId", ruleId));
        /**
         * End IU Customization
         */
        return getDataObjectService().findMatching(RuleDelegationBo.class,builder.build()).getResults();
    }

    public void save(RuleDelegationBo ruleDelegation) {
        getDataObjectService().save(ruleDelegation);
    }

    public List<RuleDelegationBo> findAllCurrentRuleDelegations(){
        org.kuali.rice.core.api.criteria.QueryByCriteria.Builder builder =
                org.kuali.rice.core.api.criteria.QueryByCriteria.Builder.create();
        builder.setPredicates(equal("delegationRule.currentInd",true));
        return getDataObjectService().findMatching(RuleDelegationBo.class,builder.build()).getResults();
    }

    public RuleDelegationBo findByRuleDelegationId(String ruleDelegationId){
        return getDataObjectService().find(RuleDelegationBo.class, ruleDelegationId);

    }
    public void delete(String ruleDelegationId){
        getDataObjectService().delete(findByRuleDelegationId(ruleDelegationId));
    }


    public List<RuleDelegationBo> findByResponsibilityIdWithCurrentRule(String responsibilityId) {
        if (StringUtils.isBlank(responsibilityId)){
            return null;
        }

        org.kuali.rice.core.api.criteria.QueryByCriteria.Builder builder =
                org.kuali.rice.core.api.criteria.QueryByCriteria.Builder.create();
        builder.setPredicates(equal("delegationRule.currentInd",true),
                equal("responsibilityId",responsibilityId));
        return getDataObjectService().findMatching(RuleDelegationBo.class,builder.build()).getResults();
    }
    private Subquery<RuleResponsibilityBo> getResponsibilitySubQuery(String ruleResponsibilityName, CriteriaQuery<RuleBaseValues> query){
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        Subquery<RuleResponsibilityBo> ruleResponsibilitySubquery = query.subquery(RuleResponsibilityBo.class);
        Root fromResp = ruleResponsibilitySubquery.from(RuleResponsibilityBo.class);
        ruleResponsibilitySubquery.where(criteriaBuilder.equal(fromResp.get("ruleResponsibilityName"),ruleResponsibilityName));
        ruleResponsibilitySubquery.select(fromResp.get("ruleBaseValuesId"));

        return ruleResponsibilitySubquery;
    }
    private Subquery<RuleResponsibilityBo> getResponsibilitySubQuery(CriteriaQuery<RuleBaseValues> query, Collection<String> kimGroupIds, String principalId, Boolean searchUser, Boolean searchUserInWorkgroups) {

        Collection<String> workgroupIdStrings = new ArrayList<String>();
        for (String workgroupId : kimGroupIds) {
            workgroupIdStrings.add(workgroupId.toString());
        }
        return getResponsibilitySubQuery(query, workgroupIdStrings,principalId,new ArrayList<String>(), searchUser, searchUserInWorkgroups);
    }

    private Subquery<RuleResponsibilityBo> getResponsibilitySubQuery(CriteriaQuery<RuleBaseValues> query, Collection<String> workgroupIds, String principalId, Collection actionRequestCodes, Boolean searchUser, Boolean searchUserInWorkgroups) {

        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        Subquery<RuleResponsibilityBo> ruleResponsibilityBoSubquery = query.subquery(RuleResponsibilityBo.class);
        Root fromResp = ruleResponsibilityBoSubquery.from(RuleResponsibilityBo.class);

        List<Predicate> respPredicates = new
                ArrayList<Predicate>();

        List<Predicate> ruleRespNamePredicates = new
                ArrayList<Predicate>();

        List<Predicate> userNamePreds =
                new ArrayList<Predicate>();

        List<Predicate> workgroupPreds =
                new ArrayList<Predicate>();


        if ( (actionRequestCodes != null) && (!actionRequestCodes.isEmpty()) ) {
            Expression<String> exp = fromResp.get("actionRequestedCd");
            Predicate actionRequestPredicate = exp.in(actionRequestCodes);

            respPredicates.add(actionRequestPredicate);
        }

        if (!org.apache.commons.lang.StringUtils.isEmpty(principalId)) {
            // workflow user id exists
            if (searchUser != null && searchUser) {
                // searching user wishes to search for rules specific to user
                userNamePreds.add(criteriaBuilder.like(fromResp.get("ruleResponsibilityName"),principalId));
                userNamePreds.add(criteriaBuilder.equal(fromResp.get("ruleResponsibilityType"), KewApiConstants.RULE_RESPONSIBILITY_WORKFLOW_ID));

                Predicate[] preds = userNamePreds.toArray(new Predicate[userNamePreds.size()]);
                ruleRespNamePredicates.add(criteriaBuilder.and(preds));

            }
            if ( (searchUserInWorkgroups != null && searchUserInWorkgroups) && (workgroupIds != null) && (!workgroupIds.isEmpty()) ) {
                // at least one workgroup id exists and user wishes to search on workgroups
                Predicate groupIdPredicate = getChunkedIn(workgroupIds, criteriaBuilder, fromResp, "ruleResponsibilityName");
                workgroupPreds.add(groupIdPredicate);
                workgroupPreds.add(criteriaBuilder.equal(fromResp.get("ruleResponsibilityType"),
                        KewApiConstants.RULE_RESPONSIBILITY_GROUP_ID));
                Predicate[] preds = workgroupPreds.toArray(new Predicate[workgroupPreds.size()]);
                ruleRespNamePredicates.add(criteriaBuilder.and(preds));
            }
        } else if ( (workgroupIds != null) && (workgroupIds.size() == 1) ) {
            // no user and one workgroup id
            workgroupPreds.add(criteriaBuilder.like(fromResp.get("ruleResponsibilityName"),
                    workgroupIds.iterator().next()));
            workgroupPreds.add(criteriaBuilder.equal(fromResp.get("ruleResponsibilityType"),
                    KewApiConstants.RULE_RESPONSIBILITY_GROUP_ID));
            Predicate[] preds = workgroupPreds.toArray(new Predicate[workgroupPreds.size()]);
            ruleRespNamePredicates.add(criteriaBuilder.and(preds));

        } else if ( (workgroupIds != null) && (workgroupIds.size() > 1) ) {
            // no user and more than one workgroup id

            Expression<String> exp = fromResp.get("ruleResponsibilityName");
            Predicate groupIdPredicate = exp.in(workgroupIds);
            workgroupPreds.add(criteriaBuilder.equal(fromResp.get("ruleResponsibilityType"),
                    KewApiConstants.RULE_RESPONSIBILITY_GROUP_ID));
            Predicate[] preds = workgroupPreds.toArray(new Predicate[workgroupPreds.size()]);
            ruleRespNamePredicates.add(criteriaBuilder.and(preds));
        }

        if (!ruleRespNamePredicates.isEmpty()) {
            Predicate[] preds = ruleRespNamePredicates.toArray(new Predicate[ruleRespNamePredicates.size()]);
            respPredicates.add(criteriaBuilder.or(preds));
        }

        if (!respPredicates.isEmpty()) {

            Predicate[] preds = respPredicates.toArray(
                    new Predicate[respPredicates.size()]);
            ruleResponsibilityBoSubquery.where(preds);
            ruleResponsibilityBoSubquery.select(fromResp.get("ruleBaseValuesId"));

            return ruleResponsibilityBoSubquery;
        }
        return null;
    }

    private Predicate getChunkedIn(Collection<String> arguments, CriteriaBuilder criteriaBuilder, Root from, String property) {
        final int chunkSize = 500;
        List<List<String>> chunks = Lists.partition(new ArrayList<String>(arguments), chunkSize);
        List<Predicate> predicates = new ArrayList<Predicate>();
        for (List<String> chunk : chunks) {
            Expression<String> exp = from.get(property);
            predicates.add(exp.in(chunk));
        }
        return criteriaBuilder.or((Predicate[]) predicates.toArray(new Predicate[predicates.size()]));
    }

    private Subquery<RuleBaseValues> getRuleBaseValuesSubQuery(String docTypeName, String ruleTemplateId, String ruleDescription, Collection<String> workgroupIds,
            String principalId, Boolean activeInd,
            Map<String, String> extensionValues, Collection actionRequestCodes, CriteriaQuery<RuleDelegationBo> query){
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<RuleBaseValues> criteriaQuery = criteriaBuilder.createQuery(RuleBaseValues.class);
        Subquery<RuleBaseValues> ruleBaseValuesSubquery = query.subquery(RuleBaseValues.class);
        Root root = ruleBaseValuesSubquery.from(RuleBaseValues.class);
        List<Predicate> predicates = getSearchCriteria(root,criteriaQuery,docTypeName, ruleTemplateId, ruleDescription, activeInd, extensionValues);
        Subquery<RuleResponsibilityBo> subquery = getResponsibilitySubQuery(criteriaQuery,workgroupIds, principalId, actionRequestCodes, (principalId != null), ((workgroupIds != null) && !workgroupIds.isEmpty()));
        predicates.add(criteriaBuilder.in(root.get("id")).value(subquery));
        ruleBaseValuesSubquery.where(criteriaBuilder.equal(root.get("delegateRule"),Boolean.TRUE));
        ruleBaseValuesSubquery.select(root.get("id"));

        return ruleBaseValuesSubquery;

    }

    private Subquery<RuleBaseValues> getRuleBaseValuesSubQuery(String docTypeName, String ruleId, String ruleTemplateId, String ruleDescription, String workgroupId, String principalId, Boolean activeInd, Map<String, String> extensionValues, String workflowIdDirective,CriteriaQuery<RuleDelegationBo> query){

        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<RuleBaseValues> criteriaQuery = criteriaBuilder.createQuery(RuleBaseValues.class);
        Subquery<RuleBaseValues> ruleBaseValuesSubquery = query.subquery(RuleBaseValues.class);
        Root fromResp = ruleBaseValuesSubquery.from(RuleBaseValues.class);
        List<Predicate> predicates = getSearchCriteria(fromResp,criteriaQuery,docTypeName, ruleTemplateId, ruleDescription, activeInd, extensionValues);

        if (ruleId != null) {
            predicates.add(criteriaBuilder.equal(fromResp.get("id"),ruleId));
        }

        if (workgroupId != null) {
            predicates.add(criteriaBuilder.in(fromResp.get("id")).value(getResponsibilitySubQuery(workgroupId, criteriaQuery)));
        }
        Collection<String> kimGroupIds = new HashSet<String>();
        Boolean searchUser = Boolean.FALSE;
        Boolean searchUserInWorkgroups = Boolean.FALSE;

        if ("group".equals(workflowIdDirective)) {
            searchUserInWorkgroups = Boolean.TRUE;
        } else if (StringUtils.isBlank(workflowIdDirective)) {
            searchUser = Boolean.TRUE;
            searchUserInWorkgroups = Boolean.TRUE;
        } else {
            searchUser = Boolean.TRUE;
        }
        if (!org.apache.commons.lang.StringUtils.isEmpty(principalId) && searchUserInWorkgroups) {
            Principal principal = null;

            principal = KimApiServiceLocator.getIdentityService().getPrincipal(principalId);

            if (principal == null)
            {
                throw new RiceRuntimeException("Failed to locate user for the given principal id: " + principalId);
            }
            kimGroupIds = KimApiServiceLocator.getGroupService().getGroupIdsByPrincipalId(principalId);
        }
        Subquery<RuleResponsibilityBo> subquery = getResponsibilitySubQuery(criteriaQuery,kimGroupIds, principalId, searchUser, searchUserInWorkgroups);
        if (CollectionUtils.isNotEmpty(kimGroupIds) || StringUtils.isNotBlank(principalId)) {
            predicates.add(criteriaBuilder.in(fromResp.get("id")).value(subquery));
        }

        predicates.add(criteriaBuilder.equal(fromResp.get("delegateRule"),Boolean.TRUE));
        Predicate[] preds = predicates.toArray(new Predicate[predicates.size()]);
        ruleBaseValuesSubquery.where(preds);
        ruleBaseValuesSubquery.select(fromResp.get("id"));

        return ruleBaseValuesSubquery;

    }

    private Subquery<RuleResponsibilityBo> getRuleResponsibilitySubQuery(Long ruleBaseValuesId, CriteriaQuery<RuleDelegationBo> query){

        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        Subquery<RuleResponsibilityBo> ruleResponsibilityBoSubquery = query.subquery(RuleResponsibilityBo.class);
        Root fromResp = ruleResponsibilityBoSubquery.from(RuleResponsibilityBo.class);
        ruleResponsibilityBoSubquery.where(criteriaBuilder.equal(fromResp.get("ruleBaseValuesId"),ruleBaseValuesId));
        ruleResponsibilityBoSubquery.select(fromResp.get("responsibilityId"));

        return ruleResponsibilityBoSubquery;
    }


    private List<Predicate> getSearchCriteria(Root<RuleBaseValues> root,CriteriaQuery<RuleBaseValues> query,
            String docTypeName, String ruleTemplateId,
            String ruleDescription, Boolean activeInd, Map<String,String> extensionValues) {
        List<Predicate> predicates = new ArrayList<Predicate>();
        CriteriaBuilder criteribaBuilder = getEntityManager().getCriteriaBuilder();

        predicates.add(criteribaBuilder.equal(root.get("currentInd"),Boolean.TRUE));
        predicates.add(criteribaBuilder.equal(root.get("templateRuleInd"), Boolean.FALSE));
        if (activeInd != null) {
            predicates.add(criteribaBuilder.equal(root.get("active"),activeInd));
        }
        if (docTypeName != null) {
            predicates.add(criteribaBuilder.like(criteribaBuilder.upper(root.<String>get("docTypeName")), docTypeName.toUpperCase()));
        }
        if (ruleDescription != null && !ruleDescription.trim().equals("")) {
            predicates.add(criteribaBuilder.like(criteribaBuilder.upper(root.<String>get("description")),ruleDescription.toUpperCase()));
        }
        if (ruleTemplateId != null) {
            predicates.add(criteribaBuilder.equal(root.get("ruleTemplateId"),ruleTemplateId));
        }

        if (extensionValues != null && !extensionValues.isEmpty()) {
            for (Map.Entry<String,String> entry : extensionValues.entrySet()) {
                if (!org.apache.commons.lang.StringUtils.isEmpty(entry.getValue())) {
                    Subquery ruleExtSubQuery = query.subquery(RuleExtensionBo.class);
                    Root<RuleExtensionBo> ruleExtRoot = ruleExtSubQuery.from(RuleExtensionBo.class);
                    Predicate predAnd = criteribaBuilder.and(
                            criteribaBuilder.equal(ruleExtRoot.get("extensionValues").get("key"),entry.getKey()),
                            criteribaBuilder.like(ruleExtRoot.get("extensionValues").<String>get("value"),
                                    ("%" + (String) entry.getValue() + "%").toUpperCase()));
                    ruleExtSubQuery.where(predAnd);
                    ruleExtSubQuery.select(ruleExtRoot.get("ruleBaseValuesId"));

                    predicates.add(criteribaBuilder.in(root.get("id")).value(ruleExtSubQuery));
                }
            }
        }
        return predicates;
    }

    /**
     * This overridden method ...
     *
     * @see org.kuali.rice.kew.rule.dao.RuleDelegationDAO#search(java.lang.String, java.lang.Long, java.lang.Long, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.util.Map, java.lang.String)
     */
    @Override
    public List<RuleDelegationBo> search(String parentRuleBaseVaueId, String parentResponsibilityId, String docTypeName, String ruleId,
            String ruleTemplateId, String ruleDescription, String workgroupId,
            String principalId, String delegationType, Boolean activeInd,
            Map extensionValues, String workflowIdDirective) {
        // TODO jjhanso - THIS METHOD NEEDS JAVADOCS
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<RuleDelegationBo> criteriaQuery = criteriaBuilder.createQuery(RuleDelegationBo.class);
        Root<RuleDelegationBo> root = criteriaQuery.from(RuleDelegationBo.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (StringUtils.isNotBlank(delegationType) && !delegationType.equals(KewApiConstants.DELEGATION_BOTH)) {
            predicates.add(criteriaBuilder.equal(root.get("delegationTypeCode"), delegationType));
        }
        if (StringUtils.isNotBlank(parentResponsibilityId) && StringUtils.isNumeric(parentResponsibilityId)) {
            predicates.add(criteriaBuilder.equal(root.get("responsibilityId"),parentResponsibilityId));
        }

        if (StringUtils.isNotBlank(parentRuleBaseVaueId) && StringUtils.isNumeric(parentRuleBaseVaueId)) {
            predicates.add(criteriaBuilder.in(root.get("responsibilityId")).value(getRuleResponsibilitySubQuery(new Long(parentRuleBaseVaueId),criteriaQuery)));
        }
        Subquery<RuleBaseValues> ruleBaseValuesSubQuery = getRuleBaseValuesSubQuery(docTypeName, ruleId, ruleTemplateId, ruleDescription, workgroupId,principalId, activeInd,extensionValues, workflowIdDirective,criteriaQuery);
        if(ruleBaseValuesSubQuery != null){
            predicates.add(criteriaBuilder.in(root.get("delegateRuleId")).value(ruleBaseValuesSubQuery));
        }
        criteriaQuery.distinct(true);
        Predicate[] preds = predicates.toArray(
                new Predicate[predicates.size()]);
        criteriaQuery.where(preds);
        TypedQuery<RuleDelegationBo> typedQuery = getEntityManager().createQuery(criteriaQuery);
        typedQuery.setMaxResults(KewApiConstants.DELEGATE_RULE_LOOKUP_MAX_ROWS_RETURNED);

        return typedQuery.getResultList();
    }

    /**
     * This overridden method ...
     *
     * @see org.kuali.rice.kew.rule.dao.RuleDelegationDAO#search(java.lang.String, java.lang.Long, java.lang.String, java.util.Collection, java.lang.String, java.lang.String, java.lang.Boolean, java.util.Map, java.util.Collection)
     */
    public List<RuleDelegationBo> search(String parentRuleBaseVaueId, String parentResponsibilityId, String docTypeName, String ruleTemplateId,
            String ruleDescription, Collection<String> workgroupIds,
            String principalId, String delegationType, Boolean activeInd,
            Map extensionValues, Collection actionRequestCodes) {
        // TODO jjhanso - THIS METHOD NEEDS JAVADOCS
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<RuleDelegationBo> criteriaQuery = criteriaBuilder.createQuery(RuleDelegationBo.class);
        Root<RuleDelegationBo> root = criteriaQuery.from(RuleDelegationBo.class);
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (StringUtils.isNotBlank(delegationType) && !delegationType.equals(KewApiConstants.DELEGATION_BOTH)) {
            predicates.add(criteriaBuilder.equal(root.get("delegationTypeCode"), delegationType));
        }
        if (StringUtils.isNotBlank(parentResponsibilityId) && StringUtils.isNumeric(parentResponsibilityId)) {
            predicates.add(criteriaBuilder.equal(root.get("responsibilityId"),parentResponsibilityId));
        }

        if (StringUtils.isNotBlank(parentRuleBaseVaueId) && StringUtils.isNumeric(parentRuleBaseVaueId)) {
            predicates.add(criteriaBuilder.in(root.get("responsibilityId")).value(getRuleResponsibilitySubQuery(new Long(parentRuleBaseVaueId),criteriaQuery)));
        }
        Subquery<RuleBaseValues> ruleBaseValuesSubQuery = getRuleBaseValuesSubQuery(docTypeName, ruleTemplateId, ruleDescription, workgroupIds,principalId, activeInd,extensionValues,actionRequestCodes,criteriaQuery);
        if(ruleBaseValuesSubQuery != null){
            predicates.add(criteriaBuilder.in(root.get("delegateRuleId")).value(ruleBaseValuesSubQuery));
        }
        criteriaQuery.distinct(true);
        TypedQuery<RuleDelegationBo> typedQuery = getEntityManager().createQuery(criteriaQuery);
        typedQuery.setMaxResults(KewApiConstants.DELEGATE_RULE_LOOKUP_MAX_ROWS_RETURNED);

        return typedQuery.getResultList();
    }

    public DataObjectService getDataObjectService() {
        return dataObjectService;
    }

    @Required
    public void setDataObjectService(DataObjectService dataObjectService) {
        this.dataObjectService = dataObjectService;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


}
