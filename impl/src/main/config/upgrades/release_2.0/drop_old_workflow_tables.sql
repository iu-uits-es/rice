-- 
-- Copyright 2008-2009 The Kuali Foundation
-- 
-- Licensed under the Educational Community License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
-- http://www.opensource.org/licenses/ecl2.php
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- 
-- Drop pre-1.6-release Tables --;

-- Suppress error messages when dropping non-existing tables --;
WHENEVER SQLERROR CONTINUE;

DROP TABLE ACTION_ITEM CASCADE CONSTRAINTS;
DROP TABLE ACTION_REQUEST CASCADE CONSTRAINTS;
DROP TABLE ACTION_TAKEN CASCADE CONSTRAINTS;
DROP TABLE BASE_ORG_RVW_HIERARCHY CASCADE CONSTRAINTS;
DROP TABLE DOCUMENT_CHANGE_HISTORY CASCADE CONSTRAINTS;
DROP TABLE document_route_header CASCADE CONSTRAINTS;
drop table document_type_t cascade constraints;
DROP TABLE DOC_TYPE_GRP_T CASCADE CONSTRAINTS;
DROP TABLE doc_type_grp CASCADE CONSTRAINTS;
DROP TABLE DOC_TYPE_RTE_LVL_T CASCADE CONSTRAINTS;
DROP TABLE RTE_LVL_RTE_TYPE_T CASCADE CONSTRAINTS;
DROP TABLE DOC_TYPE_POLICY_RELATION_T CASCADE CONSTRAINTS;
drop table DOCUMENT_VALUE_INDEX cascade constraints;
drop table FISCAL_UPAA_DELEGATION cascade constraints;
DROP TABLE HRMS_ORG_RVW_HIERARCHY CASCADE CONSTRAINTS;
drop table NON_PRIMARY_DELEGATION cascade constraints;
drop table org_responsibility_id cascade constraints;
DROP TABLE ROUTE_QUEUE CASCADE CONSTRAINTS;
drop table route_type cascade constraints;
drop table workgroup cascade constraints;
DROP TABLE WORKGROUP_MEMBER CASCADE CONSTRAINTS;
drop table FISCAL_OFFICER_UPAA_DELEGATION cascade constraints;
drop table MY_USR_OPTN_T cascade constraints;
DROP TABLE DOCUMENT_POLICY_T CASCADE CONSTRAINTS;
DROP TABLE document_type CASCADE CONSTRAINTS;
drop table DOC_TYPE_MONITOR cascade constraints;
drop table MOCK_RT_MDL_RID cascade constraints;
DROP TABLE ROUTE_TEMPLATE_ENTRY CASCADE CONSTRAINTS;
drop table SUDS_EJB_SERVICE CASCADE CONSTRAINTS;
drop table SUDS_EJB_SERVICE_PARAM CASCADE CONSTRAINTS;
DROP TABLE EN_BASE_ORG_RVW_HIER_T CASCADE CONSTRAINTS;
DROP TABLE EN_DOC_VAL_INDX_T CASCADE CONSTRAINTS;
DROP TABLE EN_FSCL_UPAA_DLGN_T CASCADE CONSTRAINTS;
DROP TABLE EN_HRMS_ORG_RVW_HIER_T CASCADE CONSTRAINTS;
DROP TABLE EN_NON_PRM_DLGN_T CASCADE CONSTRAINTS;
DROP SEQUENCE SEQ_DOCUMENT_TYPE;
DROP SEQUENCE SEQ_DOC_TYPE_GRP;
DROP SEQUENCE SEQ_DOC_TYPE_MONITOR;
DROP SEQUENCE SEQ_HRMS_ORG_RVW_HIERARCHY;
DROP SEQUENCE SEQ_NON_PRIMARY_DELEGATION;
DROP SEQUENCE SEQ_WORKGROUP;

-- Reinstate error messages and exit --;
WHENEVER SQLERROR EXIT SQL.SQLCODE;
