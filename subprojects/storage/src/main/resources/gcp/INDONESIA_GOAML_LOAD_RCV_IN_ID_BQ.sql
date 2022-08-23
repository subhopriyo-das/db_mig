with 
IC AS 
(select  *  
from country_reporting_data_extracts.cntry_reporting_rpt_prcs_dates
		 where extr_name = 'indonesia_weekly_goaml_receive_xml_extract') ,

BADT AS (
select  
	t1.poe_tran_evnt_id             as poe_tran_evnt_id,
	t1.evnt_tran_evnt_datetime 		as evnt_tran_evnt_datetime,
	t1.evnt_tran_evnt_code         	as evnt_tran_evnt_code,
	t1.poe_tran_id           		as poe_tran_id,
	t1.poe_evnt_actg_evnt_seq_nbr   as poe_evnt_actg_evnt_seq_nbr,
	0                               as indicator
	from compliance_datamart.1137_poe_tran t1, IC
	where evnt_tran_evnt_code in ('REC', 'RDT')
      and tran_evnt_date_pt between ic.rpt_beg_date and ic.rpt_end_date /* '2022-04-23' and '2022-04-29' */
      and initg_iso_cntry_code = 'ID'
      and evnt_iso_cntry_code <> 'ID'
      and initg_face_tran_amt <> 0
      and evnt_face_tran_amt <> 0
 
union all
	select 
		poe_tran_evnt_id,
		evnt_tran_evnt_datetime,
		evnt_tran_evnt_code,
		poe_tran_id,
		poe_evnt_actg_evnt_seq_nbr,
		indicator
		from 
			(select 
				poe_tran_evnt_id,
				evnt_tran_evnt_datetime,
				evnt_tran_evnt_code,
				poe_tran_id,
				poe_evnt_actg_evnt_seq_nbr,
				indicator,
				tran_evnt_date_pt
				from 
					(select
						poe_tran_evnt_id,
						evnt_tran_evnt_datetime,
						evnt_tran_evnt_code,
						poe_tran_id,
						poe_evnt_actg_evnt_seq_nbr,
						min_evnt_actg_evnt_seq_nbr,
						case
							when rownum_evnt_nbr is not null and evnt_tran_evnt_code in ('REC', 'RDT') then
								case
									when first_value(rownum_evnt_nbr) over(partition by poe_tran_id 
										order by poe_evnt_actg_evnt_seq_nbr desc) = first_value(rownum_evnt_nbr)
										over(partition by poe_tran_id order by poe_evnt_actg_evnt_seq_nbr) then 0
									when poe_evnt_actg_evnt_seq_nbr = min_evnt_actg_evnt_seq_nbr then 0
									else 1
								end
							else 1
						end as indicator,
						tran_evnt_date_pt			
					from 
						(select 
							poe_tran_evnt_id,
							evnt_tran_evnt_datetime,
							evnt_tran_evnt_code,
							poe_tran_id,
							poe_evnt_actg_evnt_seq_nbr,
							case
								when evnt_tran_evnt_code in ('REC', 'RDT') then
									row_number() over(partition by poe_tran_id, evnt_tran_evnt_code 
									order by poe_evnt_actg_evnt_seq_nbr)
								else null
							end rownum_evnt_nbr,
							case
								when evnt_tran_evnt_code in ('REC', 'RDT') then
									first_value(poe_evnt_actg_evnt_seq_nbr)
									over(partition by poe_tran_id order by
											poe_evnt_actg_evnt_seq_nbr)
								else null
							end min_evnt_actg_evnt_seq_nbr,
							tran_evnt_date_pt
						
						from compliance_datamart.gda_foundation_funded_tran, ic
						where tran_evnt_date_pt between ic.rpt_beg_date and ic.rpt_end_date
								and evnt_tran_evnt_code in ('REC', 'RDT', 'RRC', 'RRD')
								and evnt_iso_cntry_code = 'ID'
								and initg_iso_cntry_code <> 'ID'
								and initg_face_tran_amt <> 0
								and evnt_face_tran_amt <> 0
								and prod_mstr_id = '1'
								and initg_poe_tran_evnt_date >= ic.rpt_beg_date					
						order by poe_tran_id, poe_evnt_actg_evnt_seq_nbr))

union all
				select 
					poe_tran_evnt_id,
					evnt_tran_evnt_datetime,
					evnt_tran_evnt_code,
					poe_tran_id,
					poe_evnt_actg_evnt_seq_nbr,
					indicator,
					tran_evnt_date_pt
				from 
					(select 
						poe_tran_evnt_id,
						evnt_tran_evnt_datetime,
						evnt_tran_evnt_code,
						poe_tran_id,
						poe_evnt_actg_evnt_seq_nbr,
						min_evnt_actg_evnt_seq_nbr,
						case
							when rownum_evnt_nbr is not null and evnt_tran_evnt_code in ('REC', 'RDT') then
								case
									when first_value(rownum_evnt_nbr) 
											over(partition by poe_tran_id order by poe_evnt_actg_evnt_seq_nbr desc) = coalesce(first_value(rownum_evnt_nbr) 
																						over(partition by poe_tran_id order by poe_evnt_actg_evnt_seq_nbr),1) 
										and tran_evnt_date_pt between IC.rpt_beg_date and  IC.rpt_end_date  
										then 0
									when rownum_evnt_nbr = min_evnt_actg_evnt_seq_nbr 
									then 0
									else 1
								end
								else 1
						end as indicator,
						tran_evnt_date_pt				
					from IC, 
						(select 
							poe_tran_evnt_id,
							evnt_tran_evnt_datetime,
							evnt_tran_evnt_code,
							poe_tran_id,
							poe_evnt_actg_evnt_seq_nbr,
							tran_evnt_date_pt,
							case
								when evnt_tran_evnt_code in ('REC', 'RDT') then
									row_number() over(partition by poe_tran_id,
											evnt_tran_evnt_code order by poe_evnt_actg_evnt_seq_nbr)
								else null
							end rownum_evnt_nbr,
							case
								when evnt_tran_evnt_code in ('REC', 'RDT') then
									first_value(poe_evnt_actg_evnt_seq_nbr) over(partition by 
										poe_tran_id order by poe_evnt_actg_evnt_seq_nbr)
								else null
							end min_evnt_actg_evnt_seq_nbr					 
								
						from 
							(select
								trans.poe_tran_id,
								trans.initg_poe_tran_evnt_date,
								trans.tran_evnt_date_pt,
								trans.evnt_tran_evnt_datetime,
								trans.poe_evnt_actg_evnt_seq_nbr,
								trans.poe_tran_evnt_id,
								trans.evnt_tran_evnt_code,
								row_number() over(partition by trans.poe_tran_id, 
								trans.poe_tran_evnt_id order by trans.poe_evnt_actg_evnt_seq_nbr) as row_num
							from 
								compliance_datamart.gda_foundation_funded_tran trans,
								(select poe_tran_id 
									from 
										compliance_datamart.gda_foundation_funded_tran, ic
									where tran_evnt_date_pt between ic.rpt_beg_date and ic.rpt_end_date
									   and evnt_iso_cntry_code = 'ID'
									   and initg_iso_cntry_code <> 'ID'
									   and initg_face_tran_amt <> 0
									   and evnt_face_tran_amt <> 0
									   and prod_mstr_id = '1'
									   and initg_poe_tran_evnt_date < ic.rpt_beg_date) I
							where I.poe_tran_id = trans.poe_tran_id)
						where row_num = 1						
						order by poe_tran_id, poe_evnt_actg_evnt_seq_nbr)
					where evnt_tran_evnt_code in ('REC', 'RDT', 'RRC', 'RRD')), IC
		where tran_evnt_date_pt between IC.rpt_beg_date and IC.rpt_end_date ) a),
									 
bank as
	(select distinct cpd.cust_rcv_nbr, lt.locale_text, cpd.attr_val  
		from tpe.ds_transaction    dt,
			 tpe.cust_profile_data cpd,
			 mgt.tran_attr_list    tal,
			 mgt.locale_text       lt
		where dt.dlvr_optn_id = 10
			and dt.cust_rcv_nbr = cpd.cust_rcv_nbr
			and dt.rcv_cust_prfl_ver_nbr = cpd.cust_prfl_ver_nbr
			and cpd.attr_id in (select ta.attr_id from mgt.tran_attr ta where ta.tag_id in (1025, 1026))
			and cpd.attr_id = tal.attr_id
			and cpd.attr_val = tal.attr_val
			and cpd.agent_tran_cnfg_id = tal.agent_tran_cnfg_id
			and tal.attr_val_text_id = lt.text_id
			and lt.locale_id = 1)
			
, IDW_TRAN AS
(
  select distinct 
  IDW.tran_evnt_date_pt,
  IDW.poe_tran_evnt_id,
  IDW.EVNT_CNSMR_FEE_TRAN_AMT,
  IDW.EVNT_BASE_CMSN_TRAN_AMT,
  IDW.intnd_rcv_rule_prcs_acct_nbr,
  IDW.initg_src_of_fund_text,
  IDW.intnd_rcv_agent_id 
  from idw.idw_tran_evnt IDW, BADT badt
  where badt.Poe_Tran_Evnt_Id = IDW.poe_tran_evnt_id 
  and cast(badt.evnt_tran_evnt_datetime as date) = IDW.tran_evnt_date_pt
)

, PTY AS
(
select distinct
 agent_id,
 pty_id,
 iso_cntry_code 
from bir.cx_ptnr_osr_d
)

, idw_cnsmr_audit AS 
(
select distinct 
tran_evnt_date_pt,
poe_tran_evnt_id,
brth_city_name,
cnsmr_identifier_hashkey,
TRAN_EVNT_CNSMR_AUDIT_ID
from idw.idw_tran_evnt_cnsmr_audit
)

	, idw_cnsmr_map as 
(
select distinct 
tran_evnt_date_pt,
prm_snd_cnsmr_audit_id,
prm_rcv_cnsmr_audit_id,
poe_tran_evnt_id
from idw.idw_tran_evnt_cnsmr_map
)

, snd_cnsmr_brth_city AS 
(
select distinct 
ca.poe_tran_evnt_id,
ca.brth_city_name,
ca.cnsmr_identifier_hashkey
from idw_cnsmr_map cm, 
idw_cnsmr_audit ca, BADT badt
  where badt.Poe_Tran_Evnt_Id = cm.poe_tran_evnt_id 
  and cast(badt.evnt_tran_evnt_datetime as date) = cm.tran_evnt_date_pt
  and cm.prm_snd_cnsmr_audit_id = ca.TRAN_EVNT_CNSMR_AUDIT_ID
)

, rcv_cnsmr_brth_city as
(
select distinct 
ca.poe_tran_evnt_id,
ca.brth_city_name,
ca.cnsmr_identifier_hashkey
from idw_cnsmr_map cm, 
idw_cnsmr_audit ca, BADT badt
  where badt.Poe_Tran_Evnt_Id = cm.poe_tran_evnt_id 
  and cast(badt.evnt_tran_evnt_datetime as date) = cm.tran_evnt_date_pt
  and cm.prm_rcv_cnsmr_audit_id = ca.TRAN_EVNT_CNSMR_AUDIT_ID
)
	
select distinct 
	case 
		when dfact.initg_iso_cntry_code = 'ID' and
			 dfact.evnt_iso_cntry_code <> 'ID' then 'TKLOP'
		when dfact.initg_iso_cntry_code <> 'ID' and
			 dfact.evnt_iso_cntry_code = 'ID' then 'TKLIP' 
	end as report_code,
	dfact.src_tran_ref_id transaction_number,
	timestamp_add(dfact.src_tran_date, INTERVAL 13 HOUR) date_transaction,
	case
    when dfact.initg_iso_cntry_code = 'ID' and
       dfact.evnt_iso_cntry_code <> 'ID' then
       ltrim(rtrim(coalesce((case 
                  when dfact.initg_evnt_iso_crncy_code = 'IDR' then 
                    replace(FORMAT("%.*f",2,CAST(abs(dfact.initg_face_tran_amt) AS numeric)), ',', ',') 
                  else replace(FORMAT("%.*f",2,CAST(abs((dfact.initg_face_tran_amt * FX_VER_FROM_FRGN.mid)) AS numeric)), ',', ',') 
                end), ' ')))
    when dfact.initg_iso_cntry_code <> 'ID' and
       dfact.evnt_iso_cntry_code = 'ID' then
             ltrim(rtrim(coalesce((case 
                  when dfact.evnt_iso_crncy_code = 'IDR' then
                    replace(FORMAT("%.*f",2,CAST(abs(dfact.evnt_face_tran_amt) AS numeric)), ',', ',') 
                  else replace(FORMAT("%.*f",2,CAST(abs((dfact.evnt_face_tran_amt * FX_VER_TO_FRGN.mid)) AS numeric)), ',', ',') 
                 end), ' ')))
  end as amount_local,
	coalesce(dfact.initg_iso_cntry_code, '-') send_country,
	dfact.initg_evnt_iso_crncy_code send_trans_crncy,
    abs(dfact.initg_face_tran_amt) send_face_tran_amt,
    ltrim(rtrim(coalesce(dfact.sen_prsn_frst_name, ' '))) sender_fst_name,
    ltrim(rtrim(coalesce(dfact.sen_prsn_last_name, ' '))) sender_lst_name,
    ltrim(rtrim(coalesce(dfact.sen_prsn_frst_name, ' ') || ' ' ||
				coalesce(dfact.sen_prsn_last_name, ' '))) sender_full_name,
	coalesce(dfact.sen_cnsmr_brth_date, parse_date('%m/%d/%y','01/01/1900')) sender_dob,
	coalesce(scnsmr.brth_city_name, '-') sender_place_of_birth,             
	coalesce(dfact.sen_brth_cntry_iso_2_code, '-') sender_nationality,
	coalesce(dfact.sen_addr_cntry_iso_2_code, '-') sender_residence,
	dfact.sen_cnsmr_ph_nbr sender_phone,
	dfact.sen_addr_line1_text send_cnsmr_line1_addr,
	dfact.sen_addr_city_name send_cnsmr_city,
	dfact.sen_addr_postal_code send_cnsmr_zip,
	coalesce(dfact.sen_addr_cntry_iso_2_code, '-') send_cnsmr_country,
	dfact.sen_addr_state_name send_cnsmr_state,
	coalesce(dfact.sen_cnsmr_ocupn_text, '-') sender_occupation,
	coalesce(case
					when dfact.sen_cnsmr_id_1_doc_type_desc = 'Passport' OR dfact.sen_cnsmr_id_2_doc_type_desc = 'Passport' then 'PAS' 
					when dfact.sen_cnsmr_id_1_doc_type_desc = 'Drivers License' OR dfact.sen_cnsmr_id_2_doc_type_desc = 'Drivers License' then 'SIM'
					when dfact.sen_cnsmr_id_1_doc_type_desc = 'Government ID' OR dfact.sen_cnsmr_id_2_doc_type_desc = 'Government ID' then 'KTP'
					when dfact.sen_cnsmr_id_1_doc_type_desc = 'International ID' OR dfact.sen_cnsmr_id_2_doc_type_desc = 'International ID' then 'KTP'
					when dfact.sen_cnsmr_id_1_doc_type_desc = 'Social Security Nbr' OR dfact.sen_cnsmr_id_2_doc_type_desc = 'Social Security Nbr' then 'KTP'
					else NULL
				end, 'SUKET') send_photo_id_type, 
	dfact.sen_cnsmr_id_1_nbr_displ_text send_cnsmr_photo_id_number,
	
    dfact.sen_cnsmr_id_1_issu_month||'/'||dfact.sen_cnsmr_id_1_issu_day||'/'||dfact.sen_cnsmr_id_1_issu_year as sendcnsmrphoto_id_issue_year,
	coalesce(dfact.sen_cnsmr_id_1_issu_iso_cntry_code, '-') sendcnsmrphoto_id_issue_cntry,
	coalesce(dts.initg_src_of_fund_text, '-') as t_from_my_clnt_src_offund_text,
	coalesce(snd_pty.iso_cntry_code, '-') send_agent_country,
	dfact.initg_dlvr_optn_id,
	case when dfact.initg_dlvr_optn_id = 10 then 'REK'
	  else 'UT' 
	end as t_to_my_client_to_funds_code,
	coalesce(dfact.evnt_iso_cntry_code, '-') rcv_country,
	dfact.evnt_iso_crncy_code rcv_trans_crncy,
	abs(dfact.evnt_face_tran_amt) rcv_face_tran_amt,
	ltrim(rtrim(coalesce(dfact.rec_prsn_frst_name, ' '))) rcvr_fst_name,
	ltrim(rtrim(coalesce(dfact.rec_prsn_last_name, ' '))) rcvr_lst_name,
	ltrim(rtrim(coalesce(dfact.rec_prsn_frst_name, ' ') || ' ' ||
		  coalesce(dfact.rec_prsn_last_name, ' '))) rcvr_full_name,
	coalesce(dfact.rec_cnsmr_brth_date, parse_date('%m/%d/%Y','01/01/1900')) rcvr_dob,
	coalesce(rcnsmr.brth_city_name, '-') rcvr_place_of_birth, 
	coalesce(dfact.rec_brth_cntry_iso_2_code, '-') rcvr_nationality,              
	coalesce(dfact.rec_addr_cntry_iso_2_code, '-') rcvr_residence,  
	dfact.rec_cnsmr_ph_nbr rcvr_phone,
	dfact.rec_addr_line1_text rcvr_cnsmr_line1_addr,
	dfact.rec_addr_city_name rcvr_cnsmr_city,
	dfact.rec_addr_postal_code rcvr_cnsmr_zip,
	coalesce(dfact.rec_addr_cntry_iso_2_code, '-') rcvr_cnsmr_country,
	dfact.rec_addr_state_name rcvr_cnsmr_state,
	coalesce(case
				when dfact.rec_cnsmr_id_1_doc_type_desc = 'Passport'  OR dfact.rec_cnsmr_id_2_doc_type_desc = 'Passport' then
				 'PAS' 
				when dfact.rec_cnsmr_id_1_doc_type_desc = 'Drivers License' OR dfact.rec_cnsmr_id_2_doc_type_desc  = 'Drivers License' then
				 'SIM'
				when dfact.rec_cnsmr_id_1_doc_type_desc = 'Government ID'  OR dfact.rec_cnsmr_id_2_doc_type_desc = 'Government ID'then
				 'KTP'
				when dfact.rec_cnsmr_id_1_doc_type_desc = 'International ID' OR dfact.rec_cnsmr_id_2_doc_type_desc = 'International ID'then
				 'KTP'
				when dfact.rec_cnsmr_id_1_doc_type_desc = 'Social Security Nbr' OR dfact.rec_cnsmr_id_1_doc_type_desc = 'Social Security Nbr'then
				 'KTP'
				else
				 NULL
			end, 'suket') rcv_photo_id_type,  
	dfact.rec_cnsmr_id_1_nbr_displ_text rcvr_cnsmr_photo_id_number,
	
    dfact.rec_cnsmr_id_1_issu_month||'/'||dfact.rec_cnsmr_id_1_issu_day||'/'||dfact.rec_cnsmr_id_1_issu_year as rcvrcnsmrphoto_id_issue_year,
	coalesce(dfact.rec_cnsmr_id_1_issu_iso_cntry_code, '-') rcvrcnsmrphoto_id_issue_cntry,                
	coalesce(RCV_PTY.Iso_Cntry_Code, '-') rcvr_agent_country,
	coalesce(SND_PTY.Iso_Cntry_Code, '-') send_agent_country_code,
	coalesce(RCV_PTY.Iso_Cntry_Code, '-') rcv_agent_country_code,
	dfact.initg_evnt_iso_crncy_code from_frgn_crncy,
	abs(dfact.initg_face_tran_amt) from_frgn_amt,
	FX_VER_FROM_FRGN.mid as from_frgn_fx_rate,
	dfact.evnt_iso_crncy_code to_frgn_crncy,
	abs(dfact.evnt_face_tran_amt) to_frgn_amt,                
	FX_VER_TO_FRGN.mid as to_frgn_fx_rate,
	case when dfact.initg_dlvr_optn_id = 10 then 'REK'
	  else 'UT' 
	end as t_to_to_funds_code,
	'T' as t_from_my_client_tax_reg_nbr,
	'T' as t_to_my_client_tax_reg_nbr,
	coalesce((case when dts.intnd_rcv_agent_id = '70641279' then 'CIMB NIAGA' else bank.locale_text end), '-') as to_instn_name, 
	coalesce(null, '-') AS to_swift_code,
	coalesce(dts.intnd_rcv_rule_prcs_acct_nbr, '-') as to_account,
	coalesce(dfact.rec_cnsmr_ocupn_text, '-') rcvr_occupation 

	from compliance_datamart.1137_poe_tran dfact 
		left join mgt.fx_rate FX_VER_FROM_FRGN 
			ON dfact.initg_evnt_tran_fx_ver_id = cast(FX_VER_FROM_FRGN.fx_version_id as string) 
      and FX_VER_FROM_FRGN.from_iso_currency = dfact.initg_evnt_iso_crncy_code  
			and FX_VER_FROM_FRGN.to_iso_currency = 'IDR'
		left join mgt.fx_rate FX_VER_TO_FRGN
			ON dfact.evnt_fx_ver_id = cast(FX_VER_TO_FRGN.fx_version_id as string) 
      and FX_VER_TO_FRGN.from_iso_currency = dfact.evnt_iso_crncy_code and FX_VER_TO_FRGN.to_iso_currency = 'IDR' 
		Left join bank on dfact.rec_cust_rcv_nbr = bank.cust_rcv_nbr
		left join snd_cnsmr_brth_city scnsmr
				on dfact.sen_cnsmr_identifier_hashkey = scnsmr.cnsmr_identifier_hashkey
		  left join  rcv_cnsmr_brth_city rcnsmr 
				on dfact.rec_cnsmr_identifier_hashkey = rcnsmr.cnsmr_identifier_hashkey ,
		 BADT SF
		 Left join IDW_TRAN dts 
			ON SF.poe_tran_evnt_id = dts.poe_tran_evnt_id,	 
        PTY snd_pty, 
		    PTY RCV_PTY	
      		  
 where	
	dfact.poe_tran_evnt_id = SF.poe_tran_evnt_id
  and dfact.tran_evnt_date_pt = cast(SF.evnt_tran_evnt_datetime as date) 
	and dfact.partnr_rec_agent_id = RCV_PTY.agent_id
	and dfact.partnr_snd_agent_id = snd_pty.agent_id
    and dfact.evnt_tran_evnt_code in ('REC', 'RRC', 'RDT', 'RRD') 
	and (case
			when dfact.initg_iso_cntry_code = 'ID' and
              dfact.evnt_iso_cntry_code <> 'ID' then 'TKLOP'
			when dfact.initg_iso_cntry_code <> 'ID' and
              dfact.evnt_iso_cntry_code = 'ID' then 'TKLIP'
		end) = 'TKLIP' 
	and ltrim(rtrim(dfact.rec_prsn_frst_name)) != 'AUTOMATED' 
    and ltrim(rtrim(dfact.rec_prsn_last_name)) != 'INTERNAL'
	and ltrim(rtrim(dfact.sen_prsn_frst_name)) != 'AUTOMATED' 
	and ltrim(rtrim(dfact.sen_prsn_last_name)) != 'INTERNAL'
	