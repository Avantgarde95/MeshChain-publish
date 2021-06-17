import os
import sys

network = sys.argv[1]
accountIndex = int(sys.argv[2])
account = '0x' + sorted(os.listdir('keystore'))[accountIndex].split('--')[-1]

args = [
    '--bzzaccount ' + account,
    '--keystore keystore',
    '--httpaddr 0.0.0.0'
]

if network == 'private':
    args += [
        '--datadir private',
        '--bzznetworkid 200'
    ]
elif network == 'testnet':
    args += [
        '--datadir testnet'
    ]
else:
    print('Unknown network: %s' % network)
    exit(0)

print('Running swarm on the network "%s"...' % network)
print('(Args: %s)' % ' '.join(args))
os.system('swarm %s' % ' '.join(args))
