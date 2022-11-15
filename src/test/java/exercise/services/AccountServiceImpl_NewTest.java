package exercise.services;

import org.junit.Assert;
import org.junit.Test;

public class AccountServiceImpl_NewTest {

    @Test
    public void getAccountNumber() {
        AccountServiceImplNew accountService = new AccountServiceImplNew();
        Long number = accountService.getAccountNumber("Fedor");
        Assert.assertNotNull(number);
        Assert.assertEquals(888888888L, number.longValue());
    }
}