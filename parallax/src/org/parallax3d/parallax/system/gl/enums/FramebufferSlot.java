/*
 * Copyright 2012 Alex Usachev, thothbot@gmail.com
 * 
 * This file is part of Parallax project.
 * 
 * Parallax is free software: you can redistribute it and/or modify it 
 * under the terms of the Creative Commons Attribution 3.0 Unported License.
 * 
 * Parallax is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Creative Commons Attribution 
 * 3.0 Unported License. for more details.
 * 
 * You should have received a copy of the the Creative Commons Attribution 
 * 3.0 Unported License along with Parallax. 
 * If not, see http://creativecommons.org/licenses/by/3.0/.
 */

package org.parallax3d.parallax.system.gl.enums;

import org.parallax3d.parallax.system.gl.GL20;
import org.parallax3d.parallax.system.gl.GLES20Ext;

public enum FramebufferSlot implements GLEnum
{
	COLOR_ATTACHMENT0(GL20.GL_COLOR_ATTACHMENT0),
	DEPTH_ATTACHMENT(GL20.GL_DEPTH_ATTACHMENT),
	STENCIL_ATTACHMENT(GL20.GL_STENCIL_ATTACHMENT),
	DEPTH_STENCIL_ATTACHMENT(GLES20Ext.GL_DEPTH_STENCIL_ATTACHMENT);

	private final int value;

	private FramebufferSlot(int value) {
		this.value = value;
	}

	@Override
	public int getValue() {
		return value;
	}
}
