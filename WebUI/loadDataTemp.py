import os
import json
import sys

import requests
from requests import ConnectionError, HTTPError

def loadData():


    asterix_host = "localhost"
    asterix_port = 19002

   

    query_statement = '''
                        use dataverse Tasks;

                        load dataset DisplayGraph using localfs(("path"="localhost:///home/zhimin/study/CS295/Pregelix_Social_Graph_Analysis_Visulization/Pregelix_Social_Graph/WebUI/graphFiles/test/d-9.adm"),("format"="adm"));

                        load dataset TaskOne using localfs(("path"="localhost:///home/zhimin/study/CS295/Pregelix_Social_Graph_Analysis_Visulization/Pregelix_Social_Graph/WebUI/graphFiles/test/1-9.adm"),("format"="adm"));

                        load dataset TaskTwo using localfs(("path"="localhost:///home/zhimin/study/CS295/Pregelix_Social_Graph_Analysis_Visulization/Pregelix_Social_Graph/WebUI/graphFiles/randomGraph/TaskTwo.adm"),("format"="adm"));

                        load dataset TaskThree using localfs(("path"="localhost:///home/zhimin/study/CS295/Pregelix_Social_Graph_Analysis_Visulization/Pregelix_Social_Graph/WebUI/graphFiles/randomGraph/TaskThree.adm"),("format"="adm"));

                        load dataset TaskFour using localfs(("path"="localhost:///home/zhimin/study/CS295/Pregelix_Social_Graph_Analysis_Visulization/Pregelix_Social_Graph/WebUI/graphFiles/randomGraph/TaskFour.adm"),("format"="adm"));

                        load dataset AccountInfo using localfs(("path"="localhost:///home/zhimin/study/CS295/Pregelix_Social_Graph_Analysis_Visulization/Pregelix_Social_Graph/WebUI/graphFiles/randomGraph/Account.adm"),("format"="adm"));
                    '''

    load = {
        #'ddl': "create dataverse Communication;"
        'statements': query_statement
    }
    
    http_header = {
        'content-type': 'application/json'
    }

    # Now we run our query
    print "load data...\n"

    update_url = "http://" + asterix_host + ":" + str(asterix_port) + "/update"

    try:
        response = requests.get(update_url, params=load, headers=http_header)

    except (ConnectionError, HTTPError):
        print "Encountered connection error; stopping execution"
        sys.exit(1)

    return True


loadData()
