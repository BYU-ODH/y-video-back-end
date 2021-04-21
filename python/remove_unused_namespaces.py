"""
This script will remove all unused namespace imports.
It relies on the output of $ lein eastwood > {filename}
If the removed import is the last import, it will be replaced
with two closing parens on their own line. These may be
manually moved to the previous line to help parinfer with parsing.
"""

filename = input('Eastwood output: ')
with open(filename) as f:
    lines = f.read().split('\n')

for line in lines:
    if 'unused-namespaces:' not in line:
        continue
    lSplit = line.split(' ')
    tarFilename = lSplit[0].split(':')[0]
    namespace = lSplit[3]
    with open(tarFilename) as f:
        fLines = f.read().split('\n')
    removed = False
    with open(tarFilename, 'w') as f:
        for fline in fLines:
            if not removed and fline.strip()[1:].startswith(namespace):
                removed = True
                if fline.endswith('))'):
                    print('))', file=f, end='\n')
                continue
            print(fline, file=f, end='\n')
