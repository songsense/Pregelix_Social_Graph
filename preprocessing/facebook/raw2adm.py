#!/usr/bin/python
#
# Convert the raw graph into adm format
# raw graph format:
#   <id> <friend_id_1> <friend_id_2> ...
# adm format example:
#   {{"source_node":1, "label":"Siming", "target_node":[2,3],"weight":[1.0,1.0]}}
import sys
import namegen

# Maximum entry in this big social graph
MAX_ENTRY = 2198462
FIRST_NAME_SET = "first-names.txt"
LAST_NAME_SET = "surnames.txt"

def split_line(line):
    if line == "":
        return
    splited = line.split(" ")
    return splited

def generate_label():
    name = namegen.generate(FIRST_NAME_SET, LAST_NAME_SET)
    return "\"" + name + "\""

def open_outfile(filename):
    return open(filename, 'a')

def to_outfile(fp, string):
    return fp.write(string + "\n")

def close_outfile(fp):
    fp.close()

def convert(filename):
    try:
        graph_file = open(filename, 'r')
    except IOError:
        print "Cannot open graph file!"
        exit(1)
    outfile = open_outfile("test-out")

    line_cnt = 0
    for i in graph_file:
        i = i.strip('\n')
        line_cnt += 1
        strs = split_line(i)
        strs_len = len(strs)
        if strs_len == 0:
            print "Warning: @line ", line_cnt, " is empty"
            continue
        # first item: source_node
        source_node = "\"source_node\":" + strs[0]

        # second item: label
        label = "\"label\":" + generate_label()

        # third item: target node
        # count the number of friends within the range of the graph
        fcnt = 0;
        friends = "\"target_nodes\":["
        # If no friends, just discard this node
        if strs_len == 1 or strs[1] == '' or int(strs[1]) > MAX_ENTRY:
            print "Warning: @line ", line_cnt, ", no friends"
            continue;
        next_num = int(strs[1])
        for j in range(1, strs_len):
            fcnt += 1
            friends += strs[j]
            if j == strs_len - 1:
                break;
            try:
                next_num = int(strs[j+1])
            except ValueError:
                continue
            if next_num > MAX_ENTRY:
                break
            else:
                friends += ","
        friends += "]"

        # last item: weights (by now they are all 1.0
        weight = "\"weight\":["
        for j in range(0, fcnt-1):
            weight += "1.0,"
        weight += "1.0]"

        # combine items
        adm_entry = "{" + source_node + "," + label + "," + \
                    friends + "," + weight + "}"
        #print adm_entry
        outfile.write(adm_entry + '\n')

    graph_file.close()
    outfile.close()

if __name__ == '__main__':
    length = len(sys.argv)
    if length != 2:
        print "Usage: raw2adm.py <input_file> [> info_file]"
        exit(1)
    convert(sys.argv[1])
