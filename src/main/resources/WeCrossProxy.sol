pragma solidity >=0.5.0 <0.8.20;
pragma experimental ABIEncoderV2;

contract WeCrossProxy {
    string constant private version = "v1.0.0";

    struct XATransactionStep {
        string accountIdentity;
        uint256 timestamp;
        string path;
        address contractAddress;
        string func;
        bytes args;
    }

    struct XATransaction {
        string accountIdentity;
        string[] paths; // all paths related to this transaction
        address[] contractAddresses; // locked addressed in current chain
        string status; // processing | committed |  rolledback
        uint256 startTimestamp;
        uint256 commitTimestamp;
        uint256 rollbackTimestamp;
        uint256[] seqs; // sequence of each step
        uint256 stepNum; // step number
    }

    struct ContractStatus {
        bool locked; // isolation control, read-committed
        string xaTransactionID;
    }

    mapping(string => address) private cnsContracts;  // key: contractName
    mapping(address => ContractStatus) private lockedContracts;  // key: contractAddress
    mapping(string => XATransaction) private xaTransactions; // key: xaTransactionID
    mapping(string => XATransactionStep) private xaTransactionSteps; // key: xaTransactionID || xaTransactionSeq
    mapping(string => Transaction) private transactions; // key: uniqueID
    /*
     * record all xa transactionIDs
     * head: point to the current xa transaction to be checked
     * tail: point to the next position for added xa transaction
     */
    uint256 private head = 0;
    uint256 private tail = 0;
    string[] private xaTransactionIDs;

    string constant private XA_STATUS_PROCESSING = "processing";
    string constant private XA_STATUS_COMMITTED = "committed";
    string constant private XA_STATUS_ROLLEDBACK = "rolledback";

    string constant private REVERT_FLAG = "_revert";
    string constant private NULL_FLAG = "null";
    string constant private SUCCESS_FLAG = "success";

    bytes1 constant private SEPARATOR = ".";

    uint256 constant private MAX_STEP = 1024;

    string[] private resourceCache;

    struct Transaction {
        bool existed;
        bytes result;
    }

    function getVersion() public pure
    returns (string memory) {
        return version;
    }

    function addResource(string memory _name) public {
        resourceCache.push(_name);
    }

    function getResources() public view
    returns (string[] memory) {
        return resourceCache;
    }

    function deleteResources() public {
        delete resourceCache;
    }

    function deployContract(bytes memory _bin) internal
    returns (address _addr) {
        bool ok = false;
        assembly {
            _addr := create(0, add(_bin, 0x20), mload(_bin))
            ok := gt(extcodesize(_addr), 0)
        }
        if (!ok) {
            revert("deploy contract failed");
        }
    }

    function deployContractWithRegisterCNS(string memory _path, bytes memory _bin) public
    returns (address) {
        string memory name = getNameByPath(_path);
        address addr = getAddressByName(name, false);
        if ((addr != address(0x0)) && lockedContracts[addr].locked) {
            revert(string(abi.encodePacked(name, " is locked by unfinished xa transaction: ", lockedContracts[addr].xaTransactionID)));
        }

        address deploy_addr = deployContract(_bin);

        cnsContracts[name] = deploy_addr;

        if (addr == address(0x0)) {
            addResource(name);
        }
        return deploy_addr;
    }

    function registerCNS(string memory _path, address _addr) public {
        string memory name = getNameByPath(_path);
        address addr = getAddressByName(name, false);
        if ((addr != address(0x0)) && lockedContracts[addr].locked) {
            revert(string(abi.encodePacked(name, " is locked by unfinished xa transaction: ", lockedContracts[addr].xaTransactionID)));
        }

        cnsContracts[name] = _addr;

        if (addr == address(0x0)) {
            addResource(name);
        }
    }

    function selectByName(string memory _name) public view
    returns (address) {
        return cnsContracts[_name];
    }

    function constantCall(string memory _name, bytes memory _argsWithMethodId) public
    returns (bytes memory){
        address addr = getAddressByName(_name, true);

        if (lockedContracts[addr].locked) {
            revert(
                string(abi.encodePacked("resource is locked by unfinished xa transaction: ", lockedContracts[addr].xaTransactionID))
            );
        }

        return callContract(addr, _argsWithMethodId);
    }

    function constantCallWithXa(string memory _XATransactionID, string memory _path, string memory _func, bytes memory _args) public
    returns (bytes memory) {
        address addr = getAddressByPath(_path, true);

        if (!isExistedXATransaction(_XATransactionID)) {
            revert("xa transaction not found");
        }

        if (!sameString(lockedContracts[addr].xaTransactionID, _XATransactionID)) {
            revert(
                string(abi.encodePacked(_path, " is unregistered in xa transaction: ", _XATransactionID))
            );
        }

        return callContract(addr, _func, _args);
    }

    function sendTransaction(string memory _uid, string memory _name, bytes memory _argsWithMethodId) public
    returns (bytes memory) {
        if (transactions[_uid].existed) {
            return transactions[_uid].result;
        }

        address addr = getAddressByName(_name, true);

        if (lockedContracts[addr].locked) {
            revert(
                string(abi.encodePacked(_name, " is locked by unfinished xa transaction: ", lockedContracts[addr].xaTransactionID))
            );
        }

        bytes memory result = callContract(addr, _argsWithMethodId);

        transactions[_uid] = Transaction(true, result);
        return result;
    }

    function sendTransactionWithXa(string memory _uid, string memory _XATransactionID, uint256 _XATransactionSeq, string memory _path, string memory _func, bytes memory _args) public
    returns (bytes memory) {
        if (transactions[_uid].existed) {
            return transactions[_uid].result;
        }

        address addr = getAddressByPath(_path, true);

        if (!isExistedXATransaction(_XATransactionID)) {
            revert("xa transaction not found");
        }

        if (sameString(xaTransactions[_XATransactionID].status, XA_STATUS_COMMITTED)) {
            revert("xa transaction has been committed");
        }

        if (sameString(xaTransactions[_XATransactionID].status, XA_STATUS_ROLLEDBACK)) {
            revert("xa transaction has been rolledback");
        }

        if (!sameString(lockedContracts[addr].xaTransactionID, _XATransactionID)) {
            revert(
                string(abi.encodePacked(_path, " is unregistered in xa transaction ", _XATransactionID))
            );
        }

        if (!isValidXATransactionSep(_XATransactionID, _XATransactionSeq)) {
            revert("seq should be greater than before");
        }

        // recode step
        xaTransactionSteps[getXATransactionStepKey(_XATransactionID, _XATransactionSeq)] = XATransactionStep(
            addressToString(tx.origin),
            block.timestamp / 1000,
            _path,
            addr,
            _func,
            _args
        );

        // recode seq
        uint256 num = xaTransactions[_XATransactionID].stepNum;
        xaTransactions[_XATransactionID].seqs[num] = _XATransactionSeq;
        xaTransactions[_XATransactionID].stepNum = num + 1;

        bytes memory result = callContract(addr, _func, _args);

        // recode transaction
        transactions[_uid] = Transaction(true, result);
        return result;
    }

    function startXATransaction(string memory _xaTransactionID, string[] memory _selfPaths, string[] memory _otherPaths) public
    returns (string memory) {
        if (isExistedXATransaction(_xaTransactionID)) {
            revert(string(abi.encodePacked("xa transaction ", _xaTransactionID, " already exists")));
        }

        uint256 selfLen = _selfPaths.length;
        uint256 otherLen = _otherPaths.length;

        address[] memory contracts = new address[](selfLen);
        string[] memory allPaths = new string[](selfLen + otherLen);

        for (uint256 i = 0; i < selfLen; i++) {
            address addr = getAddressByPath(_selfPaths[i], true);
            contracts[i] = addr;
            if (lockedContracts[addr].locked) {
                revert(string(abi.encodePacked(_selfPaths[i], " is locked by unfinished xa transaction: ", lockedContracts[addr].xaTransactionID)));
            }
            lockedContracts[addr].locked = true;
            lockedContracts[addr].xaTransactionID = _xaTransactionID;
            allPaths[i] = _selfPaths[i];
        }

        for (uint256 i = 0; i < otherLen; i++) {
            allPaths[selfLen + i] = _otherPaths[i];
        }

        uint256[] memory seqs = new uint256[](MAX_STEP);

        xaTransactions[_xaTransactionID] = XATransaction(
            addressToString(tx.origin),
            allPaths,
            contracts,
            XA_STATUS_PROCESSING,
            block.timestamp / 1000,
            0,
            0,
            seqs,
            0
        );

        addXATransaction(_xaTransactionID);

        return SUCCESS_FLAG;
    }

    function commitXATransaction(string memory _xaTransactionID) public
    returns (string memory)
    {
        if (!isExistedXATransaction(_xaTransactionID)) {
            revert("xa transaction not found");
        }

        if (sameString(xaTransactions[_xaTransactionID].status, XA_STATUS_COMMITTED)) {
            revert("xa transaction has been committed");
        }

        // has rolledback
        if (sameString(xaTransactions[_xaTransactionID].status, XA_STATUS_ROLLEDBACK)) {
            revert("xa transaction has been rolledback");
        }

        xaTransactions[_xaTransactionID].commitTimestamp = block.timestamp / 1000;
        xaTransactions[_xaTransactionID].status = XA_STATUS_COMMITTED;
        deleteLockedContracts(_xaTransactionID);
        head++;

        return SUCCESS_FLAG;
    }


    function rollbackXATransaction(string memory _xaTransactionID) public
    returns (string memory) {
        string memory result = SUCCESS_FLAG;
        if (!isExistedXATransaction(_xaTransactionID)) {
            revert("xa transaction not found");
        }

        if (sameString(xaTransactions[_xaTransactionID].status, XA_STATUS_COMMITTED)) {
            revert("xa transaction has been committed");
        }

        if (sameString(xaTransactions[_xaTransactionID].status, XA_STATUS_ROLLEDBACK)) {
            revert("xa transaction has been rolledback");
        }

        string memory message = "warning:";
        uint256 stepNum = xaTransactions[_xaTransactionID].stepNum;
        for (uint256 i = stepNum; i > 0; i--) {
            uint256 seq = xaTransactions[_xaTransactionID].seqs[i - 1];
            string memory key = getXATransactionStepKey(_xaTransactionID, seq);

            string memory func = xaTransactionSteps[key].func;
            address contractAddress = xaTransactionSteps[key].contractAddress;
            bytes memory args = xaTransactionSteps[key].args;

            bytes memory sig = abi.encodeWithSignature(
                getRevertFunc(func, REVERT_FLAG)
            );
            bool success;
            (success,) = address(contractAddress).call(
                abi.encodePacked(sig, args)
            );
            if (!success) {
                message = string(
                    abi.encodePacked(message, ' revert "', func, '" failed.')
                );
                result = message;
            }
        }

        xaTransactions[_xaTransactionID].rollbackTimestamp = block.timestamp / 1000;
        xaTransactions[_xaTransactionID].status = XA_STATUS_ROLLEDBACK;
        deleteLockedContracts(_xaTransactionID);
        return result;
    }

    function getXATransactionNumber() public view
    returns (string memory) {
        if (xaTransactionIDs.length == 0) {
            return "0";
        } else {
            return uint256ToString(xaTransactionIDs.length);
        }
    }

    /*
    * traverse in reverse order
    * outputs:
    {
        "total": 100,
        "xaTransactions":
        [
            {
            	"xaTransactionID": "001",
        		"accountIdentity": "0x11",
        		"status": "processing",
        		"timestamp": 123,
        		"paths": ["a.b.1","a.b.2"]
        	},
        	{
            	"xaTransactionID": "002",
        		"accountIdentity": "0x11",
        		"status": "committed",
        		"timestamp": 123,
        		"paths": ["a.b.1","a.b.2"]
        	}
        ]
    }
    */
    function listXATransactions(string memory _index, uint256 _size) public view
    returns (string memory) {
        uint256 len = xaTransactionIDs.length;
        uint256 index = sameString("-1", _index) ? (len - 1) : stringToUint256(_index);

        if (len == 0 || len <= index) {
            return '{"total":0,"xaTransactions":[]}';
        }

        string memory jsonStr = "[";
        for (uint256 i = 0; i < (_size - 1) && (index - i) > 0; i++) {
            string memory xaTransactionID = xaTransactionIDs[index - i];
            jsonStr = string(
                abi.encodePacked(
                    jsonStr,
                    '{"xaTransactionID":"',
                    xaTransactionID,
                    '",',
                    '"accountIdentity":"',
                    xaTransactions[xaTransactionID].accountIdentity,
                    '",',
                    '"status":"',
                    xaTransactions[xaTransactionID].status,
                    '",',
                    '"paths":',
                    pathsToJson(xaTransactionID),
                    ",",
                    '"timestamp":',
                    uint256ToString(
                        xaTransactions[xaTransactionID].startTimestamp
                    ),
                    "},"
                )
            );
        }

        uint256 lastIndex = (index + 1) >= _size ? (index + 1 - _size) : 0;
        string memory xaTransactionID = xaTransactionIDs[lastIndex];
        jsonStr = string(
            abi.encodePacked(
                jsonStr,
                '{"xaTransactionID":"',
                xaTransactionID,
                '",',
                '"accountIdentity":"',
                xaTransactions[xaTransactionID].accountIdentity,
                '",',
                '"status":"',
                xaTransactions[xaTransactionID].status,
                '",',
                '"paths":',
                pathsToJson(xaTransactionID),
                ",",
                '"timestamp":',
                uint256ToString(xaTransactions[xaTransactionID].startTimestamp),
                "}]"
            )
        );

        return
            string(
            abi.encodePacked(
                '{"total":',
                uint256ToString(len),
                ',"xaTransactions":',
                jsonStr,
                "}"
            )
        );
    }

    /*
    *  @param xaTransactionID
    * result with json form
    * example:
    {
    	"xaTransactionID": "1",
    	"accountIdentity": "0x88",
    	"status": "processing",
    	"paths":["a.b.c1","a.b.c2","a.b1.c3"],
    	"startTimestamp": 123,
    	"commitTimestamp": 456,
    	"rollbackTimestamp": 0,
    	"xaTransactionSteps": [{
    	        "accountIdentity":"0x12",
            	"xaTransactionSeq": 233,
    			"path": "a.b.c1",
    			"timestamp": 233,
    			"method": "set",
    			"args": "0010101"
    		},
    		{
    		    "accountIdentity":"0x12",
    		    "xaTransactionSeq": 244,
    			"path": "a.b.c2",
    			"timestamp": 244,
    			"method": "set",
    			"args": "0010101"
    		}
    	]
    }
    */
    function getXATransaction(string memory _xaTransactionID) public view
    returns (string memory) {
        if (!isExistedXATransaction(_xaTransactionID)) {
            revert("xa transaction not found");
        }

        return
            string(
            abi.encodePacked(
                '{"xaTransactionID":"',
                _xaTransactionID,
                '",',
                '"accountIdentity":"',
                xaTransactions[_xaTransactionID].accountIdentity,
                '",',
                '"status":"',
                xaTransactions[_xaTransactionID].status,
                '",',
                '"paths":',
                pathsToJson(_xaTransactionID),
                ",",
                '"startTimestamp":',
                uint256ToString(
                    xaTransactions[_xaTransactionID].startTimestamp
                ),
                ",",
                '"commitTimestamp":',
                uint256ToString(
                    xaTransactions[_xaTransactionID].commitTimestamp
                ),
                ",",
                '"rollbackTimestamp":',
                uint256ToString(
                    xaTransactions[_xaTransactionID].rollbackTimestamp
                ),
                ",",
                '"xaTransactionSteps":',
                xaTransactionStepArrayToJson(
                    _xaTransactionID,
                    xaTransactions[_xaTransactionID].seqs,
                    xaTransactions[_xaTransactionID].stepNum
                ),
                "}"
            )
        );
    }

    // called by router to check xa transaction status
    function getLatestXATransaction() public view
    returns (string memory) {
        string memory xaTransactionID;
        if (head == tail) {
            return "{}";
        } else {
            xaTransactionID = xaTransactionIDs[uint256(head)];
        }
        return getXATransaction(xaTransactionID);
    }

    // called by router to rollback transaction
    function rollbackAndDeleteXATransactionTask(string memory _xaTransactionID) public
    returns (string memory) {
        rollbackXATransaction(_xaTransactionID);
        return deleteXATransactionTask(_xaTransactionID);
    }

    function getLatestXATransactionID() public view
    returns (string memory) {
        if (head == tail) {
            return NULL_FLAG;
        } else {
            return xaTransactionIDs[uint256(head)];
        }
    }

    function getXATransactionState(string memory _path) public view
    returns (string memory) {
        address addr = getAddressByPath(_path, true);
        if (!lockedContracts[addr].locked) {
            return NULL_FLAG;
        } else {
            string memory xaTransactionID = lockedContracts[addr]
                .xaTransactionID;
            uint256 index = xaTransactions[xaTransactionID].stepNum;
            uint256 seq = index == 0 ? 0 : xaTransactions[xaTransactionID].seqs[index - 1];
            return string(abi.encodePacked(xaTransactionID, " ", uint256ToString(seq)));
        }
    }

    function addXATransaction(string memory _xaTransactionID) internal {
        tail++;
        xaTransactionIDs.push(_xaTransactionID);
    }

    function deleteXATransactionTask(string memory _xaTransactionID) internal
    returns (string memory) {
        if (head == tail) {
            revert("delete nonexistent xa transaction");
        }

        if (!sameString(xaTransactionIDs[head], _xaTransactionID)) {
            revert("delete unmatched xa transaction");
        }

        head++;
        return SUCCESS_FLAG;
    }

    function callContract(address _contractAddress, string memory _sig, bytes memory _args) internal
    returns (bytes memory result) {
        bytes memory sig = abi.encodeWithSignature(_sig);
        bool success;
        (success, result) = address(_contractAddress).call(
            abi.encodePacked(sig, _args)
        );
        if (!success) {
            revert(string(result));
        }
    }

    function callContract(address _contractAddress, bytes memory _argsWithMethodId) internal
    returns (bytes memory result) {
        bool success;
        (success, result) = address(_contractAddress).call(_argsWithMethodId);
        if (!success) {
            //(string memory error) = abi.decode(result, (string));
            revert(string(result));
        }
    }

    function getAddressByName(string memory _name, bool _revertNotExist) internal view
    returns (address _address) {
        _address = cnsContracts[_name];
        if (_address == address(0x0)) {
            if (_revertNotExist) {
                revert("the name's address not exist.");
            }
        }
    }

    function getAddressByPath(string memory _path, bool _revertNotExist) internal view
    returns (address) {
        string memory name = getNameByPath(_path);
        return getAddressByName(name, _revertNotExist);
    }

    // input must be a valid path like "zone.chain.resource"
    function getNameByPath(string memory _path) internal pure
    returns (string memory) {
        bytes memory path = bytes(_path);
        uint256 len = path.length;
        uint256 nameLen = 0;
        uint256 index = 0;
        for (uint256 i = len - 1; i > 0; i--) {
            if (path[i] == SEPARATOR) {
                index = i + 1;
                break;
            } else {
                nameLen++;
            }
        }

        bytes memory name = new bytes(nameLen);
        for (uint256 i = 0; i < nameLen; i++) {
            name[i] = path[index++];
        }

        return string(name);
    }

    /*
        ["a.b.c1", "a.b.c2"]
    */
    function pathsToJson(string memory _transactionID) internal view
    returns (string memory) {
        uint256 len = xaTransactions[_transactionID].paths.length;
        string memory paths = string(
            abi.encodePacked('["', xaTransactions[_transactionID].paths[0], '"')
        );
        for (uint256 i = 1; i < len; i++) {
            paths = string(
                abi.encodePacked(
                    paths,
                    ',"',
                    xaTransactions[_transactionID].paths[i],
                    '"'
                )
            );
        }
        return string(abi.encodePacked(paths, "]"));
    }

    /*
    [
        {
    	    "accountIdentity":"0x12",
            "xaTransactionSeq": 233,
    		"path": "a.b.c1",
    		"timestamp": 233,
    		"method": "set",
    		"args": "0010101"
    	},
        {
    	    "accountIdentity":"0x12",
            "xaTransactionSeq": 233,
    		"path": "a.b.c1",
    		"timestamp": 233,
    		"method": "set",
    		"args": "0010101"
    	}
    ]
    */
    function xaTransactionStepArrayToJson(string memory _transactionID, uint256[] memory _seqs, uint256 _len) internal view
    returns (string memory result) {
        if (_len == 0) {
            return "[]";
        }

        result = string(
            abi.encodePacked("[", xaTransactionStepToJson(xaTransactionSteps[getXATransactionStepKey(_transactionID, _seqs[0])], _seqs[0]))
        );
        for (uint256 i = 1; i < _len; i++) {
            result = string(
                abi.encodePacked(result, ",", xaTransactionStepToJson(xaTransactionSteps[getXATransactionStepKey(_transactionID, _seqs[i])], _seqs[i]))
            );
        }

        return string(abi.encodePacked(result, "]"));
    }

    /*
    {
        "xaTransactionSeq": 233,
        "accountIdentity":"0x12",
		"path": "a.b.c1",
		"timestamp": 233,
		"method": "set",
		"args": "0010101"
	}
    */
    function xaTransactionStepToJson(XATransactionStep memory _xaTransactionStep, uint256 _XATransactionSeq) internal pure
    returns (string memory) {
        return
            string(
            abi.encodePacked(
                '{"xaTransactionSeq":',
                uint256ToString(_XATransactionSeq),
                ",",
                '"accountIdentity":"',
                _xaTransactionStep.accountIdentity,
                '",',
                '"path":"',
                _xaTransactionStep.path,
                '",',
                '"timestamp":',
                uint256ToString(_xaTransactionStep.timestamp),
                ",",
                '"method":"',
                getMethodFromFunc(_xaTransactionStep.func),
                '",',
                '"args":"',
                bytesToHexString(_xaTransactionStep.args),
                '"}'
            )
        );
    }

    function isExistedXATransaction(string memory _xaTransactionID) internal view
    returns (bool) {
        return xaTransactions[_xaTransactionID].startTimestamp != 0;
    }

    function isValidXATransactionSep(string memory _xaTransactionID, uint256 _XATransactionSeq) internal view
    returns (bool) {
        uint256 index = xaTransactions[_xaTransactionID].stepNum;
        return (index == 0) || (_XATransactionSeq > xaTransactions[_xaTransactionID].seqs[index - 1]);
    }

    function deleteLockedContracts(string memory _xaTransactionID) internal {
        uint256 len = xaTransactions[_xaTransactionID].contractAddresses.length;
        for (uint256 i = 0; i < len; i++) {
            address contractAddress = xaTransactions[_xaTransactionID].contractAddresses[i];
            delete lockedContracts[contractAddress];
        }
    }

    // func(string,uint256) => func_flag(string,uint256)
    function getRevertFunc(string memory _func, string memory _revertFlag) internal pure
    returns (string memory) {
        bytes memory funcBytes = bytes(_func);
        bytes memory flagBytes = bytes(_revertFlag);
        uint256 funcLen = funcBytes.length;
        uint256 flagLen = flagBytes.length;
        bytes memory newFunc = new bytes(funcLen + flagLen);

        bytes1 c = bytes1("(");
        uint256 index = 0;
        uint256 point = 0;

        for (uint256 i = 0; i < funcLen; i++) {
            if (funcBytes[i] != c) {
                newFunc[index++] = funcBytes[i];
            } else {
                point = i;
                break;
            }
        }

        for (uint256 i = 0; i < flagLen; i++) {
            newFunc[index++] = flagBytes[i];
        }

        for (uint256 i = point; i < funcLen; i++) {
            newFunc[index++] = funcBytes[i];
        }

        return string(newFunc);
    }

    // func(string,uint256) => func
    function getMethodFromFunc(string memory _func) internal pure
    returns (string memory) {
        bytes memory funcBytes = bytes(_func);
        uint256 funcLen = funcBytes.length;
        bytes memory temp = new bytes(funcLen);

        bytes1 c = bytes1("(");
        uint256 index = 0;

        for (uint256 i = 0; i < funcLen; i++) {
            if (funcBytes[i] != c) {
                temp[index++] = funcBytes[i];
            } else {
                break;
            }
        }

        bytes memory result = new bytes(index);
        for (uint256 i = 0; i < index; i++) {
            result[i] = temp[i];
        }

        return string(result);
    }

    function getXATransactionStepKey(string memory _transactionID, uint256 _transactionSeq) internal pure
    returns (string memory) {
        return
            string(
            abi.encodePacked(
                _transactionID,
                uint256ToString(_transactionSeq)
            )
        );
    }

    function sameString(string memory _str1, string memory _str2) internal pure
    returns (bool) {
        return keccak256(bytes(_str1)) == keccak256(bytes(_str2));
    }

    function stringToUint256(string memory _str) internal pure
    returns (uint256) {
        bytes memory bts = bytes(_str);
        uint256 result = 0;
        uint256 len = bts.length;
        for (uint256 i = 0; i < len; i++) {
            if (uint8(bts[i]) >= 48 && uint8(bts[i]) <= 57) {
                result = result * 10 + (uint8(bts[i]) - 48);
            }
        }
        return result;
    }

    function uint256ToString(uint256 _value) internal pure
    returns (string memory) {
        bytes32 result;
        if (_value == 0) {
            return "0";
        } else {
            while (_value > 0) {
                result = bytes32(uint256(result) / (2 ** 8));
                result |= bytes32(((_value % 10) + 48) * 2 ** (8 * 31));
                _value /= 10;
            }
        }
        return bytes32ToString(result);
    }

    function bytesToHexString(bytes memory _bts) internal pure
    returns (string memory result) {
        uint256 len = _bts.length;
        bytes memory s = new bytes(len * 2);
        for (uint256 i = 0; i < len; i++) {
            bytes1 befor = bytes1(_bts[i]);
            bytes1 high = bytes1(uint8(befor) / 16);
            bytes1 low = bytes1(uint8(befor) - 16 * uint8(high));
            s[i * 2] = convert(high);
            s[i * 2 + 1] = convert(low);
        }
        result = string(s);
    }

    function bytes32ToString(bytes32 _bts32) internal pure
    returns (string memory) {
        bytes memory result = new bytes(_bts32.length);

        uint256 len = _bts32.length;
        for (uint256 i = 0; i < len; i++) {
            result[i] = _bts32[i];
        }

        return string(result);
    }

    function addressToString(address _addr) internal pure
    returns (string memory) {
        bytes memory result = new bytes(40);
        for (uint256 i = 0; i < 20; i++) {
            bytes1 temp = bytes1(uint8(uint160(_addr) / (2 ** (8 * (19 - i)))));
            bytes1 b1 = bytes1(uint8(temp) / 16);
            bytes1 b2 = bytes1(uint8(temp) - 16 * uint8(b1));
            result[2 * i] = convert(b1);
            result[2 * i + 1] = convert(b2);
        }

        return string(abi.encodePacked("0x", string(result)));
    }

    function convert(bytes1 _b) internal pure
    returns (bytes1) {
        if (uint8(_b) < 10) {
            return bytes1(uint8(_b) + 0x30);
        } else {
            return bytes1(uint8(_b) + 0x57);
        }
    }
}
