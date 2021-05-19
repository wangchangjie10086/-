import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.Assert;
import org.junit.Test;



public class ShiroTest {
    @Test
    public void testHelloWorld(){
        //1.获取SecurityManager工厂，此处使用shiro.ini来初始化工厂
        Factory<org.apache.shiro.mgt.SecurityManager> factory = new IniSecurityManagerFactory("classpath:dev/shiro.ini");
        //创建SecurityManager实例
        org.apache.shiro.mgt.SecurityManager securityManager = factory.getInstance();
        //把实例绑定给SecurityUtils
        SecurityUtils.setSecurityManager(securityManager);
        //得到Subject并创建用户名密码，创建token
        Subject subject = SecurityUtils.getSubject();

        UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken("zhang","123");

        try {
            //登录操作
            subject.login(usernamePasswordToken);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }

        System.out.println(subject.isAuthenticated());
        //验证是否登录成功
        Assert.assertEquals(true,subject.isAuthenticated());
        //退出登录
        subject.logout();
    }
}
