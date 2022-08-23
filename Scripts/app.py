from time import sleep
from flask import Flask, send_from_directory
from flask_restx import Api, Resource, fields
from werkzeug.middleware.proxy_fix import ProxyFix
from werkzeug.datastructures import FileStorage
import os
import json
from os.path import exists
import time
import pandas as pd
import xlrd as xl
import requests as rq
from utils import csvValidator as csvVal

app = Flask(__name__)
app.wsgi_app = ProxyFix(app.wsgi_app)
api = Api(app, version="1.0", title="API to get oracle to gcp migration", description="API to get oracle to gcp migration",)
wfProcessMap = {}
otgTableMap = {}
otgTabColMap = {}
host = "localhost"
port = "9080"
path = "sql"



ns = api.namespace("gcpmig", description="oracle to gcp migration APIs")
nsqa = api.namespace("qautils", description="Testing utility APIs")

oraQueries = {
    "query1" : "select * from dummy",
}

sqlMdl = api.model(
    "sql", {"sql_query": fields.String(required=True, description="input query to get mappings")}
)

# parser = api.parser()
# parser.add_argument(
#     "sql_query", type=str, required=True, help="Input oracle query to get mapping details", location="form"
# )

parser = api.parser()
parser.add_argument(
    "query", type=str, required=True, help="Input oracle query to get mapping details", location="form"
)
parser.add_argument(
    "wf_name", type=str, required=True, help="unique work flow name, if workflow has multiple seesions append with _ss1, _ss2, _ss3 etc.. for each session", location="form"
)

csvParser = api.parser()
csvParser.add_argument( 
    "input_csv_1", type=FileStorage, required=True, help="upload csv 1 file", location='files' 
    )
csvParser.add_argument(
    "input_csv_2", type=FileStorage, required=True, help="upload csv 2 file", location='files'
    )

#todo check workflow is required
#"session_name", type=str, required=False, help="work flow session name", location="form"

@ns.route("/process_query")
class SQLParser(Resource):
    """Shows a list of all operations required to parse, and get mappings required"""

    @api.doc(parser=parser)
    #@api.marshal_with(sqlMdl, code=201)
    def post(self):
        """get list of tables from given query"""
        args = parser.parse_args()
        #todo_id = "todo%d" % (len(TODOS) + 1)
        #TODOS[todo_id] = {"task": args["task"]}
        print(args)
        sqlStr = args["query"]
        wfName = args["wf_name"]
        #writeToFile(sqlStr.upper())
        resp = processSql(sqlStr, wfName)
        #load mapping file
        #processMappingFiles()
        #return respJSON, 201
        return resp, 201

#@ns.route("/get_tables")
@ns.route("/get_tables/<wf>")
@api.param("wf", "The work flow name/identifier")
@api.response(404, "Workflow not found")
class SQLParser(Resource):
    """Shows a list of all operations required to parse, and get mappings required"""

    def get(self,wf):
        """get list of tables from given query"""
        resp = wfProcessMap[wf]["tables"]
        return resp, 200


#@ns.route("/get_columns")
@ns.route("/get_columns/<wf>")
@api.param("wf", "The work flow name/identifier")
@api.response(404, "Workflow not found")
class SQLParser(Resource):
    """Shows a list of all operations required to parse, and get mappings required"""
    def get(self, wf):
        """get a list of columns from given query"""
        resp = wfProcessMap[wf]["columns"]
        return resp, 200

#@ns.route("/get_aliases")
@ns.route("/get_aliases/<wf>")
@api.param("wf", "The work flow name/identifier")
@api.response(404, "Workflow not found")
class SQLParser(Resource):
    """Shows a list of all operations required to parse, and get mappings required"""

    def get(self, wf):
        """get a list of aliases from given query"""
        print(wfProcessMap)
        resp = wfProcessMap[wf]["alias"]
        return resp, 200

@nsqa.route("/csvCompare")
@api.representation('application/octet-stream')
class CompareCSV(Resource):
    """util API to compare two xlsx/csv files and returns detailed information about records"""

    @api.doc(parser=csvParser)
    def post(self):
        """get a list of aliases from given query"""
        args = csvParser.parse_args()
        csv1 = args['input_csv_1']
        csv2 = args['input_csv_2']
        wb1 = xl.open_workbook(file_contents=csv1.read())
        wb2 = xl.open_workbook(file_contents=csv2.read())

        resp = csvVal.processCSV(wb1,wb2)

        response = send_from_directory(directory=os.getcwd(), filename='Validation_response.txt')
        response.headers['my-custom-header'] = 'my-custom-status-0'
        return response

        # filename = rq.form.get('Validation_response.txt')
        # file_data = codecs.open(filename, 'rb').read()
        # return file_data
        #open("Validation_response.txt", "wb").write(resp.content)

        #send file as response "Validation_response.txt"
    #     response = send_file(
    #     filename_or_fp="Validation_response.txt",
    #     mimetype="application/octet-stream",
    #     as_attachment=True,
    #     attachment_filename= "Validation_response.txt" #data["Validation_response.txt"]
    # )
        #return resp, 200

@ns.route("/get_mappings/<wf>")
@api.param("wf", "The work flow name/identifier")
@api.response(404, "Workflow not found")
class SQLParser(Resource):
    """Shows a list of all operations required to parse, and get mappings required"""

    def get(self, wf):
        """get a list of mapping from given query"""
        resp = json.loads("""{"table_mapping":{}, "columns_mapping":{}, "missing_mappings":{"tables" : [], "columns": []}}""")

        # load tables Mapping

        tm = resp['table_mapping']
        mm = resp['missing_mappings']
        cm = resp['columns_mapping']
        als = wfProcessMap[wf]["alias"]
        tabKeys = otgTableMap.keys()
        for table in wfProcessMap[wf]["tables"]:
            if table in tabKeys:
                tm[table] = otgTableMap[table]
            else:
                missingtab = mm['tables']
                missingtab.append(table)

        # load columns Mapping

        colKeys = otgTabColMap.keys()
        for col in wfProcessMap[wf]["columns"]:

            lst =  col.strip().split(".")
            if len(lst) == 2:
                tabName = lst[0]
                colName = lst[1]
                if tabName in als.keys():
                    col = als[tabName] + "." + colName

            if col in colKeys:
                cm[col] = otgTabColMap[col]
            else:
                missingCol = mm['columns']
                missingCol.append(col)

        return resp, 200

def processSql(sql,wf):
    api_url = "http://" + host + ":" + port + "/"+ path
    headers =  {"Content-Type":"application/json"}
    response = rq.post(api_url, data=sql, headers=headers)
    wfProcessMap[wf] = response.json()
    return response.json()


# def processSql():
#     global respJSON
#     if os.path.isfile("response.json"):
#         os.remove("response.json")
#     os.system( "java -jar sqlutils.jar " + "inputQuery.sql")
#     print ('executing JAR')
#     file_exists = True
#     count = 0
#     while(file_exists):
#         print ('inside while And Count is ', count )
#         file_exists = os.path.isfile("response.json")
#         if file_exists:
#             break
#         else:
#             time.sleep(3)
#         count += 1
#         if count >= 30:
#             raise Exception("Unable to process the SQL, its taking more than 5 minutes")
#     respJSON = loadJSON()

def processMappingFiles():
    fls = os.listdir("mapping_files")
    for fl in fls:
        # read all the xlsx files
        wb = xl.open_workbook("mapping_files/" + fl)
        shts = wb.sheet_names()
        for sht in shts:

            print ("processing " + "Excel :" + fl + "work sheet: " + sht)
            ws = wb.sheet_by_name(sht)
            loadMappings(wb,ws)



def loadMappings(wb,ws):
    """Load mapping details of oracle table and gcp table to a map"""
    global otgTableMap
    global otgTabColMap

    # read master mapping file and build table column map
    #df = pd.read_excel('Table_Column_Mappings_Master.xlss')
    # wb = xl.open_workbook('Table_Column_Mappings_Master.xlsx')
    # ws = wb.sheet_by_name('CR_Mapping')
    num_rows = ws.nrows - 1
    cr = -1
    while cr < num_rows:
        cr += 1
        row = ws.row(cr)
        oraTable = ws.cell(cr, 0).value.upper()
        oraCol = ws.cell(cr, 2).value.upper()
        gcpTable = ws.cell(cr, 3).value.upper()
        gcpCol = ws.cell(cr, 4).value.upper()
        otgTableMap[oraTable.strip()] = gcpTable.strip() 
        otgTabColMap[oraTable.strip() + "." + oraCol.strip()] = gcpTable.strip() + "." + gcpCol.strip()
    #print ("Mappings from master file :" , otgMap)

def loadJSON():
    # Opening JSON file
    f = open('response.json')
    # returns JSON object as 
    # a dictionary
    data = json.load(f)
    return data

def writeToFile(str):
    #open text file
    f = open("inputQuery.sql", "w")
 
    #write string to file
    f.write(str)
 
    #close file
    f.close()
    
    
if __name__ == "__main__":
    app.run(port=5500, debug=True)


