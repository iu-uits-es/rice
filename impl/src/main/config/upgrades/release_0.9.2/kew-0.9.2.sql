-- 
-- Copyright 2007-2009 The Kuali Foundation
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
CREATE TABLE EN_RTE_NODE_CFG_PARM_T (
    RTE_NODE_CFG_PARM_ID    NUMBER(19) NOT NULL,
    RTE_NODE_CFG_PARM_ND    NUMBER(19) NOT NULL,
    RTE_NODE_CFG_PARM_KEY   VARCHAR2(255) NOT NULL,
    RTE_NODE_CFG_PARM_VAL   VARCHAR2(4000),
    CONSTRAINT EN_RTE_NODE_CFG_PARM_T_PK PRIMARY KEY (RTE_NODE_CFG_PARM_ID) USING INDEX
)
/

-- Table for RuleExpressionDef; child of EN_RULE_BASE_VAL_T/RuleBaseValue
CREATE TABLE EN_RULE_EXPR_T (
    RULE_EXPR_ID    NUMBER(19) NOT NULL,
    RULE_EXPR_TYP   VARCHAR(256) NOT NULL,
    RULE_EXPR       VARCHAR2(4000),
    CONSTRAINT EN_RULE_EXPR_T_PK PRIMARY KEY (RULE_EXPR_ID) USING INDEX
)
/

CREATE TABLE EN_OUT_BOX_ITM_T (
	ACTN_ITM_ID 		    NUMBER(14) NOT NULL,
	ACTN_ITM_PRSN_EN_ID     VARCHAR2(30) NOT NULL,
	ACTN_ITM_ASND_DT        DATE NOT NULL,
	ACTN_ITM_RQST_CD        CHAR(1) NOT NULL,
	ACTN_RQST_ID            NUMBER(14) NOT NULL,
	DOC_HDR_ID              NUMBER(14) NOT NULL,
	WRKGRP_ID               NUMBER(14) NULL,
	ROLE_NM 				VARCHAR2(2000) NULL,
	ACTN_ITM_DLGN_PRSN_EN_ID VARCHAR2(30) NULL,
    ACTN_ITM_DLGN_WRKGRP_ID NUMBER(14) NULL,
	DOC_TTL			        VARCHAR2(255) NULL,
	DOC_TYP_LBL_TXT         VARCHAR2(255) NOT NULL,
	DOC_TYP_HDLR_URL_ADDR   VARCHAR2(255) NOT NULL,
	DOC_TYP_NM		        VARCHAR2(255) NOT NULL,
	ACTN_ITM_RESP_ID        NUMBER(14) NOT NULL,
	DLGN_TYP				VARCHAR2(1) NULL,
	DB_LOCK_VER_NBR	        NUMBER(8) DEFAULT 0,
	CONSTRAINT EN_OUT_BOX_ITM_T_PK PRIMARY KEY (ACTN_ITM_ID)  USING INDEX
)
/

CREATE INDEX EN_OUT_BOX_ITM_T1
 ON EN_OUT_BOX_ITM_T (ACTN_ITM_PRSN_EN_ID)
/
CREATE INDEX EN_OUT_BOX_ITM_TI2
 ON EN_OUT_BOX_ITM_T (DOC_HDR_ID)
/
CREATE INDEX EN_OUT_BOX_ITM_TI3
 ON EN_OUT_BOX_ITM_T (ACTN_RQST_ID)
/
CREATE INDEX EN_OUT_BOX_ITM_TI4
	ON EN_OUT_BOX_ITM_T (ACTN_ITM_DLGN_PRSN_EN_ID, ACTN_ITM_DLGN_WRKGRP_ID, ACTN_ITM_PRSN_EN_ID, DLGN_TYP)
/

CREATE SEQUENCE SEQ_OUT_BOX_ITM INCREMENT BY 1 START WITH 10000
/

CREATE SEQUENCE SEQ_RTE_NODE_CFG_PARM INCREMENT BY 1 START WITH 2000
/

CREATE SEQUENCE SEQ_RULE_EXPR INCREMENT BY 1 START WITH 2000
/

ALTER TABLE EN_RTE_NODE_CFG_PARM_T ADD CONSTRAINT EN_RTE_NODE_CFG_PARM_TR1 FOREIGN KEY (RTE_NODE_CFG_PARM_ND) REFERENCES EN_RTE_NODE_T (RTE_NODE_ID)
/

ALTER TABLE EN_RULE_BASE_VAL_T ADD RULE_EXPR_ID NUMBER(19) NULL 
/
ALTER TABLE EN_RULE_BASE_VAL_T MODIFY (RULE_TMPL_ID NULL) 
/

ALTER TABLE EN_RULE_BASE_VAL_T ADD CONSTRAINT EN_RULE_BASE_VAL_TR1 FOREIGN KEY (RULE_EXPR_ID) REFERENCES EN_RULE_EXPR_T (RULE_EXPR_ID)
/


