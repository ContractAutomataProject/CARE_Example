package io.github.contractautomata.care.exampleWithoutCare;

import java.net.SocketAddress;

public class ContractViolationExceptionExample extends RuntimeException {

	//remote host causing the violation of the contract, for accountability
	private final SocketAddress remote;

	
	public ContractViolationExceptionExample(SocketAddress remote) {
		super();
		this.remote = remote;
	}


	public SocketAddress getRemote() {
		return remote;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
