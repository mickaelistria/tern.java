/**
 *  Copyright (c) 2013-2014 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package tern.server.nodejs.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import tern.TernException;

/**
 * node.js process which starts tern server with node.js
 */
public class NodejsProcess {

	/**
	 * The node.js base dir.
	 */
	private final File nodejsBaseDir;

	/**
	 * The node.js tern file.
	 */
	private final File nodejsTernFile;

	/**
	 * The project dir where .tern-project is hosted.
	 */
	private final File projectDir;

	/**
	 * Port of the node.js server.
	 */
	private Integer port;

	/**
	 * true if tern server must be verbose and false otherwise.
	 */
	private boolean verbose;

	/**
	 * true if tern server server won�t write a .tern-port file and false
	 * otherwise.
	 */
	private boolean noPortFile;

	/**
	 * false if the server will shut itself down after five minutes of
	 * inactivity and true otherwise.
	 */
	private boolean persistent;

	/**
	 * node.js process.
	 */
	private Process process;

	/**
	 * StdOut thread.
	 */
	private Thread outThread;

	/**
	 * StdErr thread.
	 */
	private Thread errThread;

	/**
	 * Process listeners.
	 */
	private final List<INodejsProcessListener> listeners;

	/**
	 * Lock used to wait the start of the server to retrieve port in the getPort
	 * method.
	 */
	private final Object lock = new Object();

	/**
	 * StdOut of the node.js process.
	 */
	private class StdOut implements Runnable {

		@Override
		public void run() {
			try {

				// start the node.js process with tern.
				Integer port = null;
				String line = null;
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				try {
					while ((line = br.readLine()) != null) {
						if (port == null) {
							// port was not getted, try to get it.
							if (line.startsWith("Listening on port ")) {
								port = Integer.parseInt(line.substring(
										"Listening on port ".length(),
										line.length()));

								// port is getted, notify that process is
								// started.
								setPort(port);

								synchronized (lock) {
									lock.notifyAll();
								}
								notifyStartProcess();
							}
						} else {
							// notify data
							notifyDataProcess(line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (process != null) {
					process.waitFor();
				}
				notifyStopProcess();
				kill();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	};

	/**
	 * StdErr of the node.js process.
	 */
	private class StdErr implements Runnable {
		@Override
		public void run() {
			String line = null;
			InputStream is = process.getErrorStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			try {
				while ((line = br.readLine()) != null) {
					notifyErrorProcess(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Nodejs process constructor which uses the installed node.js.
	 * 
	 * @param nodejsTernBaseDir
	 *            the node.js tern base dir.
	 * @param projectDir
	 *            the project base dir where .tern-project is hosted.
	 * @throws TernException
	 */
	NodejsProcess(File nodejsTernBaseDir, File projectDir) throws TernException {
		this(null, nodejsTernBaseDir, projectDir);
	}

	/**
	 * Nodejs process constructor.
	 * 
	 * @param nodejsBaseDir
	 *            the node.js base dir.
	 * @param nodejsTernBaseDir
	 *            the node.js tern base dir.
	 * @param projectDir
	 *            the project base dir where .tern-project is hosted.
	 * @throws TernException
	 */
	NodejsProcess(File nodejsBaseDir, File nodejsTernBaseDir, File projectDir)
			throws TernException {
		this.nodejsBaseDir = nodejsBaseDir;
		this.nodejsTernFile = getNodejsTernFile(nodejsTernBaseDir);
		this.projectDir = projectDir;
		this.listeners = new ArrayList<INodejsProcessListener>();
		setNoPortFile(true);
	}

	/**
	 * Returns the node.js tern file.
	 * 
	 * @param nodejsTernBaseDir
	 *            tern base dir.
	 * @return the node.js tern file.
	 * @throws TernException
	 */
	private File getNodejsTernFile(File nodejsTernBaseDir) throws TernException {
		if (nodejsTernBaseDir == null) {
			throw new TernException(
					"You must initialize the base dir of the tern node.js server.");
		}
		File ternServerFile = new File(nodejsTernBaseDir,
				"node_modules/tern/bin/tern");
		if (!ternServerFile.exists()) {
			try {
				throw new TernException("Cannot find tern node.js server at "
						+ ternServerFile.getCanonicalPath());
			} catch (IOException e) {
				throw new TernException("Cannot find tern node.js server at "
						+ ternServerFile.getPath());
			}
		}
		return ternServerFile;
	}

	/**
	 * Create process commands to start tern with node.js
	 * 
	 * @return
	 * @throws IOException
	 */
	protected List<String> createCommands() {
		List<String> commands = new LinkedList<String>();
		if (nodejsBaseDir == null) {
			// for osx, path of node.js should be setted?
			if (new File("/usr/local/bin/node").exists()) {
				commands.add("/usr/local/bin/node");
			}
			if (new File("/opt/local/bin/node").exists()) {
				commands.add("/opt/local/bin/node");
			} else {
				commands.add("node");
			}
		} else {
			commands.add(nodejsBaseDir.getPath());
		}
		try {
			commands.add(nodejsTernFile.getCanonicalPath());
		} catch (IOException e) {
			commands.add(nodejsTernFile.getPath());
		}
		Integer port = getPort();
		if (port != null) {
			commands.add("--port");
			commands.add(port.toString());
		}
		if (isVerbose()) {
			commands.add("--verbose");
			commands.add("1");
		}
		if (isNoPortFile()) {
			commands.add("--no-port-file");
		}
		if (isPersistent()) {
			commands.add("--persistent");
		}
		return commands;
	}

	/**
	 * Start process.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void start() throws NodejsProcessException {
		if (isStarted()) {
			notifyErrorProcess("Nodejs tern Server is already started.");
			throw new NodejsProcessException(
					"Nodejs tern Server is already started.");
		}

		try {
			List<String> commands = createCommands();
			ProcessBuilder builder = new ProcessBuilder(commands);
			// builder.redirectErrorStream(true);
			builder.directory(getProjectDir());
			notifyCreateProcess(commands, projectDir);

			this.process = builder.start();

			outThread = new Thread(new StdOut());
			outThread.setDaemon(true);
			outThread.start();

			errThread = new Thread(new StdErr());
			errThread.setDaemon(true);
			errThread.start();

		} catch (Throwable e) {
			notifyErrorProcess(e.getMessage());
			notifyErrorProcess("");
			throw new NodejsProcessException(e);
		}

	}

	/**
	 * Start the process and returns the port of the started process.
	 * 
	 * @param timeout
	 *            to wait until the process start to retrieve the port to
	 *            return.
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws TernException
	 */
	public int start(long timeout) throws NodejsProcessException,
			InterruptedException {
		if (!isStarted()) {
			start();
		}
		synchronized (lock) {
			lock.wait(timeout);
		}
		if (port == null) {
			throw new NodejsProcessException("Cannot start node process.");
		}
		return getPort();
	}

	/**
	 * Return true id process is started and false otherwise.
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return process != null;
	}

	/**
	 * Kill the process.
	 */
	public void kill() {
		if (process != null) {
			process.destroy();
			process = null;
		}
		if (outThread != null) {
			outThread.interrupt();
			outThread = null;
		}
		if (errThread != null) {
			errThread.interrupt();
			errThread = null;
		}
	}

	/**
	 * Return the node.js port and null if not started.
	 * 
	 * @return
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * Set the port when process is started;
	 * 
	 * @param port
	 */
	void setPort(Integer port) {
		this.port = port;
	}

	/**
	 * Set the verbose.
	 * 
	 * @param verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Returns true if tern is verbose and false otherwise.
	 * 
	 * @return
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * Set true if tern server server won�t write a .tern-port file and false
	 * otherwise.
	 * 
	 * @param noPortFile
	 */
	public void setNoPortFile(boolean noPortFile) {
		this.noPortFile = noPortFile;
	}

	/**
	 * return true if tern server server won�t write a .tern-port file and false
	 * otherwise.
	 * 
	 * @return
	 */
	public boolean isNoPortFile() {
		return noPortFile;
	}

	/**
	 * set false if the server will shut itself down after five minutes of
	 * inactivity and true otherwise.
	 * 
	 * @param persistent
	 */
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
	}

	/**
	 * return false if the server will shut itself down after five minutes of
	 * inactivity and true otherwise.
	 * 
	 * @return
	 */
	public boolean isPersistent() {
		return persistent;
	}

	/**
	 * return the project dir where .tern-project is hosted.
	 * 
	 * @return
	 */
	public File getProjectDir() {
		return projectDir;
	}

	/**
	 * Joint to the stdout thread;
	 * 
	 * @throws InterruptedException
	 */
	public void join() throws InterruptedException {
		if (outThread != null) {
			outThread.join();
		}
	}

	/**
	 * Add the given process listener.
	 * 
	 * @param listener
	 */
	public void addProcessListener(INodejsProcessListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Remove the given process listener.
	 * 
	 * @param listener
	 */
	public void removeProcessListener(INodejsProcessListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Notify start process.
	 */
	private void notifyCreateProcess(List<String> commands, File projectDir) {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onCreate(this, commands, projectDir);
			}
		}
	}

	/**
	 * Notify start process.
	 */
	private void notifyStartProcess() {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onStart(this);
			}
		}
	}

	/**
	 * Notify stop process.
	 */
	private void notifyStopProcess() {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onStop(this);
			}
		}
	}

	/**
	 * Notify data process.
	 * 
	 * @param line
	 */
	private void notifyDataProcess(String line) {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onData(this, line);
			}
		}
	}

	/**
	 * Notify error process.
	 */
	private void notifyErrorProcess(String line) {
		synchronized (listeners) {
			for (INodejsProcessListener listener : listeners) {
				listener.onError(NodejsProcess.this, line);
			}
		}
	}
}
