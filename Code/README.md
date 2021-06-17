# MeshChain

### Requirements
- JDK >= 1.8
- Python >= 3.7
- Node.js >= 8.11.3
- [Ganache](https://www.trufflesuite.com/ganache) >= 2.1.2
  - Necessary when you want to simulate the Ethereum network on the single computer.
- [Geth(Go Ethereum)](https://geth.ethereum.org/) >= 1.9.7
  - Necessary when you want to construct a private Ethereum network or connect to existing Ethereum network.

### Components
#### App
- Implementation of the application.

#### Plugin
- Plugin for the modeling tools to connect to the application.

#### Network
- Scripts for constructing a private Ethereum network for testing.

### How to use
#### App
- Install Node.js dependencies: `gradlew bcInstall`
- Compile the contracts: `gradlew bcCompile`
  - You have to run this first before compiling the application.
- Migrate(Upload) the contracts on the blockchain
  - On Ganache: `gradlew bcMigrateGanache`
  - On Geth: `gradlew bcMigrateGeth`
- Run the application: `gradlew bcRun`
- Pack the application to executable jar: `gradlew bcPack`

#### Plugin
- Run the plugin: `run`

#### Network
- Create a new account: `python3 createAccount.py`
- Create genesis.json: `python3 createGenesis.py`
- Initialize the (Geth) network: `python3 createGethNetwork.py`
- Run the Geth network: `python3 runGethNetwork.py (network) (account index)`
- Run the Swarm network: `python3 runSwarmNetwork.py (network) (account index)`
- Remove the account files (key files): `python3 removeAccount.py`
- Remove the network files (chain data): `python3 removeNetwork.py (network)`

(`network`: `private` or `testnet`, `account index`: 0, 1, 2, ...)

### Git issue
- If you get an error while cloning this repository on Windows, try:
  `git config --global credential.helper wincred`
  (Solution from [here](https://github.com/git-lfs/git-lfs/issues/1485))

