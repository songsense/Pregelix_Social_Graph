import os
import json
import sys

import requests
from requests import ConnectionError, HTTPError

def bootstrap():


    asterix_host = "localhost"
    asterix_port = 19002

    # First, we bootstrap our request query
    print "createDataBase..."
   

    query_statement = '''drop dataverse Graph if exists;
                         create dataverse Graph;
                         use dataverse Graph;

                        create type GraphType as open{
                               source_node: int32,
                               label: string,      
                               target_nodes: [int32],
                               weight: [double] 
                        }

                        create dataset OriginalGraph(GraphType) primary key source_node;

                        create type DisplayGraphType as open {
                               id: string,
                               login_user_id: int32, 
                               source_node: int32, 
                               label: string,      
                               target_nodes: [int32] 
                        }
                        create dataset DisplayGraph(DisplayGraphType) primary key id;


                        drop dataverse Tasks if exists;
                        create dataverse Tasks;
                        use dataverse Tasks;

                        create type TaskOneType as open{
                               id: string,
                               login_user_id: int32,
                               target_user_id: int32,
                               length: int32,
                               path: [int32]

                        }

                        create dataset TaskOne(TaskOneType) primary key id;
                        create index TaskOneIdx on TaskOne(login_user_id);

                        create type TaskTwoType as open {
                               user_id: int32,
                               community_id: int32
                        }
                        create dataset TaskTwo(TaskTwoType) primary key user_id;

                        create type TaskThreeType as open {
                               user_id: int32, 
                               suggested_friends: [int32]
                        }
                        create dataset TaskThree(TaskThreeType) primary key user_id;


                        create type TaskFourType as open {
                               user_id: int32, 
                               importance: double
                        }
                        create dataset TaskFour(TaskFourType) primary key user_id;

                        drop dataverse Account if exists;
                         create dataverse Account;
                         use dataverse Account;

                         create type AccountType as open{
                               user_id: int32,
                               label: string,      
                               password: string
                        }

                        create dataset AccountInfo(AccountType) primary key user_id;
                    '''

    ddl = {
        #'ddl': "create dataverse Communication;"
        'ddl': query_statement
    }
    
    http_header = {
        'content-type': 'application/json'
    }

    # Now we run our query
    print "Running query...\n"

    ddl_url = "http://" + asterix_host + ":" + str(asterix_port) + "/ddl"
    try:
        requests.get(ddl_url, params=ddl)
    except (ConnectionError, HTTPError):
        print "Encountered connection error; stopping execution"
        sys.exit(1)

    return True


bootstrap()
