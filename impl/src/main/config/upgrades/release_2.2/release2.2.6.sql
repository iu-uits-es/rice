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
ALTER TABLE EN_DOC_TYP_T ADD DOC_TYP_SECURITY_XML LONG;

CREATE INDEX EN_DOC_HDR_EXT_TI3 ON EN_DOC_HDR_EXT_T(DOC_HDR_ID,DOC_HDR_EXT_VAL_KEY,DOC_HDR_EXT_VAL);
