// SPDX-License-Identifier: Apache-2.0
/*
 *   v1.0.0
 *   proxy contract for WeCross
 *   main entrance of all contract call
 */

pragma solidity >=0.5.0 <0.8.20;

contract WeCrossProxy {
    string constant version = "v1.0.0";

    function getVersion() public pure
    returns (string memory) {
        return version;
    }

    event LuyuSendTransaction(
        string path,
        string method,
        string[] args,
        uint256 nonce,
        string luyuIdentity,
        string callbackMethod,
        address sender
    );

    event LuyuCall(
        string path,
        string method,
        string[] args,
        uint256 nonce,
        string luyuIdentity,
        string callbackMethod,
        address sender
    );

    function constantCall(string memory path, string memory method, string[] memory args, uint256 uid, string memory identity, string memory callbackMethod) public returns (uint256) {
        emit LuyuCall(
            path,
            method,
            args,
            uid,
            identity,
            callbackMethod,
            tx.origin
        );
        return uid;
    }

    function sendTransaction(string memory path, string memory method, string[] memory args, uint256 uid, string memory identity, string memory callbackMethod) public returns (uint256)  {
        emit LuyuSendTransaction(
            path,
            method,
            args,
            uid,
            identity,
            callbackMethod,
            tx.origin
        );
        return uid;
    }
}
