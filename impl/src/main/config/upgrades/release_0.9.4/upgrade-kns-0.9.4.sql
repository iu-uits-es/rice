CREATE TABLE KR_KNS_SESN_DOC_T (
        SESSION_ID                     VARCHAR2(40) CONSTRAINT KR_KNS_SESN_DOC_TN1 NOT NULL,
        FDOC_NBR                       VARCHAR2(14) CONSTRAINT KR_KNS_SESN_DOC_TN2 NOT NULL,
        SERIALIZED_DOC_FRM             BLOB,
        LST_UPDATE_DT                  DATE,
     CONSTRAINT KR_KNS_SESN_DOC_TP1 PRIMARY KEY (SESSION_ID, FDOC_NBR) 
)

/

CREATE INDEX KR_KNS_SESN_DOC_TI1 ON KR_KNS_SESN_DOC_T (
        LST_UPDATE_DT )
/
