import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import cn.tony.DeployWar;


public class TestMojo extends AbstractMojoTestCase {

	protected void setUp() throws Exception {
        super.setUp();
    }
	
	
	public void testDeployMojo() throws Exception{
		File file = new File(this.getClassLoader().getResource("plugin-test.xml").getFile());
		System.out.println(file.getAbsolutePath());
		DeployWar mojo = (DeployWar)lookupMojo("deploy",file);
		mojo.execute();
	}
}
