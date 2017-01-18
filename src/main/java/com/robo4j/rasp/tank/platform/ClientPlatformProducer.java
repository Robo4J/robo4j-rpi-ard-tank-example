/*
 * Copyright (C) 2016-2017, Miroslav Wengner, Marcus Hirt
 * This ClientPlatformProducer.java  is part of robo4j.
 * module: robo4j-rpi-ard-tank-example
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.rasp.tank.platform;

import java.util.concurrent.Exchanger;
import java.util.concurrent.LinkedBlockingQueue;

import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.command.PlatformUnitCommandEnum;
import com.robo4j.commons.concurrent.CoreBusQueue;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.core.platform.ClientPlatformException;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 17.12.2016
 */
// TODO: move to generic
public class ClientPlatformProducer implements AgentProducer, Runnable {

	private LinkedBlockingQueue<GenericCommand<PlatformUnitCommandEnum>> commandQueue;
	private Exchanger<GenericCommand<PlatformUnitCommandEnum>> exchanger;

	public ClientPlatformProducer(final LinkedBlockingQueue<GenericCommand<PlatformUnitCommandEnum>> commandQueue,
			final Exchanger<GenericCommand<PlatformUnitCommandEnum>> exchanger) {
		this.commandQueue = commandQueue;
		this.exchanger = exchanger;
	}

	@Override
	public CoreBusQueue getMessageQueue() {
		return null;
	}

	@Override
	public void run() {

		GenericCommand<PlatformUnitCommandEnum> command = null;
		try {
			command = commandQueue.take();
			exchanger.exchange(command);
		} catch (InterruptedException e) {
			throw new ClientPlatformException("Platform PRODUCER e", e);
		} finally {
			SimpleLoggingUtil.print(getClass(), "ClientPlatformProducer exchanged= " + command);
		}
	}

}
