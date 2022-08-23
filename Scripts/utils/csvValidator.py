import xlrd
from collections import Counter


def processCSV(rb1_birp, rb2_gcp):
    # rb1_birp = xlrd.open_workbook('Guatemala_Send_Birp_View.xlsx')
    # rb2_gcp = xlrd.open_workbook('CR_GUATEMALA_SEND_VIEW_GCP_PROD_202207280803.xlsx')
    birp_sheet = rb1_birp.sheet_by_index(0)
    gcp_sheet = rb2_gcp.sheet_by_index(0)

    respJSON = {"meta_data" : {"meta_info" : "TODO"}, "entity":{}}

    max_rownum = birp_sheet.nrows -1
    tofile = open('Validation_response.txt', 'a')
    tofile.truncate(0)
    header = birp_sheet.row_values(0)
    primary_key = birp_sheet.col_values(0)
    gcp_primary_key = gcp_sheet.col_values(0)
    del gcp_primary_key[0]

    null_count = 0
    null_count = gcp_primary_key.count('')
    if null_count > 0:
        for x in range(0,null_count):
            gcp_primary_key.remove('')
    empty_count = gcp_primary_key.count('EMPTY')
    if empty_count > 0:
        for x in range(0,empty_count):
            gcp_primary_key.remove('EMPTY')
    gcp_count = len(gcp_primary_key)
    tofile.write("No. of records in Source: {}\n".format(birp_sheet.nrows-1))
    tofile.write("No. of records in Target: {}".format(gcp_count))
    tofile.write("\n---------------------------------------------------------------------")
    missing_records = list(set(primary_key).difference(gcp_primary_key))
    for k in range(0,len(missing_records)):
        if missing_records[k] == header[0]:
            del missing_records[k]

    if len(missing_records) != 0:
        tofile.write("\nMissing records:\n")
        for k in missing_records:
            tofile.write(k)
            tofile.write('\t')       
        tofile.write("\n---------------------------------------------------------------------")
    if len(missing_records) == 0:
        tofile.write("\nNo Missing records in Target\n")
        tofile.write("\n---------------------------------------------------------------------")
    #Duplicate check
    primary_key_remove_dup = Counter(primary_key)
    duplicates = {key:value for key, value in primary_key_remove_dup.items() if value > 1}
    duplicate_pkey = duplicates.keys()

    if len(duplicate_pkey) == 0:
        tofile.write("\nDuplicate check:\n\nNo Duplicate Records found in Source!")
        tofile.write("\n---------------------------------------------------------------------")
    else:
        tofile.write("\nDuplicate check:\nDuplicate Records in source:\n")
        for k in duplicate_pkey:
            tofile.write(k)
            tofile.write('\t')
    #source count without duplicates
    source_count_del_dup = (birp_sheet.nrows-1) - len(duplicate_pkey)
    tofile.write("\n\nsource count without duplicates: {}".format(source_count_del_dup))
    tofile.write("\n------------------------------------------------------------------------")

    if len(missing_records) == 0 and len(duplicate_pkey) == 0:
        tofile = open('Validation_response.txt', 'a')
        tofile.write("\n\n\t\tColumn name\t\tSource value\t\tTarget value")
        for rownum in range(1,max_rownum+1): 
            tofile.write("\n\nPrimary_key: {}\n".format(int(primary_key[rownum])))
            if rownum < birp_sheet.nrows:    
                row_rb1_birp = birp_sheet.row_values(rownum)
                row_rb2_gcp = gcp_sheet.row_values(rownum)
        
                for colnum, (c1, c2) in enumerate(zip(row_rb1_birp, row_rb2_gcp)):
                    if c1 != c2:
                        tofile.write('\n')
                        tofile.write("Cell {}{}    {} - {} != {}".format(rownum+1,xlrd.formula.colname(colnum),header[colnum], c1, c2))

            if row_rb1_birp == row_rb2_gcp:
                tofile.write("\n\t\tNo mismatches found. Record is matching")
    else:
        tofile.write("\n\nRemove duplicates from source or missing records from Source to proceed Data validation")
    tofile.close()
    return respJSON
#    tofile.close()
