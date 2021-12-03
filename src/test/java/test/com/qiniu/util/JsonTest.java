package test.com.qiniu.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.google.gson.Gson;
import com.qiniu.util.Json;
import com.qiniu.util.StringMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class JsonTest {
    @Test
    @Tag("UnitTest")
    public void testMapToString() {
        StringMap map = new StringMap().put("k", "v").put("n", 1);
        String j = Json.encode(map);
        assertTrue(j.equals("{\"k\":\"v\",\"n\":1}") || j.equals("{\"n\":1,\"k\":\"v\"}"));
    }

    @Test
    @Tag("UnitTest")
    public void testJ0() {
        User u = new User("a", 23);
        u.s1 = "s1";
        u.i1 = 1;
        String j = u.toString();
        System.out.println(u);
        System.out.println(j);
        assertTrue(j.indexOf("s1") == -1, " no s1, i1 fields");
    }

    @Test
    @Tag("UnitTest")
    public void testJ() {
        String s = "{\"name\":\"怪盗kidou\",\"age\":24}";
        User u = new Gson().fromJson(s, User.class);
        System.out.println(u);
        assertEquals("怪盗kidou", u.name);
        assertEquals(24, u.age);
    }

    @Test
    @Tag("UnitTest")
    public void testJ2() {
        String s = "{\"emailAddress\":\"怪盗kidou\",\"age\":24}";
        User u = new Gson().fromJson(s, User.class);
        System.out.println(u);
        assertEquals("怪盗kidou", u.emailAddress);
        assertEquals(24, u.age);
    }

    @Test
    @Tag("UnitTest")
    public void testJ3() {
        String s = "{\"name\":\"怪盗kidou\",\"age\":24, \"emailAddress\":\"s.com\", \"xxx\":98}";
        User u = new Gson().fromJson(s, User.class);
        System.out.println(u);
        assertEquals("怪盗kidou", u.name);
        assertEquals("s.com", u.emailAddress);
        assertEquals(24, u.age);
    }

    private class User {
        transient String s1;
        transient int i1;
        // 省略其它
        private String name;
        private int age;
        private String emailAddress;
        private String nameNull;

        User(String name, int age) {
            this.name = name;
            this.age = age;
            // this.emailAddress = emailAddress;
        }

        public String toString() {
            return new Gson().toJson(this);
        }
    }
}
