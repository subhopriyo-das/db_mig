with BATCHID as
 ((SELECT BATCH_ID
     FROM MGIAU_BATCH_CNTL_FILE_INFO
    WHERE INTFC_SYS_ID = 37
      AND INTFC_NAME = 'INDONESIA LOAD'
      AND BATCH_STAT_CODE = 'SUC'
      AND PRCS_DATE IN (SELECT MAX(PRCS_DATE) PRCS_DATE
                          FROM MGIAU_BATCH_CNTL_FILE_INFO
                         WHERE INTFC_SYS_ID = 37
                           AND INTFC_NAME = 'INDONESIA LOAD'
                           AND BATCH_STAT_CODE = 'SUC'))),
BADT as
 (select BADT.*
    from CX_BADT_POE_TRAN_EVNT_SF BADT, BATCHID
   where BADT.BATCH_ID = BATCHID.BATCH_ID),
BANK as
 (select distinct cpd.CUST_RCV_NBR, lt.locale_text, cpd.attr_val
    from cx_ds_transaction_det    dt,
         CX_DS_CUST_PRFL_DATA_DET cpd,
         cx_tran_attr_list_ref    tal,
         cx_locale_text_ref       lt
   where /*cpd.CUST_RCV_NBR = 'MG72097137'
                               and */
   dt.DLVR_OPTN_ID = '10'
and dt.CUST_RCV_NBR = cpd.CUST_RCV_NBR
and dt.RCV_CUST_PRFL_VER_NBR = cpd.CUST_PRFL_VER_NBR
and cpd.ATTR_ID in (select ta.ATTR_ID
                     from cx_tran_attr_ref ta
                    where ta.TAG_ID in ('1025', '1026'))
and cpd.ATTR_ID = tal.ATTR_ID
and cpd.ATTR_VAL = tal.ATTR_VAL
and cpd.AGENT_TRAN_CNFG_ID = tal.AGENT_TRAN_CNFG_ID
and tal.ATTR_VAL_TEXT_ID = lt.TEXT_ID
and lt.LOCALE_ID = '1'),
PEP as
 (select /*+PARALLEL(4)*/
   *
    from CX_C360_LAST_CNSMR_INFO_FS a
   where (a.ii_ent_id, a.md_update_date) in
         (select b.ii_ent_id, max(b.md_update_date)
            from CX_C360_LAST_CNSMR_INFO_FS b
           where b.ii_ent_id = a.ii_ent_id
           group by b.ii_ent_id))
SELECT distinct case
                  when DFACT.INITG_ISO_CNTRY_CODE = 'ID' AND
                       DFACT.EVNT_ISO_CNTRY_CODE <> 'ID' then
                   'TKLOP'
                  when DFACT.INITG_ISO_CNTRY_CODE <> 'ID' AND
                       DFACT.EVNT_ISO_CNTRY_CODE = 'ID' then
                   'TKLIP'
                end as report_code,
                DFACT.Src_Tran_Ref_Id transaction_number,
                (DFACT.Src_Tran_Datetime + 13 / 24) date_transaction, --namita confirmed (CST + 13hrs)
                case
                  when DFACT.INITG_ISO_CNTRY_CODE = 'ID' AND
                       DFACT.EVNT_ISO_CNTRY_CODE <> 'ID' then
                   LTRIM(RTRIM(NVL((CASE
                                     WHEN DFACT.INITG_EVNT_ISO_CRNCY_CODE = 'IDR' THEN
                                      replace(to_char(ABS(DFACT.INITG_FACE_TRAN_AMT),
                                                      '999999999999999999.99'),
                                              ',',
                                              ',')
                                     ELSE
                                      replace(to_char(abs((DFACT.INITG_FACE_TRAN_AMT *
                                                          (SELECT FX_RATE.MID_RATE
                                                              FROM CXV_FX_RATE_VER_DIM FX_VER,
                                                                   CXV_FX_RATE_DFACT   FX_RATE
                                                             WHERE DFACT.INITG_EVNT_TRAN_FX_VER_ID =
                                                                   FX_VER.VER_VERSION_ID
                                                               AND FX_VER.CX_FX_RATE_VER_DKEY =
                                                                   FX_RATE.CX_FX_RATE_VER_DKEY
                                                               AND FX_RATE.TO_ISO_CRNCY_CODE =
                                                                   'IDR'
                                                               AND FX_RATE.FROM_ISO_CRNCY_CODE =
                                                                   DFACT.INITG_EVNT_ISO_CRNCY_CODE
                                                               AND ROWNUM = 1))),
                                                      '999999999999999999.99'),
                                              ',',
                                              ',')
                                   END),
                                   ' ')))
                  when DFACT.INITG_ISO_CNTRY_CODE <> 'ID' AND
                       DFACT.EVNT_ISO_CNTRY_CODE = 'ID' then
                   LTRIM(RTRIM(NVL((CASE
                                     WHEN DFACT.EVNT_ISO_CRNCY_CODE = 'IDR' THEN
                                      replace(to_char(ABS(DFACT.EVNT_FACE_TRAN_AMT),
                                                      '999999999999999999.99'),
                                              ',',
                                              ',')
                                     ELSE
                                      replace(to_char(abs((DFACT.EVNT_FACE_TRAN_AMT *
                                                          (SELECT FX_RATE.MID_RATE
                                                              FROM CXV_FX_RATE_VER_DIM FX_VER,
                                                                   CXV_FX_RATE_DFACT   FX_RATE
                                                             WHERE DFACT.EVNT_FX_VER_ID =
                                                                   FX_VER.VER_VERSION_ID
                                                               AND FX_VER.CX_FX_RATE_VER_DKEY =
                                                                   FX_RATE.CX_FX_RATE_VER_DKEY
                                                               AND FX_RATE.TO_ISO_CRNCY_CODE =
                                                                   'IDR'
                                                               AND FX_RATE.FROM_ISO_CRNCY_CODE =
                                                                   DFACT.EVNT_ISO_CRNCY_CODE
                                                               AND ROWNUM = 1))),
                                                      '999999999999999999.99'),
                                              ',',
                                              ',')
                                   END),
                                   ' ')))
                end as amount_local,
                nvl(DFACT.INITG_ISO_CNTRY_CODE, '-') SEND_COUNTRY,
                DFACT.INITG_EVNT_ISO_CRNCY_CODE SEND_TRANS_CRNCY,
                abs(DFACT.Initg_Face_Tran_Amt) SEND_FACE_TRAN_AMT,
                ltrim(rtrim(nvl(SEND_CNSMR.PRSN_FRST_NAME, ' '))) sender_fst_name,
                ltrim(rtrim(nvl(SEND_CNSMR.PRSN_LAST_NAME, ' '))) sender_lst_name,
                ltrim(rtrim(nvl(SEND_CNSMR.PRSN_FRST_NAME, ' ') || ' ' ||
                            nvl(SEND_CNSMR.PRSN_LAST_NAME, ' '))) SENDER_FULL_NAME,
                nvl(SEND_CNSMR.PRSN_BRTH_DATE,
                    to_date('01/01/1900', 'MM/DD/YYYY')) SENDER_DOB,
                nvl(send_cnsmr.PRSN_BRTH_CITY_NAME, '-') SENDER_PLACE_OF_BIRTH,
                /*(select cntry_name
                 from (select a.cntry_name
                         from cxv_country_master_dim a
                        where a.iso_cntry_abbr_code =
                              send_cnsmr.PRSN_BRTH_ISO_CNTRY_CODE)
                where rownum = 1)*/
                nvl(send_cnsmr.PRSN_BRTH_ISO_CNTRY_CODE, '-') SENDER_NATIONALITY,
                /*(select cntry_name
                 from (select a.cntry_name
                         from cxv_country_master_dim a
                        where a.iso_cntry_abbr_code =
                              send_cnsmr.CNSMR_ISO_CNTRY_CODE)
                where rownum = 1)*/
                nvl(send_cnsmr.CNSMR_ISO_CNTRY_CODE, '-') sender_residence,
                send_cnsmr.CNSMR_PH_NBR sender_phone,
                send_cnsmr.CNSMR_LINE1_ADDR send_CNSMR_LINE1_ADDR,
                send_cnsmr.CNSMR_CITY_NAME send_cnsmr_city,
                send_cnsmr.CNSMR_POSTAL_CODE send_cnsmr_zip,
                /*(select cntry_name
                 from (select a.cntry_name
                         from cxv_country_master_dim a
                        where a.iso_cntry_abbr_code =
                              send_cnsmr.CNSMR_ISO_CNTRY_CODE)
                where rownum = 1)*/
                nvl(send_cnsmr.CNSMR_ISO_CNTRY_CODE, '-') send_cnsmr_country,
                send_cnsmr.CNSMR_STATEPROVINCE_NAME send_cnsmr_state,
                nvl(send_cnsmr.PRSN_OCCUPN_TEXT, '-') sender_occupation,
                nvl((select send_photo_id_type
                      from (select case
                                     when b.id_type_desc = 'Passport' then
                                      'PAS'
                                     when b.id_type_desc = 'Drivers License' then
                                      'SIM'
                                     when b.id_type_desc = 'Government ID' then
                                      'KTP'
                                     when b.id_type_desc = 'International ID' then
                                      'KTP'
                                     when b.id_type_desc =
                                          'Social Security Nbr' then
                                      'KTP'
                                     else
                                      NULL
                                   end as send_photo_id_type
                              from cx_identification_type_ref b
                             where b.id_cat_code = 2 --means PHOTO
                               and b.id_type_abbr =
                                   send_cnsmr.CNSMR_PHT_ID_TYPE_CODE)
                     where rownum = 1),
                    'SUKET') send_photo_id_type,
                send_cnsmr.CNSMR_PHT_ID send_cnsmr_photo_id_number,
                to_date(to_char(to_date(send_cnsmr.CNSMR_PHT_ID_ISSU_DATE_TEXT,
                                        'MM/DD/YYYY HH24:MI:SS'),
                                'MM/DD/YYYY'),
                        'MM/DD/YYYY') sendcnsmrphoto_id_issue_date,
                nvl(send_cnsmr.CNSMR_PHT_ID_ISSU_CNTRY_CODE, '-') sendcnsmrphoto_id_issue_cntry,
                nvl(dts.src_of_fund_text, '-') as t_from_my_clnt_src_offund_text,
                /*(select cntry_name
                 from (select a.cntry_name
                         from cxv_country_master_dim a
                        where a.iso_cntry_abbr_code =
                              SND_PTY.Iso_Cntry_Code)
                where rownum = 1)*/
                nvl(SND_PTY.Iso_Cntry_Code, '-') send_agent_country,
                dfact.intnd_rcv_dlvr_optn_id,
                case
                  when dfact.intnd_rcv_dlvr_optn_id = 10 then
                   'REK'
                  else
                   'UT'
                end as t_to_my_client_to_funds_code,
                nvl(DFACT.Evnt_Iso_Cntry_Code, '-') RCV_COUNTRY,
                DFACT.Evnt_Iso_Crncy_Code RCV_TRANS_CRNCY,
                abs(DFACT.Evnt_Face_Tran_Amt) RCV_FACE_TRAN_AMT,
                --RCV_PTY.Pty_Id,
                /*(select customer_number
                 from (select distinct T7.customer_number
                         from CUSTOMER_SITE_DIM T7, CUSTOMER_ENTITY_DIM T8
                        where T8.PARTY_ID = RCV_PTY.PTY_ID
                          and T7.CUST_ACCOUNT_ID = T8.CUST_ACCOUNT_ID)
                where rownum = 1) account_number, */ ---
                ltrim(rtrim(nvl(RCV_CNSMR.PRSN_FRST_NAME, ' '))) RCVR_FST_NAME,
                ltrim(rtrim(nvl(RCV_CNSMR.PRSN_LAST_NAME, ' '))) RCVR_LST_NAME,
                ltrim(rtrim(nvl(RCV_CNSMR.PRSN_FRST_NAME, ' ') || ' ' ||
                            nvl(RCV_CNSMR.PRSN_LAST_NAME, ' '))) RCVR_FULL_NAME,
                nvl(RCV_CNSMR.PRSN_BRTH_DATE,
                    to_date('01/01/1900', 'MM/DD/YYYY')) RCVR_DOB,
                nvl(RCV_CNSMR.PRSN_BRTH_CITY_NAME, '-') RCVR_PLACE_OF_BIRTH,
                nvl( /*(select cntry_name
                                                                                                                                                                                                                                                                                                                                                                          from (select a.cntry_name
                                                                                                                                                                                                                                                                                                                                                                                  from cxv_country_master_dim a
                                                                                                                                                                                                                                                                                                                                                                                 where a.iso_cntry_abbr_code =
                                                                                                                                                                                                                                                                                                                                                                                       RCV_CNSMR.PRSN_BRTH_ISO_CNTRY_CODE)
                                                                                                                                                                                                                                                                                                                                                                         where rownum = 1)*/RCV_CNSMR.PRSN_BRTH_ISO_CNTRY_CODE,
                    '-') RCVR_NATIONALITY,
                /*(select cntry_name
                 from (select a.cntry_name
                         from cxv_country_master_dim a
                        where a.iso_cntry_abbr_code =
                              RCV_CNSMR.CNSMR_ISO_CNTRY_CODE)
                where rownum = 1)*/
                nvl(RCV_CNSMR.CNSMR_ISO_CNTRY_CODE, '-') rcvr_residence,
                RCV_CNSMR.CNSMR_PH_NBR rcvr_phone,
                RCV_CNSMR.CNSMR_LINE1_ADDR rcvr_CNSMR_LINE1_ADDR,
                RCV_CNSMR.CNSMR_CITY_NAME rcvr_cnsmr_city,
                RCV_CNSMR.CNSMR_POSTAL_CODE rcvr_cnsmr_zip,
                /*(select cntry_name
                 from (select a.cntry_name
                         from cxv_country_master_dim a
                        where a.iso_cntry_abbr_code =
                              RCV_CNSMR.CNSMR_ISO_CNTRY_CODE)
                where rownum = 1)*/
                nvl(RCV_CNSMR.CNSMR_ISO_CNTRY_CODE, '-') rcvr_cnsmr_country,
                RCV_CNSMR.CNSMR_STATEPROVINCE_NAME rcvr_cnsmr_state,
                nvl((select rcv_photo_id_type
                      from (select case
                                     when b.id_type_desc = 'Passport' then
                                      'PAS'
                                     when b.id_type_desc = 'Drivers License' then
                                      'SIM'
                                     when b.id_type_desc = 'Government ID' then
                                      'KTP'
                                     when b.id_type_desc = 'International ID' then
                                      'KTP'
                                     when b.id_type_desc =
                                          'Social Security Nbr' then
                                      'KTP'
                                     else
                                      NULL
                                   end as rcv_photo_id_type
                              from cx_identification_type_ref b
                             where b.id_cat_code = 2 --means PHOTO
                               and b.id_type_abbr =
                                   RCV_CNSMR.CNSMR_PHT_ID_TYPE_CODE)
                     where rownum = 1),
                    'SUKET') rcv_photo_id_type,
                RCV_CNSMR.CNSMR_PHT_ID rcvr_cnsmr_photo_id_number,
                to_date(to_char(to_date(RCV_CNSMR.CNSMR_PHT_ID_ISSU_DATE_TEXT,
                                        'MM/DD/YYYY HH24:MI:SS'),
                                'MM/DD/YYYY'),
                        'MM/DD/YYYY') rcvrcnsmrphoto_id_issue_date,
                nvl(RCV_CNSMR.CNSMR_PHT_ID_ISSU_CNTRY_CODE, '-') rcvrcnsmrphoto_id_issue_cntry,
                /*(select cntry_name
                 from (select a.cntry_name
                         from cxv_country_master_dim a
                        where a.iso_cntry_abbr_code =
                              RCV_PTY.Iso_Cntry_Code)
                where rownum = 1)*/
                nvl(RCV_PTY.Iso_Cntry_Code, '-') rcvr_agent_country,
                nvl(SND_PTY.Iso_Cntry_Code, '-') send_agent_country_code,
                nvl(RCV_PTY.Iso_Cntry_Code, '-') rcv_agent_country_code,
                DFACT.INITG_EVNT_ISO_CRNCY_CODE from_frgn_crncy,
                abs(DFACT.Initg_Face_Tran_Amt) from_frgn_amt,
                /*case
                when DFACT.INITG_ISO_CNTRY_CODE <> 'ID' AND
                     DFACT.EVNT_ISO_CNTRY_CODE = 'ID' and
                     DFACT.INITG_EVNT_ISO_CRNCY_CODE <> 'IDR' and
                     DFACT.EVNT_ISO_CRNCY_CODE = 'IDR' then*/
                (SELECT FX_RATE.MID_RATE
                    FROM CXV_FX_RATE_VER_DIM FX_VER,
                         CXV_FX_RATE_DFACT   FX_RATE
                   WHERE DFACT.INITG_EVNT_TRAN_FX_VER_ID =
                         FX_VER.VER_VERSION_ID
                     AND FX_VER.CX_FX_RATE_VER_DKEY =
                         FX_RATE.CX_FX_RATE_VER_DKEY
                     AND FX_RATE.FROM_ISO_CRNCY_CODE =
                         DFACT.INITG_EVNT_ISO_CRNCY_CODE
                     AND FX_RATE.TO_ISO_CRNCY_CODE = 'IDR'
                     AND ROWNUM = 1)
                /*end*/ as from_frgn_fx_rate,
                DFACT.Evnt_Iso_Crncy_Code to_frgn_crncy,
                abs(DFACT.Evnt_Face_Tran_Amt) to_frgn_amt,
                /*case
                when DFACT.INITG_ISO_CNTRY_CODE = 'ID' AND
                     DFACT.EVNT_ISO_CNTRY_CODE <> 'ID' and
                     DFACT.Initg_Evnt_Iso_Crncy_Code = 'IDR' and
                     DFACT.Evnt_Iso_Crncy_Code <> 'IDR' then*/
                (SELECT FX_RATE.MID_RATE
                    FROM CXV_FX_RATE_VER_DIM FX_VER,
                         CXV_FX_RATE_DFACT   FX_RATE
                   WHERE DFACT.EVNT_FX_VER_ID = FX_VER.VER_VERSION_ID
                     AND FX_VER.CX_FX_RATE_VER_DKEY =
                         FX_RATE.CX_FX_RATE_VER_DKEY
                     AND FX_RATE.FROM_ISO_CRNCY_CODE =
                         DFACT.EVNT_ISO_CRNCY_CODE
                     AND FX_RATE.TO_ISO_CRNCY_CODE = 'IDR'
                     AND ROWNUM = 1)
                /*end*/ as to_frgn_fx_rate,
                case
                  when dfact.intnd_rcv_dlvr_optn_id = 10 then
                   'REK'
                  else
                   'UT'
                end as t_to_to_funds_code,
                decode(upper(PEP_SEND.PEP_FLAG), 'Y', 'Y', 'T') as t_from_my_client_tax_reg_nbr, --
                decode(upper(PEP_REC.PEP_FLAG), 'Y', 'Y', 'T') as t_to_my_client_tax_reg_nbr, --
                nvl((case
                      when dfact.intnd_rcv_agent_id = '70641279' then
                       n'CIMB NIAGA'
                      else
                       BANK.locale_text
                    end),
                    '-') as to_instn_name, --
                nvl(NULL, '-') as to_swift_code, --
                --nvl(DTS.INTND_RCV_ACCT_NBR, '-') as to_account, --
                nvl(DTS.Rule_Prcs_Acct_Nbr, '-') as to_account, --
                nvl(RCV_CNSMR.PRSN_OCCUPN_TEXT, '-') rcvr_occupation

  FROM CX_POE_TRAN_EVNT_DFACT DFACT,
       CX_DTS_TRAN_EVNT_DET DTS,
       (select *
          from CXV_POE_TRANEVNT_CNSMR_DFACT TCD
         where ltrim(rtrim(TCD.PRSN_FRST_NAME)) != 'AUTOMATED'
           AND ltrim(rtrim(TCD.PRSN_LAST_NAME)) != 'INTERNAL') SEND_CNSMR,
       (select *
          from CXV_POE_TRANEVNT_CNSMR_DFACT TCD
         where ltrim(rtrim(TCD.PRSN_FRST_NAME)) != 'AUTOMATED'
           AND ltrim(rtrim(TCD.PRSN_LAST_NAME)) != 'INTERNAL') RCV_CNSMR,
       BADT,
       BATCHID,
       (select FACT_KEY,
               CUST_SITE_DLKP,
               LGCY_AGENT_NBR,
               CUST_ACCT_SITE_CX_PTY_DKEY
          from CX_PTY_OSR_DFACT OSR
         where OSR.CAS_SRC_SYS_REF_USG_TEXT = 'TRANSACTING_AGENT'
        /*AND OSR.CAS_SRC_SYS_STAT_CODE = 'A'*/
        ) SND_OSR,
       CX_PTY_DIM SND_PTY,
       (select FACT_KEY,
               CUST_SITE_DLKP,
               LGCY_AGENT_NBR,
               CUST_ACCT_SITE_CX_PTY_DKEY
          from CX_PTY_OSR_DFACT OSR
         where OSR.CAS_SRC_SYS_REF_USG_TEXT = 'TRANSACTING_AGENT'
        /*AND OSR.CAS_SRC_SYS_STAT_CODE = 'A'*/
        ) RCV_OSR,
       CX_PTY_DIM RCV_PTY,
       BANK,
       PEP PEP_SEND,
       PEP PEP_REC
 WHERE DFACT.INITG_CNSMR_ID = SEND_CNSMR.FACT_KEY
   AND DFACT.EVNT_CNSMR_ID = RCV_CNSMR.FACT_KEY
   AND DFACT.EVNT_TRAN_EVNT_CODE IN ('REC', 'RRC', 'RDT', 'RRD')
      --AND DFACT.EVNT_ISO_CNTRY_CODE = 'ID'
   AND DFACT.FACT_KEY = BADT.POE_TRAN_EVNT_ID
   and /*DFACT.FACT_KEY*/
       BADT.POE_TRAN_EVNT_ID = DTS.Detail_Key(+)
   AND DFACT.INITG_CX_PTY_OSR_FACT_FKEY = SND_OSR.FACT_KEY
   AND SND_PTY.CX_PTY_DKEY = SND_OSR.CUST_ACCT_SITE_CX_PTY_DKEY
   AND DFACT.EVNT_CX_PTY_OSR_FACT_FKEY = RCV_OSR.FACT_KEY
   AND RCV_PTY.CX_PTY_DKEY = RCV_OSR.CUST_ACCT_SITE_CX_PTY_DKEY
      --and DFACT.fact_key=3860885228
   AND SEND_CNSMR.cust_rcv_nbr = BANK.CUST_RCV_NBR(+)
   AND ( /*DFACT*/
        BADT.CX_POE_TRAN_DKEY, /*DFACT*/
        BADT.EVNT_ACTG_EVNT_SEQ_NBR) IN
       (SELECT P.CX_POE_TRAN_DKEY, MAX(P.EVNT_ACTG_EVNT_SEQ_NBR)
          FROM CX_POE_TRAN_EVNT_DFACT P
         WHERE P.CX_POE_TRAN_DKEY = /*DFACT*/
               BADT.CX_POE_TRAN_DKEY
         GROUP BY P.CX_POE_TRAN_DKEY)
   and to_char(SEND_CNSMR.ii_entity_id) = PEP_SEND.ii_ent_id(+)
   and to_char(RCV_CNSMR.ii_entity_id) = PEP_REC.ii_ent_id(+)
   and (case
         when DFACT.INITG_ISO_CNTRY_CODE = 'ID' AND
              DFACT.EVNT_ISO_CNTRY_CODE <> 'ID' then
          'TKLOP'
         when DFACT.INITG_ISO_CNTRY_CODE <> 'ID' AND
              DFACT.EVNT_ISO_CNTRY_CODE = 'ID' then
          'TKLIP'
       end) = 'TKLIP' --only receive in indonesia