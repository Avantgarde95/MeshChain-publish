const net = require('net');
const fs = require('fs');
const Web3 = require('web3');
const truffleConfig = require('../../../truffle-config');

const Migrations = artifacts.require('Migrations');

module.exports = function (deployer, networkName) {
    if (networkName === 'infura-fork') {
        networkName = 'infura';
    }

    const network = truffleConfig.networks[networkName];
    const web3 = new Web3();
    const configurationJSON = fs.readFileSync('Configuration.json', 'utf8');
    const configuration = JSON.parse(configurationJSON);
    const userAddress = configuration.userAddress;

    let blockchainURI = network.scheme + '://' + network.host;

    if (network.port >= 0) {
        blockchainURI += ':' + network.port;
    }

    if (blockchainURI.startsWith('ws')) {
        web3.setProvider(new Web3.providers.WebsocketProvider(blockchainURI));
    } else if (blockchainURI.startsWith('http')) {
        web3.setProvider(new Web3.providers.HttpProvider(blockchainURI));
    } else {
        web3.setProvider(new Web3.providers.IpcProvider(network.host, net));
    }

    if (network.password !== null) {
        console.log('   > Unlocking the account to migrate the contracts.');

        (async () => {
            await web3.eth.personal.unlockAccount(
                userAddress,
                network.password
            );
        })().catch(e => {
            throw e;
        });
    }

    deployer.deploy(Migrations);
};
