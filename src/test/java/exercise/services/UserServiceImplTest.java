package exercise.services;

import org.junit.Assert;
import org.junit.Test;

public class UserServiceImplTest {

    @Test
    public void getUserName() {
        UserServiceImpl userService=new UserServiceImpl();
        String name= userService.getUserName();
        Assert.assertEquals("username", name);
    }
}
