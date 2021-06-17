const HDWalletProvider = require('@truffle/hdwallet-provider');

const mnemonic = 'portion fold vessel oppose method monkey kid pretty improve tank negative country';

module.exports = {
    contracts_directory: './src/main/solidity',
    migrations_directory: './src/main/javascript',
    networks: {
        ganache: {
            scheme: 'http',
            host: '127.0.0.1',
            port: 7545,
            network_id: '*',
            websockets: false,
            password: null
        },
        geth: {
            scheme: 'ws',
            host: '127.0.0.1',
            port: 8546,
            network_id: '*',
            websockets: true,
            password: 'bc3d'
        },
        infura: {
            scheme: 'https',
            host: 'ropsten.infura.io/v3/d9e4fcb5d7bc4fa0a9d8e52583062591',
            port: -1,
            network_id: '*',
            websockets: false,
            password: null,
            provider: () => new HDWalletProvider(
                mnemonic,
                'https://ropsten.infura.io/v3/d9e4fcb5d7bc4fa0a9d8e52583062591'
            )
        }
    }
};
