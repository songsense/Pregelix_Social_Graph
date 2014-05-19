import os
import json
import sys

import requests
from requests import ConnectionError, HTTPError

def bootstrap():
    asterix_host = "localhost"
    asterix_port = 19002

    # First, let's get the path to this script, we'll need it to configure the demo.
    base = os.path.dirname(os.path.realpath(__file__))
    print "Running AdmAQL101 from",base

    # First, we bootstrap our request query
    print "Loading Graph Dataset..."
    query_statement = open("taskRelatedFile/createDataBase.txt").read().split("####");
    
    print query_statement[0];
    
    ddl = {
        #'ddl': "create dataverse Communication;"
        'ddl': query_statement[0]
    }
    
    ddl1 = {
        'ddl':query_statement[0]
    }

    insert = {
        #"statements" : "use dataverse Task1; " + "\n".join(query_statement[1:3])
        "statements": query_statement[1]
    }

    http_header = {
        'content-type': 'application/json'
    }

    # Now we run our query
    print "Running query...\n"

    ddl_url = "http://" + asterix_host + ":" + str(asterix_port) + "/ddl"
    update_url = "http://" + asterix_host + ":" + str(asterix_port) + "/update"
    try:
        requests.get(ddl_url, params=ddl)
        response = requests.get(update_url, params=insert, headers=http_header)
        #requests.get(ddl_url, params=ddl1)
    except (ConnectionError, HTTPError):
        print "Encountered connection error; stopping execution"
        sys.exit(1)

    return True
