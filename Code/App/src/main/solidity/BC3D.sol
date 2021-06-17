pragma solidity ^0.5.7;

contract BC3D {
    // ======================================================
    // For measuring the time according to the data size.

    string[] private m_tsCommitAddresses;

    function tsClear() external {
        delete m_tsCommitAddresses;
    }

    function tsAddCommitAddress(
        string calldata _commitAddress
    ) external {
        // Make sure that the commit is new.
        //int commitIndex = tsFindCommitIndex(_commitAddress);
        //require(commitIndex < 0);

        // Store the commit address.
        m_tsCommitAddresses.push(_commitAddress);
    }

    function tsFindCommitIndex(
        string memory _commitAddress
    ) internal view returns (int) {
        bytes32 commitAddressHash = computeHash(_commitAddress);

        for (uint i = 0; i < m_tsCommitAddresses.length; i++) {
            if (computeHash(m_tsCommitAddresses[i]) == commitAddressHash) {
                return int(i);
            }
        }

        return 0 - 1;
    }

    function tsGetCommitAddress(
        int _index
    ) external view returns (string memory) {
        return m_tsCommitAddresses[uint(_index)];
    }

    function tsGetCommitAddressCount(
    ) external view returns (int) {
        return int(m_tsCommitAddresses.length);
    }

    // ======================================================
    // Types.

    struct Project {
        bytes32 id;
        string name;
        string keyword;
        address[] authorAddresses;
        string[] commitAddresses;
        int commitIncentiveSupply;
    }

    // ======================================================
    // Storage.

    Project[] private m_projects;

    // ======================================================
    // Events.

    // ======================================================
    // Public/External functions.

    // -------------------------------
    // Test functions.

    function testCall() external pure returns (string memory) {
        return "Ok";
    }

    function testStack() external payable {
        toPayable(address(this)).transfer(msg.value);
    }

    function testReceive() external {
        msg.sender.transfer(address(this).balance / 2);
    }

    // -------------------------------
    // Non-view functions.

    function createProject(string calldata _name, string calldata _keyword) external payable {
        int index = findProjectIndex(_name);
        require(index < 0);

        int initialCommitIncentiveSupply = int(msg.value);

        m_projects.push(
            Project(
                computeHash(_name),
                _name,
                _keyword,
                new address[](0),
                new string[](0),
                initialCommitIncentiveSupply
            )
        );

        toPayable(address(this)).transfer(uint(initialCommitIncentiveSupply));
    }

    function addAuthorAddressToProject(
        string calldata _projectName,
        address _authorAddress
    ) external {
        int projectIndex = findProjectIndex(_projectName);
        require(projectIndex >= 0);
        Project memory project = m_projects[uint(projectIndex)];

        // Store the author address only if the author address does not exist.
        int authorIndex = findAuthorIndex(project, _authorAddress);

        if (authorIndex < 0) {
            m_projects[uint(projectIndex)].authorAddresses.push(_authorAddress);
        }
    }

    function addCommitAddressToProject(
        string calldata _projectName,
        string calldata _commitAddress,
        int _commitIncentive
    ) external {
        int projectIndex = findProjectIndex(_projectName);
        require(projectIndex >= 0);
        Project memory project = m_projects[uint(projectIndex)];

        // Make sure that the commit is new.
        int commitIndex = findCommitIndex(project, _commitAddress);
        require(commitIndex < 0);

        // Store the commit address.
        m_projects[uint(projectIndex)].commitAddresses.push(_commitAddress);

        // Give the commit incentive.
        require(_commitIncentive >= 0);
        m_projects[uint(projectIndex)].commitIncentiveSupply -= _commitIncentive;
        msg.sender.transfer(uint(_commitIncentive));
    }

    // -------------------------------
    // View functions.

    function getProjectCount() external view returns (int) {
        return int(m_projects.length);
    }

    function getNameOfProject(
        int _projectIndex
    ) external view returns (string memory) {
        return m_projects[uint(_projectIndex)].name;
    }

    function getKeywordOfProject(
        string calldata _projectName
    ) external view returns (string memory) {
        int projectIndex = findProjectIndex(_projectName);
        require(projectIndex >= 0);

        return m_projects[uint(projectIndex)].keyword;
    }

    function getAuthorAddressesOfProject(
        string calldata _projectName
    ) external view returns (address[] memory) {
        int projectIndex = findProjectIndex(_projectName);
        require(projectIndex >= 0);

        return m_projects[uint(projectIndex)].authorAddresses;
    }

    function getCommitAddressOfProject(
        string calldata _projectName,
        int _index
    ) external view returns (string memory) {
        int projectIndex = findProjectIndex(_projectName);
        require(projectIndex >= 0);

        return m_projects[uint(projectIndex)].commitAddresses[uint(_index)];
    }

    function getCommitAddressCountOfProject(
        string calldata _projectName
    ) external view returns (int) {
        int projectIndex = findProjectIndex(_projectName);
        require(projectIndex >= 0);

        return int(m_projects[uint(projectIndex)].commitAddresses.length);
    }

    function getCommitIncentiveSupply(
        string calldata _projectName
    ) external view returns (int) {
        int projectIndex = findProjectIndex(_projectName);
        require(projectIndex >= 0);

        return m_projects[uint(projectIndex)].commitIncentiveSupply;
    }

    // -------------------------------
    // Fallback function(s).

    function() external payable {
    }

    // ======================================================
    // Private/Internal functions.

    function findProjectIndex(
        string memory _name
    ) internal view returns (int) {
        bytes32 id = computeHash(_name);

        for (uint i = 0; i < m_projects.length; i++) {
            if (m_projects[i].id == id) {
                return int(i);
            }
        }

        return 0 - 1;
    }

    function findAuthorIndex(
        Project memory _project,
        address _author
    ) internal pure returns (int) {
        for (uint i = 0; i < _project.authorAddresses.length; i++) {
            if (_project.authorAddresses[i] == _author) {
                return int(i);
            }
        }

        return 0 - 1;
    }

    function findCommitIndex(
        Project memory _project,
        string memory _commitAddress
    ) internal pure returns (int) {
        bytes32 commitAddressHash = computeHash(_commitAddress);

        for (uint i = 0; i < _project.commitAddresses.length; i++) {
            if (computeHash(_project.commitAddresses[i]) == commitAddressHash) {
                return int(i);
            }
        }

        return 0 - 1;
    }

    function computeHash(
        string memory _value
    ) private pure returns (bytes32) {
        return keccak256(abi.encode(_value));
    }

    function toPayable(
        address _address
    ) private pure returns (address payable) {
        return address(uint160(_address));
    }
}
