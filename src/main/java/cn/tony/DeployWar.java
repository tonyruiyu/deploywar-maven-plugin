package cn.tony;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import cn.tony.ssh.Server;

/**
 * 
 * 
 * @author tony
 * 
 */
@Mojo(name = "deploy", requiresProject = true, defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class DeployWar extends AbstractMojo {

	/** war所在的目录 */
	@Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
	private File outputDirectory;

	/** war名称可以加后缀.war 也可以不加 */
	@Parameter(defaultValue = "${projcet.artifactId}", alias = "finalName", required = true)
	private String finalName;

	/** linux服务器host */
	@Parameter(alias = "server-host", required = true)
	private String serverhost;

	/** 服务器端口 */
	@Parameter(defaultValue = "22", alias = "port")
	private Integer port;

	/** 用户名 */
	@Parameter(defaultValue = "root", alias = "user")
	private String username;

	/** 密码 */
	@Parameter(alias = "password", required = true)
	private String password;

	/** tomcat在服务器上的根路径 */
	@Parameter(alias = "tomcat-home", required = true)
	private String tomcatHome;

	/** 是否重启taomcat */
	@Parameter(defaultValue = "true", alias = "restart-tomcat")
	private Boolean restartTomcat;

	/** 是否启用脚本重启 */
	@Parameter(defaultValue = "true", alias = "restart-tomcat-for-kill")
	private Boolean restartForKill;

	/** 是否自定义发布shellcommand */
	@Parameter(defaultValue = "false", alias = "is-run-shell-command")
	private Boolean isRunShellCommand;

	/** ShellCommand */
	@Parameter(defaultValue = "", alias = "shell-command")
	private String shellCommand;

	/** 是否备份过去的war包 */
	@Parameter(defaultValue = "true", alias = "is-backup-war")
	private Boolean isBackUp;

	/** 备份war包的格式 */
	@Parameter(defaultValue = "yyMMdd-HHmmss", alias = "backup-war-format")
	private String backupWarFormat;

	private String warfile = "";

	private Server sshserver = null;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			checkParameter();

			sshserver = new Server(serverhost, port, username, password);
			sshserver.setIsRun(true);
			if (isRunShellCommand) {
				List<String> logs = sshserver.execShellCommand(shellCommand);
				logExec(logs);
				return;
			}

			if (restartTomcat)
				stopTomcat();

			if (isBackUp)
				backup();

			uploadWar();

			if (restartTomcat)
				startTomcat();

		} catch (IOException e) {
			getLog().error(e);
		}
	}

	private void uploadWar() throws IOException {
		sshserver.scpUpload(outputDirectory + "/" + finalName, tomcatHome + "/webapps");
		String webapps_path = tomcatHome + "/webapps/";
		String unzip_war_command = "unzip -d " + webapps_path + warfile + " " + webapps_path + finalName;
		logExec("info -- " +unzip_war_command);
		List<String> unzip_log = sshserver.execShellCommand(unzip_war_command);
		logExec(unzip_log);
		String rm_war_command = "rm -f " + webapps_path + finalName;
		List<String> rm_log = sshserver.execShellCommand(rm_war_command);
		logExec(rm_log);
	}

	private void backup() throws IOException {
		SimpleDateFormat format = new SimpleDateFormat(backupWarFormat);
		String format_tmep = format.format(new Date());
		String webapps_path = tomcatHome + "/webapps/";
		String mv_war_backup_command = "mv -f " + webapps_path + finalName + " " + webapps_path
				+ finalName + "." + format_tmep;
		String back_dir_tar_gz_path = webapps_path + warfile + "-" + format_tmep + ".tar.gz ";
		String tar_prj_dir_command = "tar zcvf " + back_dir_tar_gz_path + webapps_path + warfile;
		String rm_prj_dir_command = "rm -rf " + webapps_path +  warfile;

		List<String> mvlog = sshserver.execShellCommand(mv_war_backup_command);
		logExec(mvlog);
		List<String> tarlog = sshserver.execShellCommand(tar_prj_dir_command);
		logExec(tarlog);
		List<String> rmlog = sshserver.execShellCommand(rm_prj_dir_command);
		logExec(rmlog);
	}

	private void logExec(List<String> logs) {
		for (int i = 0; i < logs.size(); i++) {
			logExec(logs.get(i));
		}
	}

	private void logExec(String log) {
		getLog().info("<sshexec>" + log + "</sshexec>");
	}

	/**
	 * 检查参数
	 */
	private void checkParameter() {
		if (!finalName.toLowerCase().endsWith(".war")) {
			finalName += ".war";
		}
		warfile = finalName.replace(".war", "");
	}

	/**
	 * 停止tomcat
	 * 
	 * @throws IOException
	 */
	private void stopTomcat() throws IOException {

		String command = "source /etc/profile;sh " + tomcatHome + "/bin/shutdown.sh";
		List<String> command_log = sshserver.execShellCommand(command);
		logExec(command_log);

		if (restartForKill) {
			String kill_tomcat_command = "kill -9 `ps -ef|grep " + tomcatHome + "|grep -v grep`";
			List<String> kill_log = sshserver.execShellCommand(kill_tomcat_command);
			logExec(kill_log);
		}
	}

	/**
	 * 启动tomcat
	 * 
	 * @throws IOException
	 */
	private void startTomcat() throws IOException {
		String command = "source /etc/profile;sh " + tomcatHome + "/bin/startup.sh";
		List<String> command_log = sshserver.execShellCommand(command);
		logExec(command_log);
	}

}
