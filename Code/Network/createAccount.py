import os
import sys

repeat = 1

if len(sys.argv) > 1:
    repeat = int(sys.argv[1])

for i in range(repeat):
    os.system('geth account new --datadir .')
