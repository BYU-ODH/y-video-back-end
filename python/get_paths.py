"""
Extracts all url paths from swagger json.

Either pipe json into stdin, or provide url for swagger.json as 1st argument.
Priority is given to command line argument. If provided, stdin will be ignored.

Example usage:

url method:
    $ python3 get_paths.py http://localhost:3030/api/swagger.json

stdin method:
    $ cat my_data.json | python3 get_paths.py
"""

import sys

args = sys.argv

if len(args) > 1:
    if args[1] in ['-h', '--help', '-help', '--h']:
        print(f'USAGE: python3 {args[0]} <url-to-json>\nor\nUSAGE: cat <file-with-json> | python3 {args[0]}')
        exit(0)
    # get json from url
    import requests as r
    url = args[1]
    res = r.get(url)
    assert res.status_code == 200
    js = res.json()
else:
    # get json from stdin
    import json
    raw_in = sys.stdin.readlines()
    raw_in = '\n'.join(raw_in)
    js = json.loads(raw_in)

for path in sorted(js.get('paths').keys()):
    for path_method in js.get('paths').get(path).keys():
        print(path_method.lower(), path, sep=':\t')
