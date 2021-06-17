import json
import os

genesis = {
    'config': {
        'chainId': 2757,
        'homesteadBlock': 0,
        'eip150Block': 0,
        'eip155Block': 0,
        'eip158Block': 0,
        'byzantiumBlock': 0,
        'constantinopleBlock': 0,
        'petersburgBlock': 0,
        'istanbulBlock': 0,
        'muirGlacierBlock': 0
    },
    'difficulty': '0',
    'gasLimit': '1000000000',
    'alloc': {}
}

balance = '100000000000000000000000'

for path in sorted(os.listdir('keystore')):
    account = '0x' + path.split('--')[-1]
    genesis['alloc'][account] = {'balance': balance}

with open('genesis.json', 'w') as p:
    p.write(json.dumps(genesis, indent=4))
    p.write('\n')
