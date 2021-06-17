import shutil
import sys

network = sys.argv[1]

if network == 'private' or network == 'testnet':
    print('Removing "%s" directory...' % network)
    shutil.rmtree(network)
else:
    print('Unknown network: %s' % network)
