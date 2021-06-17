const fs = require('fs');
const truffleConfig = require('../../../truffle-config');

const BC3D = artifacts.require('BC3D');

module.exports = function (deployer, networkName) {
    if (networkName === 'infura-fork') {
        networkName = 'infura';
    }

    const configurationJSON = fs.readFileSync('Configuration.json', 'utf8');
    const configuration = JSON.parse(configurationJSON);

    deployer.deploy(BC3D)
        .then(() => BC3D.deployed())
        .then(instance => {
            console.log('   > Saving contract information to Configuration.json.');

            const network = truffleConfig.networks[networkName];

            configuration.contractAddress = instance.address;
            configuration.userPassword = network.password;
            configuration.blockchainURI.scheme = network.scheme;
            configuration.blockchainURI.host = network.host;
            configuration.blockchainURI.port = network.port;

            const newConfigurationJSON = JSON.stringify(configuration, null, 4);
            fs.writeFileSync('Configuration.json', newConfigurationJSON, 'utf8');
        });
};
