import sys

with open('full_build_output.txt', 'r') as f:
    for line in f:
        if '[ERROR]' in line:
            print(line.strip())
