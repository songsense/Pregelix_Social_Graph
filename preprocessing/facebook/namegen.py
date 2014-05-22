#!/usr/bin/python
#
# Random name generator
# format:
# <First name> <Last name>
import sys
import random

def generate_first(fname_set):
    try:
        fset = open(fname_set, 'r')
    except IOError:
        print "Cannot open first name fileset!"
        exit(1)
    names = fset.read().splitlines()
    pos = random.randrange(len(names))
    return names[pos]

def generate_last(lname_set):
    try:
        lset = open(lname_set, 'r')
    except IOError:
        print "Cannot open last name fileset!"
        exit(1)
    names = lset.read().splitlines()
    pos = random.randrange(len(names))
    return names[pos]

def generate(fname_set, lname_set):
    return generate_first(fname_set) + " " + generate_last(lname_set)

if __name__ == '__main__':
    length = len(sys.argv)
    if length != 3:
        print "Usage: namegen.py <first_name_set> <last_name_set>"
        exit(1)
    print generate(sys.argv[1], sys.argv[2])

