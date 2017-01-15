/*
 * Copyright (C) 2016-2017, Miroslav Wengner, Marcus Hirt
 * This RpiMotorProvider.java  is part of robo4j.
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

package com.robo4j.rasp.tank.provider;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import com.robo4j.commons.annotation.RoboProvider;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.motor.GenericMotor;
import com.robo4j.commons.registry.BaseRegistryProvider;
import com.robo4j.rpi.motor.RpiBaseMotor;
import com.robo4j.rpi.motor.RpiDevice;
import com.robo4j.rpi.motor.RpiMotor;
import com.robo4j.rpi.motor.RpiMotorException;

/**
 * Engine provider is responsible for the specific motor creation and activation
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 21.12.2016
 */
@RoboProvider(id = "engineProvider")
public class RpiMotorProvider<Type extends RpiMotor> implements BaseRegistryProvider<RpiBaseMotor, Type> {

	private static final int BUS_NUMBER = I2CBus.BUS_1;

	@Override
	public RpiBaseMotor create(RpiMotor motor) {
		RpiDevice device = (RpiDevice) motor;
		try {
			device.setBus(I2CFactory.getInstance(BUS_NUMBER));
			device.setDevice(motor.getAddress());
		} catch (IOException | I2CFactory.UnsupportedBusNumberException e) {
			throw new RpiMotorException("wrong: ", e);
		}
		SimpleLoggingUtil.debug(getClass(), "port= " + ((RpiBaseMotor) device).getPort());
		return (RpiBaseMotor) device;
	}

	@Override
	public Map<String, Type> activate(Map<String, Type> engines) {
		return engines.entrySet().stream().peek(e -> {
			GenericMotor le = e.getValue();
			/* always instance of regulated motor */
			if (le instanceof RpiDevice) {
				create((RpiMotor) le);
				SimpleLoggingUtil.debug(getClass(), "activate motor address: " + ((RpiBaseMotor) le).getAddress());
			}
			SimpleLoggingUtil.debug(getClass(), "activate not implemented yet");
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}
}
