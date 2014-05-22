# import bootstrap
import requests
import os
from bottle import route, run, template, get, debug, static_file, request, response
import json


debug(True)
http_header = { "content-type": "application/json" }

# Core Routing
@route('/')
def jsontest():
    return template('graph_display')
    # return template('test')
    

@route('/static/<filename:path>')
def send_static(filename):
    return static_file(filename, root='static')

# API Helpers
def build_response(endpoint, data):
    api_endpoint = "http://localhost:19002/" + endpoint
    response = requests.get(api_endpoint, params=data, headers=http_header)
    try:
        return response.json();
    except ValueError:
        return []

# Upload file
# @route('/upload', method='POST')
# def run_upload_file():
#     upload = request.files.get('upload')
#     name, ext = os.path.splitext(upload.filename)
#     if ext not in ('.adm','.txt'):
#         return 'File extension not allowed.'

#     file_path = "./graphFiles/";
#     if not os.path.exists(file_path):
#         os.makedirs(file_path)

#     file_path_name = os.path.join(file_path, upload.filename);
#     print file_path_name;
#     with open(file_path_name, 'w') as open_file:
#         open_file.write(upload.file.read())
#     return '<p>OK</p>'
    # endpoint = "ddl";
    # data = {'ddl': 'drop dataverse OriginalGraph if exists; create dataverse OriginalGraph; use dataverse OriginalGraph; create type GraphType as open{source_node: int32, label: string, target_node:{{int32}}, weight:{{double}}} \n create dataset Graph(GraphType) primary key source_node;'}
    # build_response(endpoint, data);
    # endpoint = "update";
    # updateQuery = 'use dataverse OriginalGraph; load dataset Graph using localfs(("path"="localhost://'+file_path_name+'"),("format"="adm"))';
    # data = {'update' : updateQuery}
    # build_response(endpint, data);

# Log In
@route('/logIn', method = 'POST')
def run_log_in():
    asterix_host = "localhost"
    asterix_port = 19002
    user_id = request.forms.get('user_id');
    password = request.forms.get('password');
    #print password;
    #print user_id+','+password
    query_statement = 'use dataverse Account; for $n in dataset AccountInfo where $n.user_id='+str(user_id)+' return $n';
    query = {
        'query': query_statement
    };
    http_header = {
        'content-type': 'application/json'
    }
    query_url = "http://" + asterix_host + ":" + str(asterix_port) + "/query"
    try:
        response = requests.get(query_url, params=query, headers=http_header)
        result = str(response.json()["results"][0]);
        # print result;
        resultArray = result.split(', ');
        resultLabel = resultArray[1];
        resultPassword = resultArray[2];
        labelArray = resultLabel.split(':');
        label = labelArray[1].replace('"', '').strip();
        passwordArray = resultPassword.split(':');
        correctPassword = passwordArray[1].replace('"', '').replace('}', '').strip();
        
        if(password==correctPassword):
            print "correct"
            return '<p id="returnResult">1</p><p id="returnLabel">'+ label +'</p>'
        else:
            print "error"  
            return '<p id="returnResult">0</p>'
    except (ConnectionError, HTTPError):
        print "Encountered connection error; stopping execution"
        sys.exit(1)

    return True

# API Endpoints   
@route('/query')
def run_asterix_query():
    return (build_response("query", dict(request.query)))
    
@route('/query/status')
def run_asterix_query_status():
    return (build_response("query/status", dict(request.query)))

@route('/query/result')
def run_asterix_query_result():
    return (build_response("query/result", dict(request.query)))


@route('/ddl')
def run_asterix_ddl():
    return (build_response("ddl", dict(request.query)))

@route('/update')
def run_asterix_update():
    return (build_response("update", dict(request.query)))

    
# res = bootstrap.bootstrap()
run(host='localhost', port=8081, debug=True)
