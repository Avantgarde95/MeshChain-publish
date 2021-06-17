import json
import os
import sys

network = sys.argv[1]
accountIndex = int(sys.argv[2])
account = '0x' + sorted(os.listdir('keystore'))[accountIndex].split('--')[-1]

configurationPath = '../App/Configuration.json'
print('Saving user address to %s...' % configurationPath)

with open(configurationPath, 'r') as p:
    configuration = json.load(p)

with open(configurationPath, 'w') as p:
    configuration['userAddress'] = account
    json.dump(configuration, p, indent=4)

args = [
    '--keystore keystore',
    '--etherbase ' + account,
    '--rpc',
    '--rpcaddr 0.0.0.0',
    '--rpcport 8545',
    '--rpccorsdomain "*"',
    '--rpcapi personal,admin,eth,net,web3,txpool,debug',
    '--ws',
    '--wsaddr 0.0.0.0',
    '--wsport 8546',
    '--wsapi personal,admin,eth,net,web3,txpool,debug',
    '--wsorigins "*"',
    '--allow-insecure-unlock'
]

if network == 'private':
    args += [
        '--datadir private',
        '--port 8888',
        '--networkid 2757',
        '--syncmode full',
        '--nodiscover'
    ]
elif network == 'testnet':
    args += [
        '--testnet',
        '--datadir testnet',
        '--syncmode fast'
    ]
else:
    print('Unknown network: %s' % network)
    exit(0)

print('Running geth on the network "%s"...' % network)
print('(Args: %s)' % ' '.join(args))
os.system('geth console %s' % ' '.join(args))
