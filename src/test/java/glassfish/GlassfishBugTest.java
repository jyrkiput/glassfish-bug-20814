package glassfish;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@RunWith(Arquillian.class)
public class GlassfishBugTest {

    @Deployment(order = 1, name="server", testable = true)
    public static JavaArchive deployServer() {
        return ShrinkWrap
                .create(JavaArchive.class, "serverapp.jar")
                .addClass(DemoBean.class)
                .addAsManifestResource("glassfish-ejb-jar.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }



    @Stateless
    public static class DemoBean implements Demo {
        public OuterContainer getMap(OuterContainer outerContainer) {
            outerContainer.container.map.put("foo", new Value("value"));
            return new OuterContainer(new Container());

        }
    }
    @EJB(mappedName = "java:global/test/DemoBean")
    Demo demoBean;

    @Remote
    public interface Demo {
        OuterContainer getMap(OuterContainer container);
    }

    public static class Container implements Serializable {
        final Map<Object, Value> map = new HashMap<Object, Value>();
    }

    public static class OuterContainer implements Serializable {
        final Container container;

        public OuterContainer(Container container) {
            this.container = container;
        }
    }

    public static class Value {
        private final String value;

        public Value(String value) {
            this.value = value;
        }
    }

    @Test
    public void fails() throws NamingException {
        Demo demoBean = (Demo) new InitialContext().lookup("java:global/test/DemoBean");
        OuterContainer container = demoBean.getMap(new OuterContainer(new Container()));
        container.container.map.put("foo", new Value("value"));
    }

}
