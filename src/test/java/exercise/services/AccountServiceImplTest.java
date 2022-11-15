package exercise.services;

import org.junit.Assert;
import org.junit.Test;

public class AccountServiceImplTest {

    @Test
    public void getAccountNumber() {
        AccountServiceImpl accountService = new AccountServiceImpl();
        Long number = accountService.getAccountNumber("Fedor");
        Assert.assertEquals(12345689, number.longValue());
    }
}
