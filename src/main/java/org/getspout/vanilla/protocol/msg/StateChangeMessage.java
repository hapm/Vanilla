package org.getspout.vanilla.protocol.msg;

import org.getspout.api.protocol.Message;

public final class StateChangeMessage extends Message {
	private final byte state, gameMode;

	public StateChangeMessage(byte state, byte gameMode) {
		this.state = state;
		this.gameMode = gameMode;
	}

	public byte getState() {
		return state;
	}

	public byte getGameMode() {
		return gameMode;
	}

	@Override
	public String toString() {
		return "StateChangeMessage{state=" + state + ",gamemode=" + gameMode + "}";
	}
}