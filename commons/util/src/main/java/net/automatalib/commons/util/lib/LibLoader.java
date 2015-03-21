/* Copyright (C) 2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * AutomataLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * AutomataLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with AutomataLib; if not, see
 * http://www.gnu.de/documents/lgpl.en.html.
 */
package net.automatalib.commons.util.lib;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;


/**
 * Utility (singleton) class to manage loading of native libraries.
 * 
 * @author Malte Isberner
 *
 */
public class LibLoader {
	
	private static final Logger LOG = Logger.getLogger(LibLoader.class.getName());
	
	private static final LibLoader INSTANCE = new LibLoader();
	
	public static LibLoader getInstance() {
		return INSTANCE;
	}
	
	private final String libPrefix;
	private final String libExtension;
	private final Path tempLibDir;
	private final Set<String> loaded = new HashSet<>();
	
	private LibLoader() {
		// Read architecture properties
		if (PlatformProperties.OS_NAME.startsWith("windows")) {
			this.libPrefix = "";
		}
		else {
			this.libPrefix = "lib";
		}
		
		if (PlatformProperties.OS_NAME.startsWith("windows")) {
			this.libExtension = "dll";
		}
		else if (PlatformProperties.OS_NAME.startsWith("mac")) {
			this.libExtension = "dylib";
		}
		else {
			// assume OS is some UNIX
			this.libExtension = "so";
		}
		
		Path tmpDir = null;
		try {
			tmpDir = Files.createTempDirectory(getClass().getName());
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[])field.get(null);
			String[] newPaths = new String[paths.length + 1];
			System.arraycopy(paths, 0, newPaths, 0, paths.length);
			newPaths[paths.length] = tmpDir.toString();
			field.set(null, newPaths);
		}
		catch (Exception ex) {
			LOG.severe("Error setting up classloader for custom library loading: " + ex.getMessage());
			LOG.severe("Loading of shipped libraries may fail");
		}
		this.tempLibDir = tmpDir;
	}
	
	private void loadShippedLibrary(Class<?> clazz, String name) throws LoadLibraryException {
		String libFileName = libPrefix + name + "." + libExtension;
		String libResourcePath = "/lib/" + PlatformProperties.OS_NAME + "/" + PlatformProperties.OS_ARCH + "/" + libFileName;
		InputStream libStream = clazz.getResourceAsStream(libResourcePath);
		if (libStream == null) {
			throw new LoadLibraryException("Could not find shipped library resource '" + libFileName + "'");
		}
		
		Path libPath = tempLibDir.resolve(libFileName);
		try {
			Files.copy(libStream, libPath);
		}
		catch (IOException ex) {
			throw new LoadLibraryException("Could not copy shipped library to local file system at '"
				+ libPath + "': " + ex.getMessage(), ex);
		}
		libPath.toFile().deleteOnExit();
		try {
			System.load(libPath.toString());
		}
		catch (SecurityException ex) {
			throw new LoadLibraryException(ex);
		}
		catch (UnsatisfiedLinkError err) {
			throw new LoadLibraryException(err);
		}
	}
	
	private void loadSystemLibrary(String name) throws LoadLibraryException {
		try {
			System.loadLibrary(name);
		}
		catch (SecurityException ex) {
			throw new LoadLibraryException(ex);
		}
		catch (UnsatisfiedLinkError err) {
			throw new LoadLibraryException(err);
		}
	}
	
	
	public void loadLibrary(Class<?> clazz, String name) {
		loadLibrary(clazz, name, LoadPolicy.PREFER_SHIPPED);
	}
	
	public void loadLibrary(Class<?> clazz, String name, LoadPolicy policy) {
		if (loaded.contains(name)) {
			return;
		}
		
		switch (policy) {
		case PREFER_SHIPPED:
			try {
				loadShippedLibrary(clazz, name);
			}
			catch (LoadLibraryException ex) {
				try {
					loadSystemLibrary(name);
				}
				catch (LoadLibraryException ex2) {
					throw ex;
				}
			}
			break;
		case PREFER_SYSTEM:
			try {
				loadSystemLibrary(name);
			}
			catch (LoadLibraryException ex) {
				try {
					loadShippedLibrary(clazz, name);
				}
				catch (LoadLibraryException ex2) {
					throw ex;
				}
			}
			break;
		case SHIPPED_ONLY:
			loadShippedLibrary(clazz, name);
			break;
		case SYSTEM_ONLY:
			loadSystemLibrary(name);
			break;
		}
		
		loaded.add(name);
	}

}