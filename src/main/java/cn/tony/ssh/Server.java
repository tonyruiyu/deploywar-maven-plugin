package cn.tony.ssh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class Server {

	private Boolean isRun;

	public Boolean getIsRun() {
		return isRun;
	}

	public void setIsRun(Boolean isRun) {
		this.isRun = isRun;
	}

	private Connection connection;

	public Server(String host, Integer port, String user, String password) throws IOException {
		super();
		connection = createConn(host, port, user, password);
	}

	private Connection createConn(String host, Integer port, String user, String password) throws IOException {
		Connection connection = new Connection(host, port);
		connection.connect();
		boolean auth = connection.authenticateWithPassword(user, password);
		if (!auth)
			new IOException("Authentication failed.");
		return connection;
	}

	/**
	 * 上传文件到服务器上
	 * 
	 * @param conn
	 *            Connection
	 * @param localFile
	 *            本地文件
	 * @param remoteDirectory
	 *            远程文件夹
	 * @throws IOException
	 */
	public void scpUpload(String localFile, String remoteDirectory) throws IOException {
		if (!isRun)
			return;
		SCPClient client = connection.createSCPClient();
		client.put(localFile, remoteDirectory);
	}

	/**
	 * 运行shellcommand
	 * 
	 * @param commands
	 *            命令
	 * @return
	 * @throws IOException
	 */
	public List<String> execShellCommand(String... commands) throws IOException {
		List<String> resp = new ArrayList<String>();

		String command = StringUtils.join(commands, ";");
		resp.add(StringUtils.join(commands, "\n"));
		if (!isRun)
			return resp;
		Session session = connection.openSession();
		session.execCommand(command);

		InputStream is = null;
		BufferedReader reader = null;
		try {

			is = new StreamGobbler(session.getStdout());
			reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null) {
				resp.add(line);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
			reader = null;
			is = null;
			if (session != null) {
				session.close();

			}
			session = null;
		}
		return resp;
	}

	/**
	 * 退出登录
	 */
	public void close() {
		if (connection != null)
			connection.close();
	}

}
