/*
 * Copyright (C) 2016-2017, Miroslav Wengner, Marcus Hirt
 * This ClientPlatformConsumer.java  is part of robo4j.
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

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.command.PlatformUnitCommandEnum;
import com.robo4j.commons.concurrent.CoreBusQueue;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.motor.GenericMotor;
import com.robo4j.commons.motor.MotorRotationEnum;
import com.robo4j.core.platform.AbstractPlatformConsumer;
import com.robo4j.core.platform.ClientPlatformException;
import com.robo4j.core.util.ConstantUtil;

/**
 * @author Miroslav Wengner (@miragemiko)
 * @since 19.12.2016
 */
public class ClientPlatformConsumer extends AbstractPlatformConsumer implements AgentConsumer, Callable<Boolean> {

	private static final String RIGHT = "right";
	private static final String LEFT = "left";

	private ExecutorService executor;
	private Exchanger<GenericCommand<PlatformUnitCommandEnum>> exchanger;
	private volatile GenericMotor rightMotor;
	private volatile GenericMotor leftMotor;

	public ClientPlatformConsumer(final ExecutorService executor,
			final Exchanger<GenericCommand<PlatformUnitCommandEnum>> exchanger,
			final Map<String, GenericMotor> engineCache) {
		this.executor = executor;
		this.exchanger = exchanger;
		this.rightMotor = engineCache.get(RIGHT);
		this.leftMotor = engineCache.get(LEFT);

	}

	@Override
	public void setMessageQueue(CoreBusQueue commandsQueue) {
		throw new ClientPlatformException("NOT IMPLEMENTED messageQueue");
	}

	@Override
	public Boolean call() throws Exception {

		final GenericCommand<PlatformUnitCommandEnum> command = exchanger.exchange(null);
		final boolean isValue = commandEmpty(command.getValue());
		SimpleLoggingUtil.debug(getClass(), "IsValue: " + isValue + ", command: " + command.getType().getName());
		SimpleLoggingUtil.debug(getClass(), "direction: " + command.getType());
		switch (command.getType()) {
		case LEFT:
			return executeTurn(leftMotor, rightMotor);
		case RIGHT:
			return executeTurn(rightMotor, leftMotor);
		case MOVE:
			return executeBothEngines(MotorRotationEnum.FORWARD, rightMotor, leftMotor);
		case BACK:
			return executeBothEngines(MotorRotationEnum.BACKWARD, rightMotor, leftMotor);
		case STOP:
			return executeBothEngines(MotorRotationEnum.STOP, rightMotor, leftMotor);
		default:
			throw new ClientPlatformException("PLATFORM COMMAND= " + command);
		}

	}

	@Override
	public Future<Boolean> runEngine(GenericMotor engine, MotorRotationEnum rotation) {
		return executor.submit(() -> {
			switch (rotation) {
			/* stop */
			case STOP:
				SimpleLoggingUtil.debug(getClass(), "runEngine STOP");
				engine.stop();
				return engine.isMoving();
			/* forward */
			case FORWARD:
				SimpleLoggingUtil.debug(getClass(), "runEngine FORWARD");
				engine.forward();
				return engine.isMoving();
			/* backward */
			case BACKWARD:
				SimpleLoggingUtil.debug(getClass(), "runEngine BACKWARD");
				engine.backward();
				return true;
			default:
				throw new ClientPlatformException("no such rotation= " + rotation);
			}
		});
	}

	// Private Methods
	private boolean commandEmpty(final String value) {
		return value.equals(ConstantUtil.EMPTY_STRING);
	}

}
