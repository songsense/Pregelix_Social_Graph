import sys
import requests
from requests import ConnectionError, HTTPError
import json
if __name__ == '__main__':
	length = len(sys.argv)
	if length != 3:
		print "usage: python db_admcoconverter.py dataverse, dataset"
		exit(1)
	dataverse = sys.argv[1]
	dataset = sys.argv[2]

	queryStatement = "use dataverse " + dataverse + ";";
	queryStatement = queryStatement + "for $i in dataset " + dataset + " return $i;"
	http_header = {"content-type":"application/json"}
	query = {"query": queryStatement}
	print queryStatement

	asterix_host = "localhost"
	asterix_port = 19002
	query_url = "http://" + asterix_host + ":" + str(asterix_port) + "/query"

	response = 01
	try:
		response = requests.get(query_url, params=query, headers=http_header)
		print response.status_code		
	except (ConnectionError, HTTPError):
		print "Encountered connection error; stopping execution with code: " + str(response)
		sys.exit(1)
	data = response.json()
	print data
	results = data['results']

	f = open(dataset + ".adm", "w")
	for line in results:
		line = line.replace("\n","")
		line = line.replace("int32:","")
		line = line.replace("{","")
		line = line.replace("}","")
		line = "{" + line + "}"
		print line
		f.write(line)
		f.write("\n")
	f.close()
